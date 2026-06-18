package com.example.backend.controller.session;

import com.example.backend.dto.session.SessionResponse;
import com.example.backend.security.JwtDenylistService;
import com.example.backend.service.session.ActiveSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionControllerTests {

    private final ActiveSessionService activeSessionService = mock(ActiveSessionService.class);
    private final JwtDenylistService jwtDenylistService = mock(JwtDenylistService.class);
    private final SessionController controller = new SessionController(activeSessionService, jwtDenylistService);

    @Test
    void controllerMappingUsesServletPrefixOnlyOnce() {
        RequestMapping mapping = SessionController.class.getAnnotation(RequestMapping.class);

        assertThat(mapping.value()).containsExactly("/sessions");
    }

    @Test
    void listsActiveSessionsForAuthenticatedUser() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        SessionResponse session = new SessionResponse(UUID.randomUUID(), "Desktop", "203.0.113.10", "JUnit/5", Instant.now(), Instant.now());
        when(activeSessionService.listActive(userId)).thenReturn(List.of(session));

        var response = controller.listSessions(jwt(userId));

        assertThat(response.getBody()).containsExactly(session);
        verify(activeSessionService).listActive(userId);
    }

    @Test
    void revokesCurrentSessionAndDenylistsCurrentAccessToken() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Jwt jwt = jwt(userId);
        HttpServletRequest request = request();
        when(jwtDenylistService.sessionId(jwt)).thenReturn(sessionId);

        controller.revokeSession(jwt, sessionId, request);

        verify(activeSessionService).revokeSession(org.mockito.Mockito.eq(userId), org.mockito.Mockito.eq(sessionId), org.mockito.Mockito.any());
        verify(jwtDenylistService).denylist(jwt, com.example.backend.domain.JwtDenylistReason.LOGOUT);
    }

    @Test
    void revokeAllSessionsDenylistsCurrentAccessToken() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Jwt jwt = jwt(userId);
        HttpServletRequest request = request();
        when(jwtDenylistService.sessionId(jwt)).thenReturn(sessionId);

        controller.revokeAllSessions(jwt, request);

        verify(activeSessionService).revokeAllSessions(org.mockito.Mockito.eq(userId), org.mockito.Mockito.eq(sessionId), org.mockito.Mockito.any());
        verify(jwtDenylistService).denylist(jwt, com.example.backend.domain.JwtDenylistReason.LOGOUT);
    }

    private Jwt jwt(UUID userId) {
        Instant issuedAt = Instant.parse("2026-06-18T12:00:00Z");
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .claim("jti", "22222222-2222-2222-2222-222222222222")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(900))
                .claim("session_id", "33333333-3333-3333-3333-333333333333")
                .build();
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("203.0.113.10");
        when(request.getHeader("User-Agent")).thenReturn("JUnit/5");
        return request;
    }
}
