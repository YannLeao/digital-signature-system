package com.example.backend.service.session;

import com.example.backend.domain.ActiveSession;
import com.example.backend.domain.AuditAction;
import com.example.backend.domain.User;
import com.example.backend.event.NewLoginEvent;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ActiveSessionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.ClientContext;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.auth.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActiveSessionServiceTests {

    private static final Instant NOW = Instant.parse("2026-06-18T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final ClientContext CLIENT = new ClientContext("203.0.113.10", "JUnit/5");

    private final ActiveSessionRepository activeSessionRepository = mock(ActiveSessionRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final AuditService auditService = mock(AuditService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final ActiveSessionService service = new ActiveSessionService(
            activeSessionRepository,
            userRepository,
            refreshTokenService,
            auditService,
            eventPublisher,
            CLOCK
    );

    @Test
    void registersSessionAndPublishesNewLoginOnlyForUnknownIp() {
        UUID sessionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(activeSessionRepository.existsByUserIdAndIp(user.getId(), CLIENT.ipAddress())).thenReturn(false);

        service.register(sessionId, user.getId(), CLIENT);

        ArgumentCaptor<ActiveSession> sessionCaptor = ArgumentCaptor.forClass(ActiveSession.class);
        verify(activeSessionRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getSessionId()).isEqualTo(sessionId);
        assertThat(sessionCaptor.getValue().getIp()).isEqualTo(CLIENT.ipAddress());
        verify(eventPublisher).publishEvent(org.mockito.ArgumentMatchers.any(NewLoginEvent.class));
    }

    @Test
    void doesNotPublishNewLoginWhenIpIsAlreadyKnown() {
        UUID sessionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        User user = user();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(activeSessionRepository.existsByUserIdAndIp(user.getId(), CLIENT.ipAddress())).thenReturn(true);

        service.register(sessionId, user.getId(), CLIENT);

        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any(NewLoginEvent.class));
    }

    @Test
    void revokesOnlyActiveSessionOwnedByUserAndAuditsLogout() {
        UUID sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        User user = user();
        ActiveSession session = ActiveSession.create(sessionId, user, CLIENT.ipAddress(), CLIENT.userAgent(), NOW.minusSeconds(60));
        when(activeSessionRepository.findBySessionIdAndUserIdAndIsActiveTrue(sessionId, user.getId()))
                .thenReturn(Optional.of(session));

        service.revokeSession(user.getId(), sessionId, CLIENT);

        assertThat(session.isActive()).isFalse();
        assertThat(session.getLastSeenAt()).isEqualTo(NOW);
        verify(refreshTokenService).revokeSession(sessionId);
        verify(auditService).logSuccess(user.getId(), AuditAction.LOGOUT, CLIENT.ipAddress(), CLIENT.userAgent());
    }

    @Test
    void rejectsRevocationWhenSessionIsNotActiveOrNotOwnedByUser() {
        UUID sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(activeSessionRepository.findBySessionIdAndUserIdAndIsActiveTrue(sessionId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revokeSession(userId, sessionId, CLIENT))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(refreshTokenService, never()).revokeSession(sessionId);
    }

    @Test
    void revokeAllSessionsDeactivatesEveryActiveSessionAndRevokesRefreshTokens() {
        User user = user();
        ActiveSession first = ActiveSession.create(UUID.randomUUID(), user, "203.0.113.10", "A", NOW.minusSeconds(60));
        ActiveSession second = ActiveSession.create(UUID.randomUUID(), user, "203.0.113.20", "B", NOW.minusSeconds(30));
        when(activeSessionRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(List.of(first, second));

        service.revokeAllSessions(user.getId(), first.getSessionId(), CLIENT);

        assertThat(first.isActive()).isFalse();
        assertThat(second.isActive()).isFalse();
        verify(refreshTokenService).revokeSession(first.getSessionId());
        verify(refreshTokenService).revokeSession(second.getSessionId());
        verify(activeSessionRepository).saveAll(List.of(first, second));
        verify(auditService).logSuccess(user.getId(), AuditAction.LOGOUT, CLIENT.ipAddress(), CLIENT.userAgent());
    }

    @Test
    void revokeOtherSessionsKeepsCurrentSessionActive() {
        User user = user();
        ActiveSession current = ActiveSession.create(UUID.randomUUID(), user, "203.0.113.10", "A", NOW.minusSeconds(60));
        ActiveSession other = ActiveSession.create(UUID.randomUUID(), user, "203.0.113.20", "B", NOW.minusSeconds(30));
        when(activeSessionRepository.findByUserIdAndIsActiveTrue(user.getId())).thenReturn(List.of(current, other));

        service.revokeOtherSessions(user.getId(), current.getSessionId(), CLIENT);

        assertThat(current.isActive()).isTrue();
        assertThat(other.isActive()).isFalse();
        verify(refreshTokenService, never()).revokeSession(current.getSessionId());
        verify(refreshTokenService).revokeSession(other.getSessionId());
        verify(activeSessionRepository).saveAll(List.of(other));
        verify(auditService, never()).logSuccess(user.getId(), AuditAction.LOGOUT, CLIENT.ipAddress(), CLIENT.userAgent());
    }

    private User user() {
        return User.register(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "user@example.com",
                "password-hash",
                NOW.minusSeconds(3600)
        );
    }
}
