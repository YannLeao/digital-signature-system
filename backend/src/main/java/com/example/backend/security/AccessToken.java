package com.example.backend.security;

public record AccessToken(String token, String tokenType, long expiresIn) {
}
