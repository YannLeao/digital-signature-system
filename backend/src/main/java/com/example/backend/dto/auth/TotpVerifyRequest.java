package com.example.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TotpVerifyRequest(
		@NotBlank
		@Pattern(regexp = "(\\d{6}|[A-Fa-f0-9]{20})", message = "Codigo deve ser um TOTP de 6 digitos ou backup code valido.")
		String code
) {
}
