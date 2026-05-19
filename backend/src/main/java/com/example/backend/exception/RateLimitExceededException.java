package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ApiException {

	public RateLimitExceededException() {
		super(ApiErrorCode.SEC_001, HttpStatus.TOO_MANY_REQUESTS, "Muitas requisicoes.");
	}
}
