package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
		String code,
		String message,
		Instant timestamp,
		List<FieldErrorResponse> fields
) {
	public static ApiErrorResponse of(String code, String message, Instant timestamp) {
		return new ApiErrorResponse(code, message, timestamp, List.of());
	}

	public static ApiErrorResponse withFields(
			String code,
			String message,
			Instant timestamp,
			List<FieldErrorResponse> fields
	) {
		return new ApiErrorResponse(code, message, timestamp, fields);
	}
}
