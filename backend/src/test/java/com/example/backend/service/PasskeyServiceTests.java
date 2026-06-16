package com.example.backend.service;

import com.example.backend.domain.Passkey;
import com.example.backend.domain.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.PasskeyAuthenticationFailedException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.AccessToken;
import com.example.backend.security.ClientContext;
import com.example.backend.security.JwtService;
import com.example.backend.security.RefreshTokenPair;
import com.example.backend.security.RefreshTokenResult;
import com.example.backend.service.auth.RefreshTokenService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasskeyServiceTests {

	private static final Instant NOW = Instant.parse("2026-05-20T12:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
	private static final ClientContext CLIENT_CONTEXT = new ClientContext("203.0.113.10", "JUnit/5");

	private final RelyingParty relyingParty = mock(RelyingParty.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasskeyRepository passkeyRepository = mock(PasskeyRepository.class);
	private final JwtService jwtService = mock(JwtService.class);
	private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
	private final ConcurrentMap<String, PublicKeyCredentialCreationOptions> pendingOptions = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AssertionRequest> pendingAuthenticationOptions = new ConcurrentHashMap<>();

	private final ByteArray credentialId = new ByteArray("credential-id".getBytes());
	private final PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential = credential();

	private final PasskeyService service = new PasskeyService(
			relyingParty,
			userRepository,
			passkeyRepository,
			jwtService,
			refreshTokenService,
			CLOCK,
			pendingOptions,
			pendingAuthenticationOptions,
			ignored -> credential
	);

	@Test
	void storesServerGeneratedOptionsWhenStartingRegistration() {
		User user = user();
		PublicKeyCredentialCreationOptions options = mock(PublicKeyCredentialCreationOptions.class);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
		when(relyingParty.startRegistration(any(StartRegistrationOptions.class))).thenReturn(options);

		PublicKeyCredentialCreationOptions result = service.startRegistration(" User@Example.com ");

		assertThat(result).isSameAs(options);
		assertThat(pendingOptions).containsEntry("user@example.com", options);
	}

	@Test
	void failsStartRegistrationWhenUserDoesNotExist() {
		when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.startRegistration("missing@example.com"))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void failsFinishRegistrationWhenChallengeWasNotStarted() {
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user()));

		assertThatThrownBy(() -> service.finishRegistration("user@example.com", "{}", "Notebook"))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Registro de passkey nao iniciado.");

		verify(passkeyRepository, never()).save(any());
	}

	@Test
	void formatsAaguidAsCanonicalUuid() {
		ByteArray aaguid = new ByteArray(new byte[] {
				0x00, 0x01, 0x02, 0x03,
				0x04, 0x05,
				0x06, 0x07,
				0x08, 0x09,
				0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
		});

		assertThat(PasskeyService.formatAaguid(aaguid))
				.isEqualTo("00010203-0405-0607-0809-0a0b0c0d0e0f");
	}

	@Test
	void storesServerGeneratedOptionsWhenStartingAuthentication() {
		AssertionRequest assertionRequest = mock(AssertionRequest.class);
		PublicKeyCredentialRequestOptions options = mock(PublicKeyCredentialRequestOptions.class);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user()));
		when(relyingParty.startAssertion(any(StartAssertionOptions.class))).thenReturn(assertionRequest);
		when(assertionRequest.getPublicKeyCredentialRequestOptions()).thenReturn(options);

		PublicKeyCredentialRequestOptions result = service.startAuthentication(" User@Example.com ");

		assertThat(result).isSameAs(options);
		assertThat(pendingAuthenticationOptions).containsEntry("user@example.com", assertionRequest);
	}

	@Test
	void authenticatesPasskeyAndUpdatesCounterWhenCounterIncreases() throws Exception {
		User user = user();
		Passkey passkey = passkey(user, 7);
		AssertionRequest assertionRequest = mock(AssertionRequest.class);
		AssertionResult assertionResult = assertionResult(true, 8);
		pendingAuthenticationOptions.put("user@example.com", assertionRequest);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
		when(relyingParty.finishAssertion(any())).thenReturn(assertionResult);
		when(passkeyRepository.findByCredentialIdAndActiveTrue(credentialId.getBase64Url())).thenReturn(Optional.of(passkey));
		when(jwtService.issueAccessToken(any(), any(), any())).thenReturn(new AccessToken("jwt-token", "Bearer", 900));
		when(refreshTokenService.issueForLogin(any(), any(), any())).thenReturn(new RefreshTokenPair("refresh-token", null));

		RefreshTokenResult result = service.finishAuthentication("user@example.com", "{}", CLIENT_CONTEXT);

		assertThat(result.accessToken().token()).isEqualTo("jwt-token");
		assertThat(result.refreshToken()).isEqualTo("refresh-token");
		assertThat(passkey.getCounter()).isEqualTo(8);
		assertThat(passkey.getLastUsed()).isEqualTo(NOW);
		verify(passkeyRepository).save(passkey);
		verify(jwtService).issueAccessToken(any(), any(), any());
		verify(refreshTokenService).issueForLogin(any(), any(), any());
	}

	@Test
	void rejectsEqualCounterWithoutUpdatingOrIssuingTokens() throws Exception {
		assertInvalidCounterRejected(7, 7);
	}

	@Test
	void authenticatesPasskeyWhenAuthenticatorDoesNotSupportCounter() throws Exception {
		User user = user();
		Passkey passkey = passkey(user, 0);
		AssertionRequest assertionRequest = mock(AssertionRequest.class);
		AssertionResult assertionResult = assertionResult(true, 0);
		pendingAuthenticationOptions.put("user@example.com", assertionRequest);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
		when(relyingParty.finishAssertion(any())).thenReturn(assertionResult);
		when(passkeyRepository.findByCredentialIdAndActiveTrue(credentialId.getBase64Url())).thenReturn(Optional.of(passkey));
		when(jwtService.issueAccessToken(any(), any(), any())).thenReturn(new AccessToken("jwt-token", "Bearer", 900));
		when(refreshTokenService.issueForLogin(any(), any(), any())).thenReturn(new RefreshTokenPair("refresh-token", null));

		RefreshTokenResult result = service.finishAuthentication("user@example.com", "{}", CLIENT_CONTEXT);

		assertThat(result.accessToken().token()).isEqualTo("jwt-token");
		assertThat(passkey.getCounter()).isZero();
		assertThat(passkey.getLastUsed()).isEqualTo(NOW);
		verify(passkeyRepository).save(passkey);
		verify(jwtService).issueAccessToken(any(), any(), any());
		verify(refreshTokenService).issueForLogin(any(), any(), any());
	}

	@Test
	void rejectsLowerCounterWithoutUpdatingOrIssuingTokens() throws Exception {
		assertInvalidCounterRejected(7, 6);
	}

	@Test
	void rejectsMissingCredentialWithoutIssuingTokens() throws Exception {
		AssertionRequest assertionRequest = mock(AssertionRequest.class);
		AssertionResult assertionResult = assertionResult(true, 8);
		pendingAuthenticationOptions.put("user@example.com", assertionRequest);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user()));
		when(relyingParty.finishAssertion(any())).thenReturn(assertionResult);
		when(passkeyRepository.findByCredentialIdAndActiveTrue(credentialId.getBase64Url())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.finishAuthentication("user@example.com", "{}", CLIENT_CONTEXT))
				.isInstanceOf(PasskeyAuthenticationFailedException.class);

		verify(jwtService, never()).issueAccessToken(any(), any(), any());
		verify(refreshTokenService, never()).issueForLogin(any(), any(), any());
	}

	@Test
	void rejectsFailedWebAuthnAssertionWithoutIssuingTokens() throws Exception {
		AssertionRequest assertionRequest = mock(AssertionRequest.class);
		AssertionResult assertionResult = assertionResult(false, 8);
		pendingAuthenticationOptions.put("user@example.com", assertionRequest);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user()));
		when(relyingParty.finishAssertion(any())).thenReturn(assertionResult);

		assertThatThrownBy(() -> service.finishAuthentication("user@example.com", "{}", CLIENT_CONTEXT))
				.isInstanceOf(PasskeyAuthenticationFailedException.class);

		verify(passkeyRepository, never()).save(any());
		verify(jwtService, never()).issueAccessToken(any(), any(), any());
		verify(refreshTokenService, never()).issueForLogin(any(), any(), any());
	}

	@Test
	void rejectsFinishAuthenticationWhenChallengeWasNotStarted() {
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user()));

		assertThatThrownBy(() -> service.finishAuthentication("user@example.com", "{}", CLIENT_CONTEXT))
				.isInstanceOf(PasskeyAuthenticationFailedException.class);

		verify(passkeyRepository, never()).save(any());
		verify(jwtService, never()).issueAccessToken(any(), any(), any());
	}

	private void assertInvalidCounterRejected(long storedCounter, long receivedCounter) throws Exception {
		User user = user();
		Passkey passkey = passkey(user, storedCounter);
		AssertionRequest assertionRequest = mock(AssertionRequest.class);
		AssertionResult assertionResult = assertionResult(true, receivedCounter);
		pendingAuthenticationOptions.put("user@example.com", assertionRequest);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
		when(relyingParty.finishAssertion(any())).thenReturn(assertionResult);
		when(passkeyRepository.findByCredentialIdAndActiveTrue(credentialId.getBase64Url())).thenReturn(Optional.of(passkey));

		assertThatThrownBy(() -> service.finishAuthentication("user@example.com", "{}", CLIENT_CONTEXT))
				.isInstanceOf(PasskeyAuthenticationFailedException.class);

		assertThat(passkey.getCounter()).isEqualTo(storedCounter);
		assertThat(passkey.getLastUsed()).isNull();
		verify(passkeyRepository, never()).save(any());
		verify(jwtService, never()).issueAccessToken(any(), any(), any());
		verify(refreshTokenService, never()).issueForLogin(any(), any(), any());
	}

	private User user() {
		return User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"User@Example.com",
				"password-hash",
				NOW
		);
	}

	private Passkey passkey(User user, long counter) {
		return Passkey.register(
				user,
				credentialId.getBase64Url(),
				"public-key",
				counter,
				"00000000-0000-0000-0000-000000000000",
				"Notebook",
				NOW.minusSeconds(3600)
		);
	}

	@SuppressWarnings("unchecked")
	private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential() {
		PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> mockedCredential =
				mock(PublicKeyCredential.class);
		when(mockedCredential.getId()).thenReturn(credentialId);
		return mockedCredential;
	}

	private AssertionResult assertionResult(boolean success, long signatureCount) {
		AssertionResult assertionResult = mock(AssertionResult.class);
		when(assertionResult.isSuccess()).thenReturn(success);
		when(assertionResult.getSignatureCount()).thenReturn(signatureCount);
		return assertionResult;
	}
}
