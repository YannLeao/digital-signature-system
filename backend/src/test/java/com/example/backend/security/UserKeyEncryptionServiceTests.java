package com.example.backend.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserKeyEncryptionServiceTests {

    private static final String KEY = base64Key((byte) 0);
    private static final String OTHER_KEY = base64Key((byte) 1);

    @Test
    void encryptsAndDecryptsPrivateKeyPayload() {
        UserKeyEncryptionService service = new UserKeyEncryptionService(KEY);
        String plaintext = "pkcs8-private-key-base64";

        String ciphertext = service.encrypt(plaintext);

        assertThat(ciphertext).isNotEqualTo(plaintext);
        assertThat(service.decrypt(ciphertext)).isEqualTo(plaintext);
    }

    @Test
    void encryptingSamePlaintextTwiceUsesDifferentIv() {
        UserKeyEncryptionService service = new UserKeyEncryptionService(KEY);
        String plaintext = "same-private-key";

        String first = service.encrypt(plaintext);
        String second = service.encrypt(plaintext);

        assertThat(first).isNotEqualTo(second);
        assertThat(service.decrypt(first)).isEqualTo(plaintext);
        assertThat(service.decrypt(second)).isEqualTo(plaintext);
    }

    @Test
    void decryptWithWrongKeyFailsAuthentication() {
        UserKeyEncryptionService service = new UserKeyEncryptionService(KEY);
        UserKeyEncryptionService wrongKeyService = new UserKeyEncryptionService(OTHER_KEY);

        String ciphertext = service.encrypt("private-key");

        assertThatThrownBy(() -> wrongKeyService.decrypt(ciphertext))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsInvalidMasterKeyLength() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> new UserKeyEncryptionService(shortKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid USER_KEY_ENCRYPTION_KEY_BASE64 configuration.");
    }

    private static String base64Key(byte fill) {
        byte[] key = new byte[32];
        java.util.Arrays.fill(key, fill);
        return Base64.getEncoder().encodeToString(key);
    }
}

