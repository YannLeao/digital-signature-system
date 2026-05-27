package com.example.backend.dto.passkey;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasskeyAuthenticationFinishRequest(
		@NotBlank(message = "E-mail obrigatorio.")
		@Email(message = "E-mail invalido.")
		String email,

		@NotBlank(message = "Credencial obrigatoria.")
		String credential
) {
}
