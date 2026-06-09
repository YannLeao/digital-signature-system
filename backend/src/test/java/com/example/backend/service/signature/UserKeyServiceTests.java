package com.example.backend.service.signature;

import com.example.backend.domain.User;
import com.example.backend.domain.UserKey;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.security.UserKeyEncryptionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserKeyServiceTests {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);
    private static final Instant NOW = Instant.parse("2026-06-08T12:00:00Z");

    private final UserKeyRepository userKeyRepository = mock(UserKeyRepository.class);
    private final UserKeyEncryptionService encryptionService = new UserKeyEncryptionService(KEY);

    @Test
    void generatesStoresRsaKeyPairWithEncryptedPrivateKey() throws Exception {
        User user = user("11111111-1111-1111-1111-111111111111", "user@example.com");
        UserKeyService service = new UserKeyService(userKeyRepository, encryptionService, "RSA", new java.security.SecureRandom());

        service.generateAndStoreKeyPair(user, NOW);

        UserKey savedKey = capturedUserKey();
        assertThat(savedKey.getUser()).isSameAs(user);
        assertThat(savedKey.getKeyAlgorithm()).isEqualTo("RSA");
        assertThat(savedKey.getCreatedAt()).isEqualTo(NOW);
        assertThat(savedKey.getPublicKey()).isNotBlank();
        assertThat(savedKey.getEncryptedPrivateKey()).isNotBlank();
        assertThat(savedKey.getEncryptedPrivateKey()).isNotEqualTo(encryptionService.decrypt(savedKey.getEncryptedPrivateKey()));

        PublicKey publicKey = decodePublicKey(savedKey.getPublicKey(), "RSA");
        PrivateKey privateKey = decodePrivateKey(encryptionService.decrypt(savedKey.getEncryptedPrivateKey()), "RSA");
        assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
        assertThat(privateKey.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    void generatesDifferentKeyPairsForDifferentUsers() {
        User firstUser = user("11111111-1111-1111-1111-111111111111", "first@example.com");
        User secondUser = user("22222222-2222-2222-2222-222222222222", "second@example.com");
        UserKeyService service = new UserKeyService(userKeyRepository, encryptionService, "ECDSA", new java.security.SecureRandom());

        service.generateAndStoreKeyPair(firstUser, NOW);
        UserKey firstKey = capturedUserKey();
        service.generateAndStoreKeyPair(secondUser, NOW);
        UserKey secondKey = capturedUserKey();

        assertThat(firstKey.getPublicKey()).isNotEqualTo(secondKey.getPublicKey());
        assertThat(encryptionService.decrypt(firstKey.getEncryptedPrivateKey()))
                .isNotEqualTo(encryptionService.decrypt(secondKey.getEncryptedPrivateKey()));
        assertThat(firstKey.getKeyAlgorithm()).isEqualTo("ECDSA");
        assertThat(secondKey.getKeyAlgorithm()).isEqualTo("ECDSA");
    }

    private UserKey capturedUserKey() {
        ArgumentCaptor<UserKey> captor = ArgumentCaptor.forClass(UserKey.class);
        verify(userKeyRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private User user(String id, String email) {
        return User.register(UUID.fromString(id), email, "password-hash", NOW);
    }

    private PublicKey decodePublicKey(String publicKeyBase64, String algorithm) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(publicKeyBase64);
        return KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(bytes));
    }

    private PrivateKey decodePrivateKey(String privateKeyBase64, String algorithm) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(privateKeyBase64);
        return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }
}

