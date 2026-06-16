package com.example.backend.service.auth;

import com.example.backend.domain.TotpBackupCode;
import com.example.backend.domain.User;
import com.example.backend.event.TotpLockedEvent;
import com.example.backend.exception.InvalidTotpException;
import com.example.backend.exception.TotpLockedException;
import com.example.backend.repository.TotpBackupCodeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.AccessToken;
import com.example.backend.security.ClientContext;
import com.example.backend.security.JwtService;
import com.example.backend.security.RefreshTokenPair;
import com.example.backend.security.RefreshTokenResult;
import com.example.backend.security.TotpEncryptionService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TotpVerifyService {

	private static final int MAX_TOTP_FAILURES = 5;
	private static final Duration TOTP_LOCK_DURATION = Duration.ofMinutes(15);

	private final UserRepository userRepository;
	private final TotpBackupCodeRepository backupCodeRepository;
	private final TotpEncryptionService totpEncryptionService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final Clock clock;

	@Autowired
	public TotpVerifyService(
			UserRepository userRepository,
			TotpBackupCodeRepository backupCodeRepository,
			TotpEncryptionService totpEncryptionService,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			PasswordEncoder passwordEncoder,
			ApplicationEventPublisher applicationEventPublisher
	) {
		this(userRepository, backupCodeRepository, totpEncryptionService,
				jwtService, refreshTokenService, passwordEncoder,
				applicationEventPublisher, Clock.systemUTC());
	}

	TotpVerifyService(
			UserRepository userRepository,
			TotpBackupCodeRepository backupCodeRepository,
			TotpEncryptionService totpEncryptionService,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			PasswordEncoder passwordEncoder,
			ApplicationEventPublisher applicationEventPublisher,
			Clock clock
	) {
		this.userRepository = userRepository;
		this.backupCodeRepository = backupCodeRepository;
		this.totpEncryptionService = totpEncryptionService;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.passwordEncoder = passwordEncoder;
		this.applicationEventPublisher = applicationEventPublisher;
		this.clock = clock;
	}

	@Transactional
	public RefreshTokenResult verify(UUID userId, String code, ClientContext clientContext) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));

		Instant now = Instant.now(clock);

		if (user.isTotpLockedAt(now)) {
			throw new TotpLockedException();
		}

		String normalizedCode = normalizeCode(code);
		String secret = totpEncryptionService.decrypt(user.getTotpSecretEncrypted());
		boolean valid = isValidTotp(secret, normalizedCode) || isValidBackupCode(user, normalizedCode, now);

		if (!valid) {
			handleFailure(user, now);
			throw new InvalidTotpException();
		}

		user.clearTotpFailures(now);
		userRepository.save(user);

		UUID sessionId = UUID.randomUUID();
		AccessToken accessToken = jwtService.issueAccessToken(user, clientContext, sessionId);
		RefreshTokenPair refreshToken = refreshTokenService.issueForLogin(user, clientContext, sessionId);

		return new RefreshTokenResult(accessToken, refreshToken.rawToken(), sessionId);
	}

	private boolean isValidTotp(String secret, String code) {
		try {
			DefaultCodeVerifier verifier = new DefaultCodeVerifier(
					new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6),
					new SystemTimeProvider()
			);
			verifier.setAllowedTimePeriodDiscrepancy(1);
			return verifier.isValidCode(secret, code);
		} catch (Exception exception) {
			return false;
		}
	}

	private boolean isValidBackupCode(User user, String code, Instant now) {
		List<TotpBackupCode> backupCodes = backupCodeRepository.findAllByUserId(user.getId());
		for (TotpBackupCode backupCode : backupCodes) {
			if (!backupCode.isUsed() && passwordEncoder.matches(code, backupCode.getCodeHash())) {
				backupCode.markUsed(now);
				backupCodeRepository.save(backupCode);
				return true;
			}
		}
		return false;
	}

	private void handleFailure(User user, Instant now) {
		user.recordTotpFailure(now);
		if (user.getTotpFailedAttempts() >= MAX_TOTP_FAILURES) {
			user.lockTotpUntil(now.plus(TOTP_LOCK_DURATION), now);
			applicationEventPublisher.publishEvent(
					new TotpLockedEvent(user.getId(), user.getEmail(), user.getTotpLockedUntil())
			);
		}
		userRepository.save(user);
	}

	private String normalizeCode(String code) {
		return code == null ? "" : code.replace(" ", "").replace("-", "").trim().toUpperCase();
	}
}