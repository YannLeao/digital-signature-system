package com.example.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TotpVerifyRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "Código TOTP deve ter 6 dígitos numéricos.")
        String code
) {}