package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {

	public InvalidRefreshTokenException() {
		super(ApiErrorCode.AUTH_003, HttpStatus.UNAUTHORIZED, "Sessao invalida ou expirada.");
	}
}
