package com.example.backend.event;

import java.time.Instant;
import java.util.UUID;

public record TwoFactorChangedEvent(
        UUID userId,
        String email,
        boolean enabled,
        String ip,
        Instant changedAt
) {
}