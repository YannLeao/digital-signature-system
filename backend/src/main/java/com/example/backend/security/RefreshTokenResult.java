package com.example.backend.security;

import java.util.UUID;

public record RefreshTokenResult(AccessToken accessToken, String refreshToken, UUID sessionId) {

    public RefreshTokenResult(AccessToken accessToken, String refreshToken) {
        this(accessToken, refreshToken, null);
    }
}