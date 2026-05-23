package com.example.backend.dto.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
