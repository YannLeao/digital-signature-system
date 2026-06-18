package com.example.backend.dto.auth;

import com.example.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Senha atual obrigatoria.")
        String currentPassword,

        @NotBlank(message = "Nova senha obrigatoria.")
        @StrongPassword
        String newPassword
) {
}
