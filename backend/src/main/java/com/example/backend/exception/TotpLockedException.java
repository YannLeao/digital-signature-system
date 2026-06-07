package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class TotpLockedException extends ApiException {

	public TotpLockedException() {
		super(ApiErrorCode.AUTH_005, HttpStatus.TOO_MANY_REQUESTS);
	}
}
