package com.example.backend.dto.passkey;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasskeyStartRequest(
		@NotBlank(message = "E-mail obrigatorio.")
		@Email(message = "E-mail invalido.")
		String email
) {
}
