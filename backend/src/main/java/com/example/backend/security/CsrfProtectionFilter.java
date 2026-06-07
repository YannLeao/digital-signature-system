package com.example.backend.security;

import com.example.backend.dto.ApiErrorResponse;
import com.example.backend.exception.ApiErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

@Component
public class CsrfProtectionFilter extends OncePerRequestFilter {

	private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
	private static final Set<String> EXEMPT_PATHS = Set.of(
			"/auth/register",
			"/api/v1/auth/register",
			"/auth/login",
			"/api/v1/auth/login",
			"/auth/csrf",
			"/api/v1/auth/csrf",
			"/auth/passkey/register/start",
			"/api/v1/auth/passkey/register/start",
			"/auth/passkey/register/finish",
			"/api/v1/auth/passkey/register/finish",
			"/auth/passkey/auth/start",
			"/api/v1/auth/passkey/auth/start",
			"/auth/passkey/auth/finish",
			"/api/v1/auth/passkey/auth/finish"
	);

	private final CsrfTokenService csrfTokenService;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	@Autowired
	public CsrfProtectionFilter(CsrfTokenService csrfTokenService, ObjectMapper objectMapper) {
		this(csrfTokenService, objectMapper, Clock.systemUTC());
	}

	CsrfProtectionFilter(CsrfTokenService csrfTokenService, ObjectMapper objectMapper, Clock clock) {
		this.csrfTokenService = csrfTokenService;
		this.objectMapper = objectMapper;
		this.clock = clock;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (!requiresCsrf(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String cookieToken = csrfCookieValue(request);
		String headerToken = request.getHeader(csrfTokenService.headerName());

		if (!validTokens(cookieToken, headerToken)) {
			writeForbidden(response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean requiresCsrf(HttpServletRequest request) {
		return !SAFE_METHODS.contains(request.getMethod().toUpperCase())
				&& !EXEMPT_PATHS.contains(requestPath(request));
	}

	private String requestPath(HttpServletRequest request) {
		String path = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
			return path.substring(contextPath.length());
		}
		return path;
	}

	private String csrfCookieValue(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		return Arrays.stream(cookies)
				.filter(cookie -> csrfTokenService.cookieName().equals(cookie.getName()))
				.map(Cookie::getValue)
				.filter(value -> !value.isBlank())
				.findFirst()
				.orElse(null);
	}

	private boolean validTokens(String cookieToken, String headerToken) {
		if (cookieToken == null || headerToken == null || cookieToken.isBlank() || headerToken.isBlank()) {
			return false;
		}
		return MessageDigest.isEqual(
				cookieToken.getBytes(StandardCharsets.UTF_8),
				headerToken.getBytes(StandardCharsets.UTF_8)
		);
	}

	private void writeForbidden(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(
				ApiErrorCode.SEC_001.name(),
				ApiErrorCode.SEC_001.defaultMessage(),
				Instant.now(clock)
		));
	}
}
