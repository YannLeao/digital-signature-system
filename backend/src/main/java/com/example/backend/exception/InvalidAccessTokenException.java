package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidAccessTokenException extends ApiException {

	public InvalidAccessTokenException() {
		super(ApiErrorCode.AUTH_003, HttpStatus.UNAUTHORIZED, "Sessao invalida ou expirada.");
	}
}
