package com.example.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsrfTokenServiceTests {

	@Test
	void generatesUrlSafeRandomTokenAndReadableCookie() {
		CsrfCookieProperties properties = mock(CsrfCookieProperties.class);
		when(properties.cookieName()).thenReturn("XSRF-TOKEN");
		when(properties.headerName()).thenReturn("X-CSRF-Token");
		when(properties.secure()).thenReturn(false);
		when(properties.sameSite()).thenReturn("Strict");
		when(properties.path()).thenReturn("/");
		CsrfTokenService service = new CsrfTokenService(properties, new SecureRandom());

		String token = service.generateToken();
		ResponseCookie cookie = service.createCookie(token);

		assertThat(token).matches("[A-Za-z0-9_-]{43}");
		assertThat(cookie.toString()).contains("XSRF-TOKEN=" + token);
		assertThat(cookie.toString()).contains("Path=/");
		assertThat(cookie.toString()).contains("SameSite=Strict");
		assertThat(cookie.toString()).doesNotContain("HttpOnly");
	}

	@Test
	void clearCookieExpiresReadableCookie() {
		CsrfCookieProperties properties = mock(CsrfCookieProperties.class);
		when(properties.cookieName()).thenReturn("XSRF-TOKEN");
		when(properties.secure()).thenReturn(false);
		when(properties.sameSite()).thenReturn("Strict");
		when(properties.path()).thenReturn("/");
		CsrfTokenService service = new CsrfTokenService(properties, new SecureRandom());

		assertThat(service.clearCookie().toString())
				.contains("XSRF-TOKEN=")
				.contains("Max-Age=0")
				.doesNotContain("HttpOnly");
	}
}
