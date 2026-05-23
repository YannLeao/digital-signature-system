package com.example.backend.security;

import com.example.backend.domain.RefreshToken;

public record RefreshTokenPair(String rawToken, RefreshToken storedToken) {
}
