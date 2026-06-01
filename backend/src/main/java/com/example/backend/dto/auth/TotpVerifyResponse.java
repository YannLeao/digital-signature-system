package com.example.backend.dto.auth;

public record TotpVerifyResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}