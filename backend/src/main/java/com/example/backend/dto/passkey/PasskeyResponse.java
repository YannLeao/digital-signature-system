package com.example.backend.dto.passkey;

import java.time.Instant;

public record PasskeyResponse(
		Long id,
		String deviceName,
		Instant createdAt,
		Instant lastUsed,
		boolean active
) {
}
