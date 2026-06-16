package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "passkeys")
public class Passkey {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "credential_id", nullable = false, unique = true, length = 512)
	private String credentialId;

	@Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
	private String publicKey;

	@Column(nullable = false)
	private Long counter;

	@Column(nullable = false, length = 36)
	private String aaguid;

	@Column(name = "device_name", length = 255)
	private String deviceName;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "last_used")
	private Instant lastUsed;

	@Column(nullable = false)
	private boolean active = true;

	protected Passkey() {
	}

	public static Passkey register(
			User user,
			String credentialId,
			String publicKey,
			long counter,
			String aaguid,
			String deviceName,
			Instant now
	) {
		Passkey passkey = new Passkey();
		passkey.user = user;
		passkey.credentialId = credentialId;
		passkey.publicKey = publicKey;
		passkey.counter = counter;
		passkey.aaguid = aaguid;
		passkey.deviceName = deviceName;
		passkey.createdAt = now;
		passkey.lastUsed = null;
		passkey.active = true;
		return passkey;
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public Long getCounter() {
		return counter;
	}

	public String getAaguid() {
		return aaguid;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastUsed() {
		return lastUsed;
	}

	public boolean isActive() {
		return active;
	}

	public void deactivate() {
		active = false;
	}
	public void updateCounter(long newCounter, Instant lastUsed) {
		this.counter = newCounter;
		this.lastUsed = lastUsed;
	}

	public void markUsed(Instant lastUsed) {
		this.lastUsed = lastUsed;
	}
}
