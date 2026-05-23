package com.example.backend.security;

public record RefreshTokenResult(AccessToken accessToken, String refreshToken) {
}
