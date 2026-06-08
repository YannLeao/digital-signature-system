package com.example.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsrfProtectionFilterTests {

	private static final Instant NOW = Instant.parse("2026-05-16T12:00:00Z");

	private final CsrfTokenService csrfTokenService = mock(CsrfTokenService.class);
	private final CsrfProtectionFilter filter = new CsrfProtectionFilter(
			csrfTokenService,
			new ObjectMapper().findAndRegisterModules(),
			Clock.fixed(NOW, ZoneOffset.UTC)
	);

	CsrfProtectionFilterTests() {
		when(csrfTokenService.cookieName()).thenReturn("XSRF-TOKEN");
		when(csrfTokenService.headerName()).thenReturn("X-CSRF-Token");
	}

	@Test
	void protectedPostWithMatchingCookieAndHeaderPasses() throws ServletException, IOException {
		MockHttpServletRequest request = request("POST", "/api/v1/auth/refresh");
		request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf-token"));
		request.addHeader("X-CSRF-Token", "csrf-token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	void protectedPostWithoutCookieIsForbidden() throws ServletException, IOException {
		MockHttpServletRequest request = request("POST", "/api/v1/auth/refresh");
		request.addHeader("X-CSRF-Token", "csrf-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentAsString()).contains("\"code\":\"SEC_001\"");
		assertThat(response.getContentAsString()).contains("Requisicao bloqueada por politica de seguranca.");
	}

	@Test
	void protectedPostWithoutHeaderIsForbidden() throws ServletException, IOException {
		MockHttpServletRequest request = request("POST", "/api/v1/auth/refresh");
		request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf-token"));
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(403);
	}

	@Test
	void protectedPostWithDifferentTokensIsForbidden() throws ServletException, IOException {
		MockHttpServletRequest request = request("POST", "/api/v1/auth/refresh");
		request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf-token"));
		request.addHeader("X-CSRF-Token", "other-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(403);
	}

	@Test
	void protectedPostWithBlankTokenIsForbidden() throws ServletException, IOException {
		MockHttpServletRequest request = request("POST", "/api/v1/auth/refresh");
		request.setCookies(new jakarta.servlet.http.Cookie("XSRF-TOKEN", ""));
		request.addHeader("X-CSRF-Token", "");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(403);
	}

	@Test
	void safeMethodsDoNotRequireCsrf() throws ServletException, IOException {
		MockHttpServletRequest get = request("GET", "/api/v1/auth/refresh");
		MockHttpServletResponse getResponse = new MockHttpServletResponse();
		filter.doFilter(get, getResponse, new MockFilterChain());

		MockHttpServletRequest options = request("OPTIONS", "/api/v1/auth/refresh");
		MockHttpServletResponse optionsResponse = new MockHttpServletResponse();
		filter.doFilter(options, optionsResponse, new MockFilterChain());

		assertThat(getResponse.getStatus()).isEqualTo(200);
		assertThat(optionsResponse.getStatus()).isEqualTo(200);
	}

	@Test
	void publicLoginAndRegisterDoNotRequireCsrf() throws ServletException, IOException {
		MockHttpServletResponse loginResponse = new MockHttpServletResponse();
		filter.doFilter(request("POST", "/api/v1/auth/login"), loginResponse, new MockFilterChain());

		MockHttpServletResponse registerResponse = new MockHttpServletResponse();
		filter.doFilter(request("POST", "/api/v1/auth/register"), registerResponse, new MockFilterChain());

		assertThat(loginResponse.getStatus()).isEqualTo(200);
		assertThat(registerResponse.getStatus()).isEqualTo(200);
	}

	private MockHttpServletRequest request(String method, String path) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, path);
		request.addHeader(HttpHeaders.ACCEPT, "application/json");
		return request;
	}
}
