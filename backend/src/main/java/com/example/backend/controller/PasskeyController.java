package com.example.backend.controller;

import com.example.backend.dto.PasskeyFinishRequest;
import com.example.backend.dto.PasskeyResponse;
import com.example.backend.dto.PasskeyStartRequest;
import com.example.backend.service.PasskeyService;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/passkey")
@RequiredArgsConstructor
public class PasskeyController {
    
    private final PasskeyService passkeyService;
    
    @PostMapping("/register/start")
    public ResponseEntity<PublicKeyCredentialCreationOptions> startRegistration(
            @Valid @RequestBody PasskeyStartRequest request) {
        PublicKeyCredentialCreationOptions options = passkeyService.startRegistration(request.getEmail());
        return ResponseEntity.ok(options);
    }
    
    @PostMapping("/register/finish")
    public ResponseEntity<Void> finishRegistration(
            @Valid @RequestBody PasskeyFinishRequest request) {
        passkeyService.finishRegistration(
            request.getEmail(),
            request.getCredential(),
            request.getDeviceName(),
            request.getOptionsRequest()
        );
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/devices")
    public ResponseEntity<List<PasskeyResponse>> getUserDevices(@RequestParam String email) {
        List<PasskeyResponse> devices = passkeyService.getUserPasskeys(email);
        return ResponseEntity.ok(devices);
    }
    
    @DeleteMapping("/device/{id}")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable Long id,
            @RequestParam String email) {
        passkeyService.deletePasskey(id, email);
        return ResponseEntity.noContent().build();
    }
}