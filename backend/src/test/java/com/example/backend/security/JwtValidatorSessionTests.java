package com.example.backend.security;

import com.example.backend.repository.ActiveSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtValidatorSessionTests {

    private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    private final JwtDenylistService jwtDenylistService = mock(JwtDenylistService.class);
    private final ActiveSessionRepository activeSessionRepository = mock(ActiveSessionRepository.class);
    private final JwtValidator validator = new JwtValidator(jwtDecoder, jwtDenylistService, activeSessionRepository);

    @Test
    void acceptsAccessTokenOnlyWhenSessionIsActive() {
        Jwt jwt = accessJwt();
        when(jwtDecoder.decode("token")).thenReturn(jwt);
        when(jwtDenylistService.isDenylisted("22222222-2222-2222-2222-222222222222")).thenReturn(false);
        when(activeSessionRepository.existsBySessionIdAndIsActiveTrue(UUID.fromString("33333333-3333-3333-3333-333333333333")))
                .thenReturn(true);

        assertThat(validator.validateAccessToken("token")).isSameAs(jwt);
    }

    @Test
    void rejectsAccessTokenWhenSessionWasRevoked() {
        Jwt jwt = accessJwt();
        when(jwtDecoder.decode("token")).thenReturn(jwt);
        when(jwtDenylistService.isDenylisted("22222222-2222-2222-2222-222222222222")).thenReturn(false);
        when(activeSessionRepository.existsBySessionIdAndIsActiveTrue(UUID.fromString("33333333-3333-3333-3333-333333333333")))
                .thenReturn(false);

        assertThatThrownBy(() -> validator.validateAccessToken("token"))
                .isInstanceOf(BadJwtException.class)
                .hasMessage("JWT session is not active.");
    }

    private Jwt accessJwt() {
        Instant issuedAt = Instant.parse("2026-06-18T12:00:00Z");
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("11111111-1111-1111-1111-111111111111")
                .claim("jti", "22222222-2222-2222-2222-222222222222")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(900))
                .claim("session_id", "33333333-3333-3333-3333-333333333333")
                .claim("token_use", JwtClaimsFactory.TOKEN_USE_ACCESS)
                .claim("ip", "ip-hash")
                .claim("ua_hash", "ua-hash")
                .build();
    }
}
