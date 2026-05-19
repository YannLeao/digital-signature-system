package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends ApiException {

	public AuthenticationFailedException() {
		super(ApiErrorCode.AUTH_001, HttpStatus.UNAUTHORIZED);
	}
}
