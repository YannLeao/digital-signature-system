package com.example.backend.event;

import java.time.Instant;
import java.util.UUID;

public record NewLoginEvent(
        UUID userId,
        String email,
        String ip,
        Instant loginAt
) {
}