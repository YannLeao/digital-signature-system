package com.example.backend.service.auth;

import com.example.backend.domain.User;
import com.example.backend.domain.UserKey;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.security.UserKeyEncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;

@Service
public class UserKeyService {

    private final UserKeyRepository       userKeyRepository;
    private final UserKeyEncryptionService encryptionService;
    private final String                  keyAlgorithm;

    public UserKeyService(
            UserKeyRepository userKeyRepository,
            UserKeyEncryptionService encryptionService,
            @Value("${app.user-key.algorithm:RSA}") String keyAlgorithm) {
        this.userKeyRepository = userKeyRepository;
        this.encryptionService  = encryptionService;
        this.keyAlgorithm       = keyAlgorithm.toUpperCase();
    }

    @Transactional
    public void generateAndStoreKeyPair(User user, Instant now) {
        KeyPair keyPair = generateKeyPair();

        String publicKeyB64  = Base64.getEncoder()
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
                case "RSA" -> {
                    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                    gen.initialize(2048);
                    yield gen.generateKeyPair();
                }
                case "ECDSA" -> {
                    KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
                    gen.initialize(new ECGenParameterSpec("secp256r1"));
                    yield gen.generateKeyPair();
                }
                default -> throw new IllegalArgumentException(
                        "Algoritmo de chave não suportado: " + keyAlgorithm);
            };
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar par de chaves", e);
        }
    }
}