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
@Table(name = "active_sessions")
public class ActiveSession {

    @Id
    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_info", columnDefinition = "text")
    private String deviceInfo;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    protected ActiveSession() {
    }

    public static ActiveSession create(UUID sessionId, User user, String ip, String userAgent, Instant now) {
        ActiveSession session = new ActiveSession();
        session.sessionId = sessionId;
        session.user = user;
        session.deviceInfo = parseDeviceInfo(userAgent);
        session.ip = ip;
        session.userAgent = userAgent;
        session.createdAt = now;
        session.lastSeenAt = now;
        session.isActive = true;
        return session;
    }

    public void deactivate(Instant now) {
        this.isActive = false;
        this.lastSeenAt = now;
    }

    public void touch(Instant now) {
        this.lastSeenAt = now;
    }

    private static String parseDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Dispositivo desconhecido";
        }
        if (userAgent.contains("Mobile") || userAgent.contains("Android")) {
            return "Mobile";
        }
        if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            return "Tablet";
        }
        return "Desktop";
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public boolean isActive() {
        return isActive;
    }
}