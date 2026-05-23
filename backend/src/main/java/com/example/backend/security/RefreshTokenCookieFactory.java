package com.example.backend.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

	private final RefreshTokenCookieProperties properties;

	public RefreshTokenCookieFactory(RefreshTokenCookieProperties properties) {
		this.properties = properties;
	}

	public ResponseCookie create(String token) {
		return ResponseCookie.from(properties.name(), token)
				.httpOnly(true)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path(properties.path())
				.maxAge(properties.maxAge())
				.build();
	}

	public ResponseCookie clear() {
		return ResponseCookie.from(properties.name(), "")
				.httpOnly(true)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path(properties.path())
				.maxAge(0)
				.build();
	}

	public String cookieName() {
		return properties.name();
	}
}
