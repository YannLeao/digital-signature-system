package com.example.backend.service;

import com.example.backend.domain.Passkey;
import com.example.backend.domain.User;
import com.example.backend.dto.passkey.PasskeyResponse;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.UserRepository;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class PasskeyService {

	private final RelyingParty relyingParty;
	private final UserRepository userRepository;
	private final PasskeyRepository passkeyRepository;
	private final Clock clock;
	private final ConcurrentMap<String, PublicKeyCredentialCreationOptions> pendingRegistrationOptions;

	@Autowired
	public PasskeyService(RelyingParty relyingParty, UserRepository userRepository, PasskeyRepository passkeyRepository) {
		this(relyingParty, userRepository, passkeyRepository, Clock.systemUTC(), new ConcurrentHashMap<>());
	}

	PasskeyService(
			RelyingParty relyingParty,
			UserRepository userRepository,
			PasskeyRepository passkeyRepository,
			Clock clock,
			ConcurrentMap<String, PublicKeyCredentialCreationOptions> pendingRegistrationOptions
	) {
		this.relyingParty = relyingParty;
		this.userRepository = userRepository;
		this.passkeyRepository = passkeyRepository;
		this.clock = clock;
		this.pendingRegistrationOptions = pendingRegistrationOptions;
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
					String.valueOf(result.getAaguid()),
					deviceName,
					Instant.now(clock)
			);

			passkeyRepository.save(passkey);
		} catch (Exception exception) {
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
				.collect(Collectors.toList());
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

	private User findUserByEmail(String email) {
		return userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(ResourceNotFoundException::new);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
