package com.example.backend.service;

import com.example.backend.domain.Passkey;
import com.example.backend.domain.AuditAction;
import com.example.backend.domain.User;
import com.example.backend.dto.passkey.PasskeyResponse;
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
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.session.ActiveSessionService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Service
public class PasskeyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasskeyService.class);
	private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

	private final RelyingParty relyingParty;
	private final UserRepository userRepository;
	private final PasskeyRepository passkeyRepository;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final ActiveSessionService activeSessionService;
	private final AuditService auditService;
	private final Clock clock;
	private final ConcurrentMap<String, PublicKeyCredentialCreationOptions> pendingRegistrationOptions;
	private final ConcurrentMap<String, AssertionRequest> pendingAuthenticationOptions;
	private final Function<String, PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>> assertionParser;

	@Autowired
	public PasskeyService(
			RelyingParty relyingParty,
			UserRepository userRepository,
			PasskeyRepository passkeyRepository,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			ActiveSessionService activeSessionService,
			AuditService auditService
	) {
		this(
				relyingParty,
				userRepository,
				passkeyRepository,
				jwtService,
				refreshTokenService,
				activeSessionService,
				auditService,
				Clock.systemUTC(),
				new ConcurrentHashMap<>(),
				new ConcurrentHashMap<>(),
				PasskeyService::parseAssertionCredential
		);
	}

	PasskeyService(
			RelyingParty relyingParty,
			UserRepository userRepository,
			PasskeyRepository passkeyRepository,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			ActiveSessionService activeSessionService,
			AuditService auditService,
			Clock clock,
			ConcurrentMap<String, PublicKeyCredentialCreationOptions> pendingRegistrationOptions,
			ConcurrentMap<String, AssertionRequest> pendingAuthenticationOptions,
			Function<String, PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>> assertionParser
	) {
		this.relyingParty = relyingParty;
		this.userRepository = userRepository;
		this.passkeyRepository = passkeyRepository;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.activeSessionService = activeSessionService;
		this.auditService = auditService;
		this.clock = clock;
		this.pendingRegistrationOptions = pendingRegistrationOptions;
		this.pendingAuthenticationOptions = pendingAuthenticationOptions;
		this.assertionParser = assertionParser;
	}

	@Transactional
	public PublicKeyCredentialCreationOptions startRegistration(String email) {
		String normalizedEmail = normalizeEmail(email);
		User user = findUserByEmail(normalizedEmail);

		ByteArray userId = new ByteArray(user.getId().toString().getBytes());
		UserIdentity userIdentity = UserIdentity.builder()
				.name(normalizedEmail)
				.displayName(user.getEmail())
				.id(userId)
				.build();

		AuthenticatorSelectionCriteria authenticatorSelection = AuthenticatorSelectionCriteria.builder()
				.residentKey(ResidentKeyRequirement.REQUIRED)
				.userVerification(UserVerificationRequirement.PREFERRED)
				.build();

		PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
				StartRegistrationOptions.builder()
						.user(userIdentity)
						.authenticatorSelection(authenticatorSelection)
						.build()
		);

		// Temporary until a distributed challenge/session store exists.
		pendingRegistrationOptions.put(normalizedEmail, options);
		return options;
	}

	@Transactional
	public void finishRegistration(String email, String credentialJson, String deviceName) {
		String normalizedEmail = normalizeEmail(email);
		User user = findUserByEmail(normalizedEmail);
		PublicKeyCredentialCreationOptions options = pendingRegistrationOptions.remove(normalizedEmail);

		if (options == null) {
			throw new BusinessException("Registro de passkey nao iniciado.");
		}

		try {
			PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential =
					PublicKeyCredential.parseRegistrationResponseJson(credentialJson);

			com.yubico.webauthn.RegistrationResult result = relyingParty.finishRegistration(
					FinishRegistrationOptions.builder()
							.request(options)
							.response(credential)
							.build()
			);

			Passkey passkey = Passkey.register(
					user,
					result.getKeyId().getId().getBase64Url(),
					result.getPublicKeyCose().getBase64Url(),
					result.getSignatureCount(),
					formatAaguid(result.getAaguid()),
					deviceName,
					Instant.now(clock)
			);

			passkeyRepository.save(passkey);
		} catch (Exception exception) {
			LOGGER.warn("Falha na validacao da credencial de passkey durante registro para usuario: {}", normalizedEmail, exception);
			throw new BusinessException("Credencial de passkey invalida.");
		}
	}

	public List<PasskeyResponse> getUserPasskeys(String email) {
		User user = findUserByEmail(normalizeEmail(email));

		return passkeyRepository.findByUserAndActiveTrue(user)
				.stream()
				.map(passkey -> new PasskeyResponse(
						passkey.getId(),
						passkey.getDeviceName(),
						passkey.getCreatedAt(),
						passkey.getLastUsed(),
						passkey.isActive()
				))
				.toList();
	}

	@Transactional
	public void deletePasskey(Long passkeyId, String email) {
		User user = findUserByEmail(normalizeEmail(email));
		Passkey passkey = passkeyRepository.findById(passkeyId)
				.orElseThrow(ResourceNotFoundException::new);

		if (!passkey.getUser().getId().equals(user.getId())) {
			throw new ResourceNotFoundException();
		}

		passkey.deactivate();
		passkeyRepository.save(passkey);
	}

	@Transactional
	public PublicKeyCredentialRequestOptions startAuthentication(String email) {
		String normalizedEmail = normalizeEmail(email);
		findUserByEmail(normalizedEmail);

		AssertionRequest assertionRequest = relyingParty.startAssertion(
				StartAssertionOptions.builder()
						.username(Optional.of(normalizedEmail))
						.build()
		);

		pendingAuthenticationOptions.put(normalizedEmail, assertionRequest);
		return assertionRequest.getPublicKeyCredentialRequestOptions();
	}

	@Transactional
	public RefreshTokenResult finishAuthentication(String email, String credentialJson, ClientContext clientContext) {
		String normalizedEmail = normalizeEmail(email);
		User user = findUserByEmail(normalizedEmail);
		AssertionRequest assertionRequest = pendingAuthenticationOptions.remove(normalizedEmail);
		if (assertionRequest == null) {
			throw new PasskeyAuthenticationFailedException();
		}

		try {
			PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
					assertionParser.apply(credentialJson);
			AssertionResult result = relyingParty.finishAssertion(
					FinishAssertionOptions.builder()
							.request(assertionRequest)
							.response(credential)
							.build()
			);

			if (!result.isSuccess()) {
				throw new PasskeyAuthenticationFailedException();
			}

			Passkey passkey = passkeyRepository.findByCredentialIdAndActiveTrue(credential.getId().getBase64Url())
					.orElseThrow(PasskeyAuthenticationFailedException::new);

			if (!passkey.getUser().getId().equals(user.getId())) {
				throw new PasskeyAuthenticationFailedException();
			}

			validateAndUpdateCounter(passkey, result.getSignatureCount(), normalizedEmail, clientContext.ipAddress());

			UUID sessionId = UUID.randomUUID();
			AccessToken accessToken = jwtService.issueAccessToken(user, clientContext, sessionId);
			RefreshTokenPair refreshToken = refreshTokenService.issueForLogin(user, clientContext, sessionId);
			activeSessionService.register(sessionId, user.getId(), clientContext);
			auditService.logSuccess(user.getId(), AuditAction.LOGIN, clientContext.ipAddress(), clientContext.userAgent());
			auditService.logSuccess(user.getId(), AuditAction.TOKEN_ISSUED, clientContext.ipAddress(), clientContext.userAgent());
			return new RefreshTokenResult(accessToken, refreshToken.rawToken(), sessionId);
		} catch (PasskeyAuthenticationFailedException exception) {
			auditService.logFailure(user.getId(), AuditAction.AUTH_FAIL, clientContext.ipAddress(), clientContext.userAgent());
			throw exception;
		} catch (Exception exception) {
			LOGGER.warn("Falha na validacao da credencial de passkey para usuario: {}", normalizedEmail);
			auditService.logFailure(user.getId(), AuditAction.AUTH_FAIL, clientContext.ipAddress(), clientContext.userAgent());
			throw new PasskeyAuthenticationFailedException();
		}
	}

	private void validateAndUpdateCounter(Passkey passkey, long clientCounter, String email, String clientIp) {
		long storedCounter = passkey.getCounter();
		if (clientCounter > 0 && storedCounter > 0 && clientCounter <= storedCounter) {
			AUDIT_LOG.warn(
					"Possivel clonagem de autenticador detectada. user={}, credentialPrefix={}, storedCounter={}, clientCounter={}, ip={}",
					email,
					credentialPrefix(passkey.getCredentialId()),
					storedCounter,
					clientCounter,
					clientIp
			);
			throw new PasskeyAuthenticationFailedException();
		}

		if (clientCounter > storedCounter) {
			passkey.updateCounter(clientCounter, Instant.now(clock));
		} else {
			passkey.markUsed(Instant.now(clock));
		}
		passkeyRepository.save(passkey);
	}

	private User findUserByEmail(String email) {
		return userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(ResourceNotFoundException::new);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> parseAssertionCredential(
			String credentialJson
	) {
		try {
			return PublicKeyCredential.parseAssertionResponseJson(credentialJson);
		} catch (Exception exception) {
			throw new PasskeyAuthenticationFailedException();
		}
	}

	private String credentialPrefix(String credentialId) {
		if (credentialId == null || credentialId.length() <= 8) {
			return "[redacted]";
		}

		return credentialId.substring(0, 8) + "...";
	}

	static String formatAaguid(ByteArray aaguid) {
		String hex = aaguid.getHex();
		if (hex.length() != 32) {
			return aaguid.getBase64Url();
		}

		return hex.substring(0, 8)
				+ "-"
				+ hex.substring(8, 12)
				+ "-"
				+ hex.substring(12, 16)
				+ "-"
				+ hex.substring(16, 20)
				+ "-"
				+ hex.substring(20);
	}
}
