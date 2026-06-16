package com.example.backend.security;

import com.example.backend.dto.ApiErrorResponse;
import com.example.backend.exception.ApiErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtValidator jwtValidator;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	@Autowired
	public JwtAuthenticationFilter(JwtValidator jwtValidator, ObjectMapper objectMapper) {
		this(jwtValidator, objectMapper, Clock.systemUTC());
	}

	JwtAuthenticationFilter(JwtValidator jwtValidator, ObjectMapper objectMapper, Clock clock) {
		this.jwtValidator = jwtValidator;
		this.objectMapper = objectMapper;
		this.clock = clock;
	}

    @Override
	protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
	) throws ServletException, IOException {
		String token = bearerToken(request);
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			Jwt jwt = validateTokenForRequest(token, request);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					jwt,
					null,
					List.of()
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (JwtException exception) {
			SecurityContextHolder.clearContext();
			LOGGER.warn("JWT rejeitado em {} {}: {}", request.getMethod(), request.getServletPath(), exception.getMessage());
			writeUnauthorized(response);
		}
	}

	private Jwt validateTokenForRequest(String token, HttpServletRequest request) {
		if (isTotpVerifyRequest(request)) {
			return jwtValidator.validateTotpChallengeToken(token);
		}
		return jwtValidator.validateAccessToken(token);
	}

	private boolean isTotpVerifyRequest(HttpServletRequest request) {
		String path = request.getRequestURI();
		return "POST".equalsIgnoreCase(request.getMethod())
				&& ("/auth/2fa/verify".equals(path) || "/api/v1/auth/2fa/verify".equals(path));
	}

	private String bearerToken(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}

		String token = authorization.substring(BEARER_PREFIX.length()).trim();
		if (token.isBlank()) {
			return null;
		}

		return token;
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(
				ApiErrorCode.AUTH_003.name(),
				"Sessao invalida ou expirada.",
				Instant.now(clock)
		));
	}
}
