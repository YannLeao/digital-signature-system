package com.example.backend.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AuthenticationFailedException extends ApiException {

	private final UUID userId;

	public AuthenticationFailedException() {
		this(null);
	}

	public AuthenticationFailedException(UUID userId) {
		super(ApiErrorCode.AUTH_001, HttpStatus.UNAUTHORIZED);
		this.userId = userId;
	}

	public UUID userId() {
		return userId;
	}
}
