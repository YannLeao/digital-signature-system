package com.example.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class UserKeyEncryptionService {

    private static final String ALGORITHM  = "AES/GCM/NoPadding";
    private static final int    IV_BYTES   = 12;
    private static final int    TAG_BITS   = 128;
    private static final int    KEY_BYTES  = 32;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    @Autowired
    public UserKeyEncryptionService(
            @Value("${app.user-key.encryption-key-base64}") String masterSecretBase64) {
        this(masterSecretBase64, new SecureRandom());
    }

    UserKeyEncryptionService(String masterSecretBase64, SecureRandom secureRandom) {
        byte[] keyBytes = decodeKey(masterSecretBase64);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = secureRandom;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] result     = new byte[IV_BYTES + ciphertext.length];

            System.arraycopy(iv, 0, result, 0, IV_BYTES);
            System.arraycopy(ciphertext, 0, result, IV_BYTES, ciphertext.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criptografar chave privada", e);
        }
    }

    public String decrypt(String base64Ciphertext) {
        try {
            byte[] decoded    = Base64.getDecoder().decode(base64Ciphertext);
            if (decoded.length <= IV_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted private key payload.");
            }
            byte[] iv         = Arrays.copyOfRange(decoded, 0, IV_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(decoded, IV_BYTES, decoded.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao descriptografar chave privada", e);
        }
    }

    private static byte[] decodeKey(String masterSecretBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(masterSecretBase64);
            if (keyBytes.length != KEY_BYTES) {
                throw new IllegalArgumentException("USER_KEY_ENCRYPTION_KEY_BASE64 must decode to exactly 32 bytes.");
            }
            return keyBytes;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid USER_KEY_ENCRYPTION_KEY_BASE64 configuration.", exception);
        }
    }
}
