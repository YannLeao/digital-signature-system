package com.example.backend.service.auth;

import com.example.backend.domain.User;
import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserRegistrationService {

	private static final String ARGON2ID_PREFIX = "$argon2id$";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	@Autowired
	public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this(userRepository, passwordEncoder, Clock.systemUTC());
	}

	UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	@Transactional
	public void register(RegisterUserRequest request) {
		String normalizedEmail = normalizeEmail(request.email());

		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
			throw new BusinessException("E-mail ja cadastrado.");
		}

		String passwordHash = passwordEncoder.encode(request.password());

		if (!passwordHash.startsWith(ARGON2ID_PREFIX)) {
			throw new IllegalStateException("Configured password encoder did not produce an Argon2id hash.");
		}

		User user = User.register(UUID.randomUUID(), normalizedEmail, passwordHash, clock.instant());

		try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException exception) {
			throw new BusinessException("E-mail ja cadastrado.");
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
