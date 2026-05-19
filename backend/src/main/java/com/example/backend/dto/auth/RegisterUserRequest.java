package com.example.backend.dto.auth;

import com.example.backend.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
		@NotBlank(message = "E-mail obrigatorio.")
		@Email(message = "E-mail invalido.")
		@Size(max = 320, message = "E-mail deve ter no maximo 320 caracteres.")
		String email,

		@NotBlank(message = "Senha obrigatoria.")
		@StrongPassword
		String password
) {
}
