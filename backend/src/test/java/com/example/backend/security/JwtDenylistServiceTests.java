package com.example.backend.security;

import com.example.backend.domain.JwtDenylistEntry;
import com.example.backend.domain.JwtDenylistReason;
import com.example.backend.repository.JwtDenylistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtDenylistServiceTests {

	private static final Instant NOW = Instant.parse("2026-05-23T12:00:00Z");

	private final JwtDenylistRepository jwtDenylistRepository = mock(JwtDenylistRepository.class);
	private final JwtDenylistService jwtDenylistService = new JwtDenylistService(
			jwtDenylistRepository,
			Clock.fixed(NOW, ZoneOffset.UTC)
	);

	@Test
	void addsJtiToDenylist() {
		Jwt jwt = jwt();

		jwtDenylistService.denylist(jwt, JwtDenylistReason.LOGOUT);

		verify(jwtDenylistRepository).save(org.mockito.ArgumentMatchers.argThat(entry ->
				entry.getJti().equals("22222222-2222-2222-2222-222222222222")
						&& entry.getUserId().toString().equals("11111111-1111-1111-1111-111111111111")
						&& entry.getSessionId().toString().equals("33333333-3333-3333-3333-333333333333")
						&& entry.getTokenExpiresAt().equals(NOW.plusSeconds(900))
						&& entry.getRevokedAt().equals(NOW)
						&& entry.getReason() == JwtDenylistReason.LOGOUT
		));
	}

	@Test
	void doesNotSaveDuplicateJti() {
		when(jwtDenylistRepository.existsByJti("22222222-2222-2222-2222-222222222222")).thenReturn(true);

		jwtDenylistService.denylist(jwt(), JwtDenylistReason.LOGOUT);

		verify(jwtDenylistRepository, never()).save(any(JwtDenylistEntry.class));
	}

	@Test
	void checksWhetherJtiIsDenylisted() {
		when(jwtDenylistRepository.existsByJti("jti")).thenReturn(true);

		assertThat(jwtDenylistService.isDenylisted("jti")).isTrue();
		assertThat(jwtDenylistService.isDenylisted("other")).isFalse();
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
