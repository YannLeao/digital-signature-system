package com.example.backend.service.auth;

import com.example.backend.domain.User;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.exception.AuthenticationFailedException;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserLoginServiceTests {

	private static final Instant NOW = Instant.parse("2026-05-19T12:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(16, 32, 4, 65536, 3);
	private final UserLoginService service = new UserLoginService(
			userRepository,
			passwordEncoder,
			CLOCK,
			passwordEncoder.encode("DummyPassword123!")
	);

	@Test
	void logsInValidUserAndClearsFailureState() {
		User user = user();
		user.recordFailedLogin(NOW.minusSeconds(120));
		user.lockUntil(NOW.minusSeconds(60), NOW.minusSeconds(120));
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

		service.login(new LoginRequest(" User@Example.COM ", "StrongPassword123!"));

		assertThat(user.getFailedAttempts()).isZero();
		assertThat(user.getLockedUntil()).isNull();
		assertThat(user.getUpdatedAt()).isEqualTo(NOW);
	}

	@Test
	void returnsGenericErrorForUnknownUser() {
		when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "StrongPassword123!")))
				.isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("Credenciais invalidas.");
	}

	@Test
	void returnsGenericErrorForWrongPasswordAndIncrementsFailures() {
		User user = user();
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "WrongPassword123!")))
				.isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("Credenciais invalidas.");

		assertThat(user.getFailedAttempts()).isEqualTo(1);
		assertThat(user.getLockedUntil()).isNull();
	}

	@Test
	void blocksUserForFifteenMinutesAfterFiveConsecutiveFailures() {
		User user = user();
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

		for (int attempt = 0; attempt < 5; attempt++) {
			assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "WrongPassword123!")))
					.isInstanceOf(AuthenticationFailedException.class);
		}

		assertThat(user.getFailedAttempts()).isEqualTo(5);
		assertThat(user.getLockedUntil()).isEqualTo(NOW.plusSeconds(900));
	}

	@Test
	void lockedUserCannotLoginAndReceivesGenericError() {
		User user = user();
		user.lockUntil(NOW.plusSeconds(300), NOW.minusSeconds(60));
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "StrongPassword123!")))
				.isInstanceOf(AuthenticationFailedException.class)
				.hasMessage("Credenciais invalidas.");

		assertThat(user.getLockedUntil()).isEqualTo(NOW.plusSeconds(300));
	}

	private User user() {
		return User.register(UUID.randomUUID(), "user@example.com", passwordEncoder.encode("StrongPassword123!"), NOW.minusSeconds(3600));
	}
}
