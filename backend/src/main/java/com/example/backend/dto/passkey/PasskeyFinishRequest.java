package com.example.backend.dto.passkey;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasskeyFinishRequest(
		@NotBlank(message = "E-mail obrigatorio.")
		@Email(message = "E-mail invalido.")
		String email,

		@NotBlank(message = "Credencial obrigatoria.")
		String credential,

		@NotBlank(message = "Nome do dispositivo obrigatorio.")
		String deviceName
) {
}
