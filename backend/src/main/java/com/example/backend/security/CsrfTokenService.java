package com.example.backend.security;

import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CsrfTokenService {

	private static final int TOKEN_BYTES = 32;

	private final CsrfCookieProperties properties;
	private final SecureRandom secureRandom;

	@Autowired
	public CsrfTokenService(CsrfCookieProperties properties) {
		this(properties, new SecureRandom());
	}

	CsrfTokenService(CsrfCookieProperties properties, SecureRandom secureRandom) {
		this.properties = properties;
		this.secureRandom = secureRandom;
	}

	public String generateToken() {
		byte[] bytes = new byte[TOKEN_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public ResponseCookie createCookie(String token) {
		return ResponseCookie.from(properties.cookieName(), token)
				.httpOnly(false)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path(properties.path())
				.build();
	}

	public ResponseCookie clearCookie() {
		return ResponseCookie.from(properties.cookieName(), "")
				.httpOnly(false)
				.secure(properties.secure())
				.sameSite(properties.sameSite())
				.path(properties.path())
				.maxAge(0)
				.build();
	}

	public String cookieName() {
		return properties.cookieName();
	}

	public String headerName() {
		return properties.headerName();
	}
}
