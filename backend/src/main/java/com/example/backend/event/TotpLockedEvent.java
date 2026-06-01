package com.example.backend.event;

import java.time.Instant;
import java.util.UUID;

public record TotpLockedEvent(
        UUID userId,
        String email,
        Instant lockedUntil
) {
}