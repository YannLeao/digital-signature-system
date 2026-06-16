package com.example.backend.event;

import java.time.Instant;
import java.util.UUID;

public record AccountLockedEvent(
        UUID userId,
        String email,
        Instant lockedUntil
) {
}