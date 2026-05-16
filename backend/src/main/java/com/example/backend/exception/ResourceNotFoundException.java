package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

	public ResourceNotFoundException() {
		super(ApiErrorCode.SYS_002, HttpStatus.NOT_FOUND);
	}

	public ResourceNotFoundException(String message) {
		super(ApiErrorCode.SYS_002, HttpStatus.NOT_FOUND, message);
	}
}
