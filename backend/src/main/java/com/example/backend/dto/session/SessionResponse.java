package com.example.backend.dto.session;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        String deviceInfo,
        String ip,
        String userAgent,
        Instant createdAt,
        Instant lastSeenAt
) {
}