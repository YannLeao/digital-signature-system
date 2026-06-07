package com.example.backend.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TotpEncryptionServiceTests {

	private static final String VALID_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

	@Test
	void encryptsWithUniqueIvAndDecryptsOriginalSecret() {
		TotpEncryptionService service = new TotpEncryptionService(VALID_KEY);

		String first = service.encrypt("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP");
		String second = service.encrypt("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP");

		assertThat(first).isNotEqualTo(second);
		assertThat(first).doesNotContain("JBSWY3DPEHPK3PXP");
		assertThat(service.decrypt(first)).isEqualTo("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP");
		assertThat(service.decrypt(second)).isEqualTo("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP");
	}

	@Test
	void rejectsEncryptionKeyWithWrongSizeAtStartup() {
		assertThatThrownBy(() -> new TotpEncryptionService("AAAA"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("TOTP_ENCRYPTION_KEY_BASE64");
	}
}
