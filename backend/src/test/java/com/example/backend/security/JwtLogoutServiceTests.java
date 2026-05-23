package com.example.backend.security;

import com.example.backend.domain.JwtDenylistReason;
import com.example.backend.service.auth.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtLogoutServiceTests {

	private final JwtDenylistService jwtDenylistService = mock(JwtDenylistService.class);
	private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
	private final JwtLogoutService jwtLogoutService = new JwtLogoutService(jwtDenylistService, refreshTokenService);

	@Test
	void denylistsAccessTokenAndRevokesRefreshSession() {
		Jwt jwt = jwt();
		UUID sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		org.mockito.Mockito.when(jwtDenylistService.sessionId(jwt)).thenReturn(sessionId);

		jwtLogoutService.logout(jwt);

		verify(jwtDenylistService).denylist(jwt, JwtDenylistReason.LOGOUT);
		verify(refreshTokenService).revokeSession(sessionId);
	}

	private Jwt jwt() {
		Instant issuedAt = Instant.parse("2026-05-23T12:00:00Z");
		return Jwt.withTokenValue("access-token")
				.header("alg", "RS256")
				.subject("11111111-1111-1111-1111-111111111111")
				.claim("jti", "22222222-2222-2222-2222-222222222222")
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(900))
				.claim("session_id", "33333333-3333-3333-3333-333333333333")
				.claim("ip", "ip-hash")
				.claim("ua_hash", "ua-hash")
				.build();
	}
}
