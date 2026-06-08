package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_keys")
public class UserKey {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
    private User user;

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "encrypted_private_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedPrivateKey;

    @Column(name = "key_algorithm", nullable = false, length = 20)
    private String keyAlgorithm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserKey() {}

    private UserKey(UUID id, User user, String publicKey,
                    String encryptedPrivateKey, String keyAlgorithm, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.keyAlgorithm = keyAlgorithm;
        this.createdAt = createdAt;
    }

    public static UserKey create(User user, String publicKey,
                                  String encryptedPrivateKey, String keyAlgorithm, Instant now) {
        return new UserKey(UUID.randomUUID(), user, publicKey, encryptedPrivateKey, keyAlgorithm, now);
    }

    public UUID getId()                   { return id; }
    public User getUser()                 { return user; }
    public String getPublicKey()          { return publicKey; }
    public String getEncryptedPrivateKey(){ return encryptedPrivateKey; }
    public String getKeyAlgorithm()       { return keyAlgorithm; }
    public Instant getCreatedAt()         { return createdAt; }
}