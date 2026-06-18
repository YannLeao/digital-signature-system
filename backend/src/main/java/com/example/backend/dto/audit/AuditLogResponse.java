package com.example.backend.dto.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        Instant timestampUtc,
        String ip,
        String userAgent,
        String action,
        String result,
        String metadata
) {
}
