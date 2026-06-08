package com.example.backend.exception;

public class PdfValidationException extends RuntimeException {

    private final String code;

    public PdfValidationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}