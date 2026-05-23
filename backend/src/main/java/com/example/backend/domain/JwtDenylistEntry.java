package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jwt_denylist")
public class JwtDenylistEntry {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String jti;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Column(name = "token_expires_at", nullable = false)
	private Instant tokenExpiresAt;

	@Column(name = "revoked_at", nullable = false)
	private Instant revokedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private JwtDenylistReason reason;

	protected JwtDenylistEntry() {
	}

	public static JwtDenylistEntry revoke(
			UUID id,
			String jti,
			UUID userId,
			UUID sessionId,
			Instant tokenExpiresAt,
			Instant revokedAt,
			JwtDenylistReason reason
	) {
		JwtDenylistEntry entry = new JwtDenylistEntry();
		entry.id = id;
		entry.jti = jti;
		entry.userId = userId;
		entry.sessionId = sessionId;
		entry.tokenExpiresAt = tokenExpiresAt;
		entry.revokedAt = revokedAt;
		entry.reason = reason;
		return entry;
	}

	public UUID getId() {
		return id;
	}

	public String getJti() {
		return jti;
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public Instant getTokenExpiresAt() {
		return tokenExpiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public JwtDenylistReason getReason() {
		return reason;
	}
}
