package com.example.backend.dto;

public record FieldErrorResponse(
		String field,
		String message
) {
}
