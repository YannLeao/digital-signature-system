package com.example.backend.service;

import com.example.backend.domain.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.UserRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
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

	private final RelyingParty relyingParty = mock(RelyingParty.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasskeyRepository passkeyRepository = mock(PasskeyRepository.class);
	private final ConcurrentMap<String, PublicKeyCredentialCreationOptions> pendingOptions = new ConcurrentHashMap<>();
	private final PasskeyService service = new PasskeyService(
			relyingParty,
			userRepository,
			passkeyRepository,
			CLOCK,
			pendingOptions
	);

	@Test
	void storesServerGeneratedOptionsWhenStartingRegistration() {
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"User@Example.com",
				"password-hash",
				NOW
		);
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
		User user = User.register(
				UUID.fromString("22222222-2222-2222-2222-222222222222"),
				"user@example.com",
				"password-hash",
				NOW
		);
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.finishRegistration("user@example.com", "{}", "Notebook"))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Registro de passkey nao iniciado.");

		verify(passkeyRepository, never()).save(any());
	}
}
