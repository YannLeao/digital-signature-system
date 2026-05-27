package com.example.backend.service;

import com.example.backend.domain.Passkey;
import com.example.backend.domain.User;
import com.example.backend.dto.auth.LoginResponse;
import com.example.backend.dto.passkey.PasskeyResponse;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.UserRepository;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
	private final ConcurrentMap<String, AssertionRequest> pendingAuthenticationOptions;
	
	private static final Logger log = LoggerFactory.getLogger(PasskeyService.class);
	private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

	@Autowired
	public PasskeyService(RelyingParty relyingParty, UserRepository userRepository, PasskeyRepository passkeyRepository) {
		this(relyingParty, userRepository, passkeyRepository, Clock.systemUTC(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
	}

	PasskeyService(
			RelyingParty relyingParty,
			UserRepository userRepository,
			PasskeyRepository passkeyRepository,
			Clock clock,
			ConcurrentMap<String, PublicKeyCredentialCreationOptions> pendingRegistrationOptions,
			ConcurrentMap<String, AssertionRequest> pendingAuthenticationOptions
	) {
		this.relyingParty = relyingParty;
		this.userRepository = userRepository;
		this.passkeyRepository = passkeyRepository;
		this.clock = clock;
		this.pendingRegistrationOptions = pendingRegistrationOptions;
		this.pendingAuthenticationOptions = pendingAuthenticationOptions;
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
	public LoginResponse finishAuthentication(String email, String credentialJson, String clientIp) {
		String normalizedEmail = normalizeEmail(email);
		findUserByEmail(normalizedEmail); 
		AssertionRequest assertionRequest = pendingAuthenticationOptions.remove(normalizedEmail);		
		if (assertionRequest == null) {
			throw new BusinessException("Autenticação de passkey nao iniciada.");
		}

		try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
                    PublicKeyCredential.parseAssertionResponseJson(credentialJson);

            com.yubico.webauthn.AssertionResult result = relyingParty.finishAssertion(
                    com.yubico.webauthn.FinishAssertionOptions.builder()
                            .request(assertionRequest)
                            .response(credential)
                            .build()
            );

            if (!result.isSuccess()) {
                throw new BusinessException("Assinatura digital inválida.");
            }

            String credentialIdStr = credential.getId().getBase64Url();
            Passkey passkey = passkeyRepository.findByCredentialIdAndActiveTrue(credentialIdStr)
                    .orElseThrow(ResourceNotFoundException::new);

            long clientCounter = result.getSignatureCount();
            long storedCounter = passkey.getCounter();

            if (clientCounter <= storedCounter) {
                AUDIT_LOG.error("[CLONAGEM DETECTADA] Usuário: {}, Credencial: {}, Counter DB: {}, Counter Client: {}, IP: {}", 
                        normalizedEmail, credentialIdStr, storedCounter, clientCounter, getClientIp());

				log.error("[AUDITORIA - SEGURANÇA] Bloqueio preventivo: Possível clonagem de autenticador! Usuário: {}. Credencial: {}. Counter recebido: {}, Counter em banco: {}", 
                        normalizedEmail, credentialIdStr, clientCounter, storedCounter);
                
                throw new BusinessException("Inconsistência de hardware de segurança detectada.");
            }

            passkey.updateCounter(clientCounter, Instant.now(clock)); 
    
            
            passkeyRepository.save(passkey);
            log.info("Usuário {} autenticado com sucesso via Passkey.", normalizedEmail);

            String accessToken = "MOCK_JWT_ACCESS_TOKEN";  
            //String refreshToken = "MOCK_REFRESH_TOKEN"; 

            return new LoginResponse(accessToken, 
					"Bearer", 
					3600
			);

        } catch (BusinessException e) {
            throw e; 
        } catch (Exception exception) {
			log.error("Erro na validação da credencial para usuário: {}", normalizedEmail, exception);

            throw new BusinessException("Falha na validação da credencial.");
        }

	}
	private String getClientIp() {
        return "unknown";
    }
}
