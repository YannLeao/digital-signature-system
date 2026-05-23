package com.example.backend.security;

import com.example.backend.domain.JwtDenylistEntry;
import com.example.backend.domain.JwtDenylistReason;
import com.example.backend.repository.JwtDenylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtDenylistService {

	private final JwtDenylistRepository jwtDenylistRepository;
	private final Clock clock;

	@Autowired
	public JwtDenylistService(JwtDenylistRepository jwtDenylistRepository) {
		this(jwtDenylistRepository, Clock.systemUTC());
	}

	JwtDenylistService(JwtDenylistRepository jwtDenylistRepository, Clock clock) {
		this.jwtDenylistRepository = jwtDenylistRepository;
		this.clock = clock;
	}

	@Transactional
	public void denylist(Jwt jwt, JwtDenylistReason reason) {
		if (jwtDenylistRepository.existsByJti(jwt.getId())) {
			return;
		}

		jwtDenylistRepository.save(JwtDenylistEntry.revoke(
				UUID.randomUUID(),
				jwt.getId(),
				parseUuid(jwt.getSubject()),
				sessionId(jwt),
				jwt.getExpiresAt(),
				Instant.now(clock),
				reason
		));
	}

	@Transactional(readOnly = true)
	public boolean isDenylisted(String jti) {
		// TODO: remover entradas expiradas da denylist periodicamente.
		return jwtDenylistRepository.existsByJti(jti);
	}

	public UUID sessionId(Jwt jwt) {
		return parseUuid(jwt.getClaimAsString("session_id"));
	}

	private UUID parseUuid(String value) {
		return UUID.fromString(value);
	}
}
