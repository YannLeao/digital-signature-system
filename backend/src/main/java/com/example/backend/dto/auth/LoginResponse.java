package com.example.backend.dto.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, boolean requiresTwoFactor) {

	public LoginResponse(String accessToken, String tokenType, long expiresIn) {
		this(accessToken, tokenType, expiresIn, false);
	}

	public static LoginResponse requiresTwoFactor(String accessToken, String tokenType, long expiresIn) {
		return new LoginResponse(accessToken, tokenType, expiresIn, true);
	}
}
