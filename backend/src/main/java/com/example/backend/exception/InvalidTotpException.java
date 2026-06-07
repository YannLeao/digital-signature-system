package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidTotpException extends ApiException {

	public InvalidTotpException() {
		super(ApiErrorCode.AUTH_004, HttpStatus.UNAUTHORIZED);
	}
}
