package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "totp_backup_codes")
public class TotpBackupCode {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "used_at")
    private Instant usedAt;

    protected TotpBackupCode() {}

    public static TotpBackupCode create(UUID id, User user, String codeHash) {
        TotpBackupCode code = new TotpBackupCode();
        code.id = id;
        code.user = user;
        code.codeHash = codeHash;
        return code;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getCodeHash() { return codeHash; }
    public Instant getUsedAt() { return usedAt; }
    public boolean isUsed() { return usedAt != null; }

    public void markUsed(Instant now) {
        this.usedAt = now;
    }
}