package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "timestamp_utc", nullable = false, updatable = false)
    private Instant timestampUtc;

    @Column(columnDefinition = "inet")
    private String ip;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 120)
    private AuditAction action;

    @Column(nullable = false, length = 60)
    private String result;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String metadata;

    protected AuditLog() {
    }

    public static AuditLog create(
            UUID id,
            User user,
            Instant timestampUtc,
            String ip,
            String userAgent,
            AuditAction action,
            String result,
            String metadata
    ) {
        AuditLog entry = new AuditLog();
        entry.id = id;
        entry.user = user;
        entry.timestampUtc = timestampUtc;
        entry.ip = ip;
        entry.userAgent = userAgent;
        entry.action = action;
        entry.result = result;
        entry.metadata = metadata;
        return entry;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Instant getTimestampUtc() {
        return timestampUtc;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getResult() {
        return result;
    }

    public String getMetadata() {
        return metadata;
    }
}