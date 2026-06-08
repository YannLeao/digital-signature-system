package com.example.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class UserKeyEncryptionService {

    private static final String ALGORITHM  = "AES/GCM/NoPadding";
    private static final int    IV_BYTES   = 12;
    private static final int    TAG_BITS   = 128;

    private final SecretKey secretKey;

    public UserKeyEncryptionService(
            @Value("${app.user-key.master-secret}") String masterSecretBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(masterSecretBase64);
        this.secretKey  = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
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
            byte[] iv         = Arrays.copyOfRange(decoded, 0, IV_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(decoded, IV_BYTES, decoded.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));

            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao descriptografar chave privada", e);
        }
    }
}