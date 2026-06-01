package com.example.backend.exception;

public class TotpLockedException extends RuntimeException {
    public TotpLockedException(String message) {
        super(message);
    }
}