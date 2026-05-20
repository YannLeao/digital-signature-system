package com.example.backend.config;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RelyingParty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebAuthnConfigTests {

	@Test
	void createsRelyingPartyBeanFromConfiguredValues() {
		WebAuthnConfig config = new WebAuthnConfig(
				"localhost",
				"Projeto_Seguranca",
				"http://localhost:5173"
		);

		RelyingParty relyingParty = config.relyingParty(mock(CredentialRepository.class));

		assertThat(relyingParty).isNotNull();
	}
}
