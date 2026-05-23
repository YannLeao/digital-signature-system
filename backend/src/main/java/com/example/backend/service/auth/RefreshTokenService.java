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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

	private static final int REFRESH_TOKEN_BYTES = 32;
	private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtService jwtService;
	private final TokenHashing tokenHashing;
	private final SecureRandom secureRandom;
	private final Clock clock;

	@Autowired
	public RefreshTokenService(
			RefreshTokenRepository refreshTokenRepository,
			JwtService jwtService,
			TokenHashing tokenHashing
	) {
		this(refreshTokenRepository, jwtService, tokenHashing, new SecureRandom(), Clock.systemUTC());
	}

	RefreshTokenService(
			RefreshTokenRepository refreshTokenRepository,
			JwtService jwtService,
			TokenHashing tokenHashing,
			SecureRandom secureRandom,
			Clock clock
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtService = jwtService;
		this.tokenHashing = tokenHashing;
		this.secureRandom = secureRandom;
		this.clock = clock;
	}

	@Transactional
	public RefreshTokenPair issueForLogin(User user, ClientContext clientContext, UUID sessionId) {
		return issue(user, clientContext, UUID.randomUUID(), sessionId);
	}

	@Transactional
	public RefreshTokenResult rotate(String rawToken, ClientContext clientContext) {
		RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHashing.sha256(rawToken))
				.orElseThrow(InvalidRefreshTokenException::new);
		Instant now = Instant.now(clock);

		if (currentToken.isRevoked()) {
			revokeFamily(currentToken.getFamilyId(), now);
			throw new InvalidRefreshTokenException();
		}

		if (currentToken.isExpiredAt(now)) {
			currentToken.revoke(now);
			refreshTokenRepository.save(currentToken);
			throw new InvalidRefreshTokenException();
		}

		RefreshTokenPair replacement = issue(
				currentToken.getUser(),
				clientContext,
				currentToken.getFamilyId(),
				currentToken.getSessionId()
		);
		currentToken.replaceWith(replacement.storedToken(), now);
		refreshTokenRepository.save(currentToken);

		AccessToken accessToken = jwtService.issueAccessToken(currentToken.getUser(), clientContext, currentToken.getSessionId());
		return new RefreshTokenResult(accessToken, replacement.rawToken());
	}

	private RefreshTokenPair issue(User user, ClientContext clientContext, UUID familyId, UUID sessionId) {
		String rawToken = generateOpaqueToken();
		Instant issuedAt = Instant.now(clock);
		RefreshToken refreshToken = RefreshToken.issue(
				UUID.randomUUID(),
				user,
				tokenHashing.sha256(rawToken),
				familyId,
				sessionId,
				issuedAt,
				issuedAt.plus(REFRESH_TOKEN_TTL),
				tokenHashing.sha256(clientContext.ipAddress()),
				tokenHashing.sha256(clientContext.userAgent())
		);

		return new RefreshTokenPair(rawToken, refreshTokenRepository.save(refreshToken));
	}

	private void revokeFamily(UUID familyId, Instant now) {
		for (RefreshToken token : refreshTokenRepository.findByFamilyId(familyId)) {
			if (!token.isRevoked()) {
				token.revoke(now);
			}
		}
	}

	private String generateOpaqueToken() {
		byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
