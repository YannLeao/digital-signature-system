package com.example.backend.repository;

import com.example.backend.domain.User;
import com.example.backend.entity.Passkey;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.UserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.Base64UrlException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebAuthnCredentialRepository implements CredentialRepository {

    private final PasskeyRepository passkeyRepository;
    private final UserRepository userRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isEmpty()) {
            return Collections.emptySet();
        }
        
        return passkeyRepository.findByUserAndActiveTrue(userOpt.get()).stream()
            .map(passkey -> {
                try {
                    return PublicKeyCredentialDescriptor.builder()
                        .id(ByteArray.fromBase64Url(passkey.getCredentialId()))
                        .build();
                } catch (Base64UrlException e) {
                    throw new RuntimeException("Erro ao decodificar credentialId do banco", e);
                }
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return userRepository.findByEmail(username)
            .map(user -> new ByteArray(user.getId().toString().getBytes()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        try {
            UUID userId = UUID.fromString(new String(userHandle.getBytes()));
            return userRepository.findById(userId).map(User::getEmail);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return passkeyRepository.findByCredentialIdAndActiveTrue(credentialId.getBase64Url())
            .map(passkey -> {
                try {
                    return RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(userHandle)
                        .publicKeyCose(ByteArray.fromBase64Url(passkey.getPublicKey()))
                        .signatureCount(passkey.getCounter())
                        .build();
                } catch (Base64UrlException e) {
                    throw new RuntimeException("Erro ao decodificar publicKey do banco", e);
                }
            });
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return passkeyRepository.findByCredentialIdAndActiveTrue(credentialId.getBase64Url()).stream()
            .map(passkey -> {
                try {
                    return RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(new ByteArray(passkey.getUser().getId().toString().getBytes()))
                        .publicKeyCose(ByteArray.fromBase64Url(passkey.getPublicKey()))
                        .signatureCount(passkey.getCounter())
                        .build();
                } catch (Base64UrlException e) {
                    throw new RuntimeException("Erro ao decodificar chaves no lookupAll", e);
                }
            })
            .collect(Collectors.toSet());
    }
}