package com.example.backend.security;

import com.example.backend.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class JwtClaimsFactory {

    static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    static final Duration HALF_SESSION_TTL = Duration.ofMinutes(5);

    private final Clock clock;
    private final String issuer;

    @Autowired
    public JwtClaimsFactory(@Value("${JWT_ISSUER}") String issuer) {
        this(Clock.systemUTC(), issuer);
    }

    JwtClaimsFactory(Clock clock, String issuer) {
        this.clock = clock;
        this.issuer = issuer;
    }

    public JwtClaimsSet createAccessTokenClaims(User user, ClientContext clientContext) {
        return createAccessTokenClaims(user, clientContext, UUID.randomUUID());
    }

    public JwtClaimsSet createAccessTokenClaims(User user, ClientContext clientContext, UUID sessionId) {
        Instant issuedAt = Instant.now(clock);
        return JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ACCESS_TOKEN_TTL))
                .claim("session_id", sessionId.toString())
                .claim("ip", sha256(clientContext.ipAddress()))
                .claim("ua_hash", sha256(clientContext.userAgent()))
                .build();
    }

    public JwtClaimsSet createHalfSessionClaims(User user, ClientContext clientContext) {
        Instant issuedAt = Instant.now(clock);
        return JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(HALF_SESSION_TTL))
                .claim("scope", "2fa:verify")
                .claim("ip", sha256(clientContext.ipAddress()))
                .claim("ua_hash", sha256(clientContext.userAgent()))
                .build();
    }

    public long accessTokenExpiresInSeconds() {
        return ACCESS_TOKEN_TTL.toSeconds();
    }

    public long halfSessionExpiresInSeconds() {
        return HALF_SESSION_TTL.toSeconds();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalize(value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash JWT client context.", exception);
        }
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim();
    }
}