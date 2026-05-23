package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "family_id", nullable = false)
	private UUID familyId;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Column(name = "issued_at", nullable = false)
	private Instant issuedAt;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "replaced_by_token_id")
	private RefreshToken replacedByToken;

	@Column(name = "created_by_ip_hash", nullable = false, length = 64)
	private String createdByIpHash;

	@Column(name = "created_by_user_agent_hash", nullable = false, length = 64)
	private String createdByUserAgentHash;

	protected RefreshToken() {
	}

	public static RefreshToken issue(
			UUID id,
			User user,
			String tokenHash,
			UUID familyId,
			UUID sessionId,
			Instant issuedAt,
			Instant expiresAt,
			String createdByIpHash,
			String createdByUserAgentHash
	) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.id = id;
		refreshToken.user = user;
		refreshToken.tokenHash = tokenHash;
		refreshToken.familyId = familyId;
		refreshToken.sessionId = sessionId;
		refreshToken.issuedAt = issuedAt;
		refreshToken.expiresAt = expiresAt;
		refreshToken.createdByIpHash = createdByIpHash;
		refreshToken.createdByUserAgentHash = createdByUserAgentHash;
		return refreshToken;
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public UUID getFamilyId() {
		return familyId;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public RefreshToken getReplacedByToken() {
		return replacedByToken;
	}

	public String getCreatedByIpHash() {
		return createdByIpHash;
	}

	public String getCreatedByUserAgentHash() {
		return createdByUserAgentHash;
	}

	public boolean isExpiredAt(Instant now) {
		return !expiresAt.isAfter(now);
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

	public void revoke(Instant now) {
		revokedAt = now;
	}

	public void replaceWith(RefreshToken replacement, Instant now) {
		revokedAt = now;
		replacedByToken = replacement;
	}
}
