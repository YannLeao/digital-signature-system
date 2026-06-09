package com.example.backend.service.signature;

import com.example.backend.domain.User;
import com.example.backend.domain.UserKey;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.security.UserKeyEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
public class UserKeyService {

    private static final int RSA_KEY_SIZE = 3072;
    private static final String RSA = "RSA";
    private static final String ECDSA = "ECDSA";
    private static final String EC_CURVE = "secp256r1";

    private final UserKeyRepository userKeyRepository;
    private final UserKeyEncryptionService encryptionService;
    private final String keyAlgorithm;
    private final SecureRandom secureRandom;

    @Autowired
    public UserKeyService(
            UserKeyRepository userKeyRepository,
            UserKeyEncryptionService encryptionService,
            @Value("${app.user-key.algorithm:RSA}") String keyAlgorithm) {
        this(userKeyRepository, encryptionService, keyAlgorithm, new SecureRandom());
    }

    UserKeyService(
            UserKeyRepository userKeyRepository,
            UserKeyEncryptionService encryptionService,
            String keyAlgorithm,
            SecureRandom secureRandom) {
        this.userKeyRepository = userKeyRepository;
        this.encryptionService = encryptionService;
        this.keyAlgorithm = normalizeAlgorithm(keyAlgorithm);
        this.secureRandom = secureRandom;
    }

    @Transactional
    public void generateAndStoreKeyPair(User user, Instant now) {
        KeyPair keyPair = generateKeyPair();

        String publicKeyB64 = Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyB64 = Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());
        String encryptedPrivateKey = encryptionService.encrypt(privateKeyB64);

        UserKey userKey = UserKey.create(user, publicKeyB64, encryptedPrivateKey, keyAlgorithm, now);
        userKeyRepository.save(userKey);
    }

    private KeyPair generateKeyPair() {
        try {
            return switch (keyAlgorithm) {
                case RSA -> {
                    KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
                    generator.initialize(RSA_KEY_SIZE, secureRandom);
                    yield generator.generateKeyPair();
                }
                case ECDSA -> {
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
                    generator.initialize(new ECGenParameterSpec(EC_CURVE), secureRandom);
                    yield generator.generateKeyPair();
                }
                default -> throw new IllegalArgumentException("Unsupported user key algorithm.");
            };
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate user key pair.", exception);
        }
    }

    private String normalizeAlgorithm(String algorithm) {
        if (algorithm == null || algorithm.isBlank()) {
            return RSA;
        }
        return algorithm.trim().toUpperCase(Locale.ROOT);
    }
}
