package com.example.backend.service.auth;

import com.example.backend.domain.TotpBackupCode;
import com.example.backend.domain.User;
import com.example.backend.event.TotpLockedEvent;
import com.example.backend.exception.InvalidTotpException;
import com.example.backend.repository.TotpBackupCodeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.AccessToken;
import com.example.backend.security.ClientContext;
import com.example.backend.security.JwtService;
import com.example.backend.security.RefreshTokenPair;
import com.example.backend.security.RefreshTokenResult;
import com.example.backend.security.TotpEncryptionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TotpVerifyServiceTests {

	private static final Instant NOW = Instant.parse("2026-05-22T12:00:00Z");
	private static final ClientContext CLIENT_CONTEXT = new ClientContext("203.0.113.10", "JUnit/5");

	@Test
	void backupCodeIssuesFinalJwtAndCannotBeUsedTwice() {
		UserRepository userRepository = mock(UserRepository.class);
		TotpBackupCodeRepository backupCodeRepository = mock(TotpBackupCodeRepository.class);
		TotpEncryptionService encryptionService = mock(TotpEncryptionService.class);
		JwtService jwtService = mock(JwtService.class);
		RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
		User user = user();
		TotpBackupCode backupCode = TotpBackupCode.create(UUID.randomUUID(), user, "backup-hash");
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(encryptionService.decrypt("encrypted-secret")).thenReturn("INVALIDSECRET");
		when(backupCodeRepository.findAllByUserId(user.getId())).thenReturn(List.of(backupCode));
		when(passwordEncoder.matches("ABCDEF1234567890ABCD", "backup-hash")).thenReturn(true);
		when(jwtService.issueAccessToken(eq(user), eq(CLIENT_CONTEXT), any()))
				.thenReturn(new AccessToken("final-jwt", "Bearer", 900));
		when(refreshTokenService.issueForLogin(eq(user), eq(CLIENT_CONTEXT), any()))
				.thenReturn(new RefreshTokenPair("refresh-token", null));
		TotpVerifyService service = service(
				userRepository,
				backupCodeRepository,
				encryptionService,
				jwtService,
				refreshTokenService,
				passwordEncoder,
				publisher
		);

		RefreshTokenResult result = service.verify(user.getId(), "ABCDEF1234567890ABCD", CLIENT_CONTEXT);

		assertThat(result.accessToken().token()).isEqualTo("final-jwt");
		assertThat(result.refreshToken()).isEqualTo("refresh-token");
		assertThat(backupCode.isUsed()).isTrue();

		assertThatThrownBy(() -> service.verify(user.getId(), "ABCDEF1234567890ABCD", CLIENT_CONTEXT))
				.isInstanceOf(InvalidTotpException.class);
		verify(jwtService, times(1)).issueAccessToken(eq(user), eq(CLIENT_CONTEXT), any());
	}

	@Test
	void repeatedInvalidCodesLockUserAndPublishEventWithoutIssuingJwt() {
		UserRepository userRepository = mock(UserRepository.class);
		TotpBackupCodeRepository backupCodeRepository = mock(TotpBackupCodeRepository.class);
		TotpEncryptionService encryptionService = mock(TotpEncryptionService.class);
		JwtService jwtService = mock(JwtService.class);
		RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
		User user = user();
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(encryptionService.decrypt("encrypted-secret")).thenReturn("INVALIDSECRET");
		when(backupCodeRepository.findAllByUserId(user.getId())).thenReturn(List.of());
		TotpVerifyService service = service(
				userRepository,
				backupCodeRepository,
				encryptionService,
				jwtService,
				refreshTokenService,
				passwordEncoder,
				publisher
		);

		for (int attempt = 0; attempt < 5; attempt++) {
			assertThatThrownBy(() -> service.verify(user.getId(), "000000", CLIENT_CONTEXT))
					.isInstanceOf(InvalidTotpException.class);
		}

		assertThat(user.getTotpFailedAttempts()).isEqualTo(5);
		assertThat(user.getTotpLockedUntil()).isEqualTo(NOW.plusSeconds(900));
		ArgumentCaptor<TotpLockedEvent> eventCaptor = ArgumentCaptor.forClass(TotpLockedEvent.class);
		verify(publisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().userId()).isEqualTo(user.getId());
		verify(jwtService, never()).issueAccessToken(any(), any(), any());
		verify(refreshTokenService, never()).issueForLogin(any(), any(), any());
	}

	private TotpVerifyService service(
			UserRepository userRepository,
			TotpBackupCodeRepository backupCodeRepository,
			TotpEncryptionService encryptionService,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			PasswordEncoder passwordEncoder,
			ApplicationEventPublisher publisher
	) {
		return new TotpVerifyService(
				userRepository,
				backupCodeRepository,
				encryptionService,
				jwtService,
				refreshTokenService,
				passwordEncoder,
				publisher,
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	private User user() {
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				NOW.minusSeconds(3600)
		);
		user.enableTotp("encrypted-secret", NOW.minusSeconds(60));
		return user;
	}
}
