package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "failed_attempts", nullable = false)
	private int failedAttempts;

	@Column(name = "locked_until")
	private Instant lockedUntil;

	@Column(name = "totp_secret_encrypted")
	private String totpSecretEncrypted;

	@Column(name = "totp_enabled", nullable = false)
	private boolean totpEnabled = false;

	@Column(name = "totp_failed_attempts", nullable = false)
	private int totpFailedAttempts = 0;

	@Column(name = "totp_locked_until")
	private Instant totpLockedUntil;

	public String getTotpSecretEncrypted() {
		return totpSecretEncrypted;
	}

	public boolean isTotpEnabled() {
		return totpEnabled;
	}

	public int getTotpFailedAttempts() {
		return totpFailedAttempts;
	}

	public Instant getTotpLockedUntil() {
		return totpLockedUntil;
	}

	public void enableTotp(String encryptedSecret, Instant now) {
		this.totpSecretEncrypted = encryptedSecret;
		this.totpEnabled = true;
		this.totpFailedAttempts = 0;
		this.totpLockedUntil = null;
		this.updatedAt = now;
	}

	public void startTotpSetup(String encryptedSecret, Instant now) {
		this.totpSecretEncrypted = encryptedSecret;
		this.totpEnabled = false;
		this.totpFailedAttempts = 0;
		this.totpLockedUntil = null;
		this.updatedAt = now;
	}

	public void confirmTotpSetup(Instant now) {
		this.totpEnabled = true;
		this.totpFailedAttempts = 0;
		this.totpLockedUntil = null;
		this.updatedAt = now;
	}

	public boolean isTotpLockedAt(Instant now) {
		return totpLockedUntil != null && totpLockedUntil.isAfter(now);
	}

	public void recordTotpFailure(Instant now) {
		totpFailedAttempts++;
		updatedAt = now;
	}

	public void lockTotpUntil(Instant lockedUntil, Instant now) {
		this.totpLockedUntil = lockedUntil;
		updatedAt = now;
	}

	public void clearTotpFailures(Instant now) {
		totpFailedAttempts = 0;
		totpLockedUntil = null;
		updatedAt = now;
	}

	protected User() {
	}

	private User(UUID id, String email, String passwordHash, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.failedAttempts = 0;
		this.lockedUntil = null;
	}

	public static User register(UUID id, String email, String passwordHash, Instant now) {
		return new User(id, email, passwordHash, now, now);
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public int getFailedAttempts() {
		return failedAttempts;
	}

	public Instant getLockedUntil() {
		return lockedUntil;
	}

	public boolean isLockedAt(Instant now) {
		return lockedUntil != null && lockedUntil.isAfter(now);
	}

	public void recordFailedLogin(Instant now) {
		failedAttempts++;
		updatedAt = now;
	}

	public void lockUntil(Instant lockedUntil, Instant now) {
		this.lockedUntil = lockedUntil;
		updatedAt = now;
	}

	public void clearLoginFailures(Instant now) {
		failedAttempts = 0;
		lockedUntil = null;
		updatedAt = now;
	}

	public void changePassword(String passwordHash, Instant now) {
		this.passwordHash = passwordHash;
		this.failedAttempts = 0;
		this.lockedUntil = null;
		this.updatedAt = now;
	}
}
