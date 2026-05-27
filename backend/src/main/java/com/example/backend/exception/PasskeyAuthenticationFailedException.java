package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class PasskeyAuthenticationFailedException extends ApiException {

	public PasskeyAuthenticationFailedException() {
		super(ApiErrorCode.AUTH_003, HttpStatus.UNAUTHORIZED, "Sessao invalida ou expirada.");
	}
}
