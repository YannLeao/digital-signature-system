package com.example.backend.dto;

import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PasskeyFinishRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String credential;

    @NotBlank
    private String deviceName;

    @NotNull
    private PublicKeyCredentialCreationOptions optionsRequest;
}
