package com.example.backend.config;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class WebAuthnConfig {

	private final String rpId;
	private final String rpName;
	private final String origin;

	public WebAuthnConfig(
			@Value("${WEBAUTHN_RP_ID}") String rpId,
			@Value("${WEBAUTHN_RP_NAME}") String rpName,
			@Value("${WEBAUTHN_ORIGIN}") String origin
	) {
		this.rpId = rpId;
		this.rpName = rpName;
		this.origin = origin;
	}

	@Bean
	public RelyingParty relyingParty(CredentialRepository credentialRepository) {
		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
				.id(rpId)
				.name(rpName)
				.build();

		return RelyingParty.builder()
				.identity(rpIdentity)
				.credentialRepository(credentialRepository)
				.origins(Set.of(origin))
				.build();
	}
}
