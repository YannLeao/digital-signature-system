package com.example.backend.service;

import com.example.backend.dto.PasskeyResponse;
import com.example.backend.entity.Passkey;
import com.example.backend.domain.User;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.UserRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasskeyService {
    
    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;
    
    @Transactional
    public PublicKeyCredentialCreationOptions startRegistration(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        ByteArray userId = new ByteArray(user.getId().toString().getBytes());
        
        UserIdentity userIdentity = UserIdentity.builder()
            .name(email)
            .displayName(user.getEmail())
            .id(userId)
            .build();
        
        AuthenticatorSelectionCriteria authenticatorSelection = AuthenticatorSelectionCriteria.builder()
            .residentKey(ResidentKeyRequirement.REQUIRED) // Passkey requer resident key
            .userVerification(UserVerificationRequirement.PREFERRED)
            .build();
        
        return relyingParty.startRegistration(
            com.yubico.webauthn.StartRegistrationOptions.builder()
                .user(userIdentity)
                .authenticatorSelection(authenticatorSelection)
                .build()
        );
    }
    
   @Transactional
    public void finishRegistration(String email, String credentialJson, String deviceName, PublicKeyCredentialCreationOptions creationOptions) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                PublicKeyCredential.parseRegistrationResponseJson(credentialJson);
            


            com.yubico.webauthn.RegistrationResult result = relyingParty.finishRegistration(
                com.yubico.webauthn.FinishRegistrationOptions.builder()
                    .request(creationOptions)
                    .response(pkc)
                    .build()
            );
            
            Passkey passkey = new Passkey();
            passkey.setUser(user);
            
            passkey.setCredentialId(result.getKeyId().getId().getBase64Url());
            passkey.setPublicKey(result.getPublicKeyCose().getBase64Url());
            passkey.setCounter(result.getSignatureCount());
            
            passkey.setAaguid(result.getAaguid().toString());
            passkey.setDeviceName(deviceName);
            passkey.setActive(true);
            passkey.setCreatedAt(LocalDateTime.now());
            
            passkeyRepository.save(passkey);
            
            log.info("Passkey registrada com sucesso para usuário: {}", email);
            
        } catch (Exception e) {
            log.error("Erro ao finalizar registro da passkey", e);
            throw new RuntimeException("Falha ao registrar passkey: " + e.getMessage());
        }
    }
    
    public List<PasskeyResponse> getUserPasskeys(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        return passkeyRepository.findByUserAndActiveTrue(user)
            .stream()
            .map(passkey -> PasskeyResponse.builder()
                .id(passkey.getId())
                .deviceName(passkey.getDeviceName())
                .createdAt(passkey.getCreatedAt())
                .lastUsed(passkey.getLastUsed())
                .active(passkey.getActive())
                .build())
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void deletePasskey(Long passkeyId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Passkey passkey = passkeyRepository.findById(passkeyId)
            .orElseThrow(() -> new RuntimeException("Passkey não encontrada"));
        
        if (!passkey.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Passkey não pertence ao usuário");
        }
        
        passkey.setActive(false);
        passkeyRepository.save(passkey);
        
        log.info("Passkey {} desativada para usuário: {}", passkeyId, email);
    }
}