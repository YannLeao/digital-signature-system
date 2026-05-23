package com.example.backend.service.auth;

import com.example.backend.domain.RefreshToken;
import com.example.backend.domain.User;
import com.example.backend.exception.InvalidRefreshTokenException;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.security.AccessToken;
import com.example.backend.security.ClientContext;
import com.example.backend.security.JwtService;
import com.example.backend.security.RefreshTokenPair;
import com.example.backend.security.RefreshTokenResult;
import com.example.backend.security.TokenHashing;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTests {

	private static final Instant NOW = Instant.parse("2026-05-23T12:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
	private static final ClientContext CLIENT_CONTEXT = new ClientContext("203.0.113.10", "JUnit/5");

	private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
	private final JwtService jwtService = mock(JwtService.class);
	private final TokenHashing tokenHashing = new TokenHashing();
	private final RefreshTokenService service = new RefreshTokenService(
			refreshTokenRepository,
			jwtService,
			tokenHashing,
			deterministicRandom(),
			CLOCK
	);

	@Test
	void issuesOpaqueRefreshTokenAndStoresOnlyHash() {
		when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		User user = user();

		RefreshTokenPair pair = service.issueForLogin(
				user,
				CLIENT_CONTEXT,
				UUID.fromString("22222222-2222-2222-2222-222222222222")
		);

		assertThat(pair.rawToken()).isNotBlank();
		assertThat(pair.rawToken()).doesNotContain(".");
		assertThat(pair.storedToken().getTokenHash()).isEqualTo(tokenHashing.sha256(pair.rawToken()));
		assertThat(pair.storedToken().getTokenHash()).isNotEqualTo(pair.rawToken());
		assertThat(pair.storedToken().getExpiresAt()).isEqualTo(NOW.plusSeconds(604800));
		assertThat(pair.storedToken().getSessionId()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
	}

	@Test
	void rejectsUnknownRefreshToken() {
		when(refreshTokenRepository.findByTokenHash(tokenHashing.sha256("missing-token"))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.rotate("missing-token", CLIENT_CONTEXT))
				.isInstanceOf(InvalidRefreshTokenException.class)
				.hasMessage("Sessao invalida ou expirada.");

		verify(jwtService, never()).issueAccessToken(any(), any(), any());
	}

	@Test
	void rejectsExpiredRefreshTokenAndRevokesIt() {
		RefreshToken expired = token(user(), "expired-token", NOW.minusSeconds(60));
		when(refreshTokenRepository.findByTokenHash(tokenHashing.sha256("expired-token"))).thenReturn(Optional.of(expired));

		assertThatThrownBy(() -> service.rotate("expired-token", CLIENT_CONTEXT))
				.isInstanceOf(InvalidRefreshTokenException.class);

		assertThat(expired.getRevokedAt()).isEqualTo(NOW);
		verify(refreshTokenRepository).save(expired);
		verify(jwtService, never()).issueAccessToken(any(), any(), any());
	}

	@Test
	void rotatesValidRefreshTokenAndRevokesPreviousToken() {
		User user = user();
		RefreshToken current = token(user, "current-token", NOW.plusSeconds(604800));
		List<RefreshToken> savedTokens = new ArrayList<>();
		when(refreshTokenRepository.findByTokenHash(tokenHashing.sha256("current-token"))).thenReturn(Optional.of(current));
		when(refreshTokenRepository.save(any())).thenAnswer(invocation -> {
			RefreshToken token = invocation.getArgument(0);
			savedTokens.add(token);
			return token;
		});
		when(jwtService.issueAccessToken(user, CLIENT_CONTEXT, current.getSessionId()))
				.thenReturn(new AccessToken("new-jwt", "Bearer", 900));

		RefreshTokenResult result = service.rotate("current-token", CLIENT_CONTEXT);

		assertThat(result.accessToken().token()).isEqualTo("new-jwt");
		assertThat(result.refreshToken()).isNotBlank().isNotEqualTo("current-token");
		assertThat(current.getRevokedAt()).isEqualTo(NOW);
		assertThat(current.getReplacedByToken()).isNotNull();
		assertThat(savedTokens).hasSize(2);
		assertThat(savedTokens.get(0).getFamilyId()).isEqualTo(current.getFamilyId());
		assertThat(savedTokens.get(0).getSessionId()).isEqualTo(current.getSessionId());
	}

	@Test
	void detectsReuseAndRevokesTokenFamily() {
		User user = user();
		RefreshToken reused = token(user, "reused-token", NOW.plusSeconds(604800));
		RefreshToken sibling = token(user, "sibling-token", NOW.plusSeconds(604800), reused.getFamilyId(), reused.getSessionId());
		reused.revoke(NOW.minusSeconds(60));
		when(refreshTokenRepository.findByTokenHash(tokenHashing.sha256("reused-token"))).thenReturn(Optional.of(reused));
		when(refreshTokenRepository.findByFamilyId(reused.getFamilyId())).thenReturn(List.of(reused, sibling));

		assertThatThrownBy(() -> service.rotate("reused-token", CLIENT_CONTEXT))
				.isInstanceOf(InvalidRefreshTokenException.class);

		assertThat(sibling.getRevokedAt()).isEqualTo(NOW);
		verify(jwtService, never()).issueAccessToken(any(), any(), any());
	}

	@Test
	void revokesAllRefreshTokensFromSessionOnLogout() {
		User user = user();
		UUID sessionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		RefreshToken current = token(user, "current-token", NOW.plusSeconds(604800), UUID.randomUUID(), sessionId);
		RefreshToken rotated = token(user, "rotated-token", NOW.plusSeconds(604800), UUID.randomUUID(), sessionId);
		rotated.revoke(NOW.minusSeconds(60));
		when(refreshTokenRepository.findBySessionId(sessionId)).thenReturn(List.of(current, rotated));

		service.revokeSession(sessionId);

		assertThat(current.getRevokedAt()).isEqualTo(NOW);
		assertThat(rotated.getRevokedAt()).isEqualTo(NOW.minusSeconds(60));
	}

	private RefreshToken token(User user, String rawToken, Instant expiresAt) {
		return token(user, rawToken, expiresAt, UUID.randomUUID(), UUID.randomUUID());
	}

	private RefreshToken token(User user, String rawToken, Instant expiresAt, UUID familyId, UUID sessionId) {
		return RefreshToken.issue(
				UUID.randomUUID(),
				user,
				tokenHashing.sha256(rawToken),
				familyId,
				sessionId,
				NOW.minusSeconds(60),
				expiresAt,
				tokenHashing.sha256(CLIENT_CONTEXT.ipAddress()),
				tokenHashing.sha256(CLIENT_CONTEXT.userAgent())
		);
	}

	private User user() {
		return User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				NOW.minusSeconds(3600)
		);
	}

	private SecureRandom deterministicRandom() {
		return new SecureRandom(new byte[] {1, 2, 3, 4});
	}
}
