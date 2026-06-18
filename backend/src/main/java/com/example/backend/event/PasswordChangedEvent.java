package com.example.backend.event;

import java.time.Instant;
import java.util.UUID;

public record PasswordChangedEvent(
        UUID userId,
        String email,
        String ip,
        Instant changedAt
) {
}
