package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {

	public BusinessException(String message) {
		super(ApiErrorCode.VAL_003, HttpStatus.CONFLICT, message);
	}

	public BusinessException(ApiErrorCode code, HttpStatus status, String message) {
		super(code, status, message);
	}
}
