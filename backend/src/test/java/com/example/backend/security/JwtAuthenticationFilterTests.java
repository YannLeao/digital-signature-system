package com.example.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTests {

	private static final Instant NOW = Instant.parse("2026-05-23T12:00:00Z");

	private final JwtValidator jwtValidator = mock(JwtValidator.class);
	private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
			jwtValidator,
			new ObjectMapper().findAndRegisterModules(),
			Clock.fixed(NOW, ZoneOffset.UTC)
	);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void acceptsValidBearerToken() throws ServletException, IOException {
		Jwt jwt = jwt();
		when(jwtValidator.validate("valid-token")).thenReturn(jwt);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(jwt);
	}

	@Test
	void rejectsDenylistedToken() throws ServletException, IOException {
		when(jwtValidator.validate("revoked-token")).thenThrow(new BadJwtException("JWT has been revoked."));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer revoked-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentAsString()).contains("\"code\":\"AUTH_003\"");
		assertThat(response.getContentAsString()).contains("\"message\":\"Sessao invalida ou expirada.\"");
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void rejectsInvalidToken() throws ServletException, IOException {
		when(jwtValidator.validate("invalid-token")).thenThrow(new BadJwtException("Invalid token."));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentAsString()).contains("\"code\":\"AUTH_003\"");
	}

	private Jwt jwt() {
		return Jwt.withTokenValue("access-token")
				.header("alg", "RS256")
				.subject("11111111-1111-1111-1111-111111111111")
				.claim("jti", "22222222-2222-2222-2222-222222222222")
				.issuedAt(NOW)
				.expiresAt(NOW.plusSeconds(900))
				.claim("session_id", "33333333-3333-3333-3333-333333333333")
				.claim("ip", "ip-hash")
				.claim("ua_hash", "ua-hash")
				.build();
	}
}
