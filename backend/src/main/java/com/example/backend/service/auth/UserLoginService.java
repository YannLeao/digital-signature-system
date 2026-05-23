package com.example.backend.service.auth;

import com.example.backend.domain.User;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.exception.AuthenticationFailedException;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserLoginService {

	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;
	private final String dummyPasswordHash;

	@Autowired
	public UserLoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this(userRepository, passwordEncoder, Clock.systemUTC(), passwordEncoder.encode("DummyPassword123!"));
	}

	UserLoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock, String dummyPasswordHash) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
		this.dummyPasswordHash = dummyPasswordHash;
	}

	@Transactional
	public User login(LoginRequest request) {
		String normalizedEmail = normalizeEmail(request.email());
		Instant now = Instant.now(clock);
		Optional<User> userCandidate = userRepository.findByEmailIgnoreCase(normalizedEmail);

		if (userCandidate.isEmpty()) {
			passwordEncoder.matches(request.password(), dummyPasswordHash);
			throw new AuthenticationFailedException();
		}

		User user = userCandidate.get();

		if (user.isLockedAt(now)) {
			throw new AuthenticationFailedException();
		}

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			registerFailure(user, now);
			throw new AuthenticationFailedException();
		}

		user.clearLoginFailures(now);
		return user;
	}

	private void registerFailure(User user, Instant now) {
		user.recordFailedLogin(now);

		if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
			user.lockUntil(now.plus(LOCK_DURATION), now);
			// TODO: Integrar notificacao de bloqueio quando o servico de e-mail existir.
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
