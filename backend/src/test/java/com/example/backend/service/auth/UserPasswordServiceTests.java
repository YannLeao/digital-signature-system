package com.example.backend.service.auth;

import com.example.backend.domain.AuditAction;
import com.example.backend.domain.User;
import com.example.backend.event.PasswordChangedEvent;
import com.example.backend.exception.AuthenticationFailedException;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.ClientContext;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.session.ActiveSessionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPasswordServiceTests {

    private static final Instant NOW = Instant.parse("2026-06-18T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SESSION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final ClientContext CLIENT = new ClientContext("203.0.113.10", "JUnit/5");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final ActiveSessionService activeSessionService = mock(ActiveSessionService.class);
    private final AuditService auditService = mock(AuditService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final UserPasswordService service = new UserPasswordService(
            userRepository,
            passwordEncoder,
            activeSessionService,
            auditService,
            eventPublisher,
            CLOCK
    );

    @Test
    void changesPasswordAuditsEventPublishesEmailEventAndRevokesOtherSessions() {
        User user = user();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPassword123!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123!", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("$argon2id$new-hash");

        service.changePassword(USER_ID, SESSION_ID, "CurrentPassword123!", "NewPassword123!", CLIENT);

        assertThat(user.getPasswordHash()).isEqualTo("$argon2id$new-hash");
        assertThat(user.getUpdatedAt()).isEqualTo(NOW);
        verify(userRepository).save(user);
        verify(activeSessionService).revokeOtherSessions(USER_ID, SESSION_ID, CLIENT);
        verify(auditService).logSuccess(USER_ID, AuditAction.PASSWORD_CHANGED, CLIENT.ipAddress(), CLIENT.userAgent());

        ArgumentCaptor<PasswordChangedEvent> eventCaptor = ArgumentCaptor.forClass(PasswordChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().email()).isEqualTo("user@example.com");
        assertThat(eventCaptor.getValue().ip()).isEqualTo(CLIENT.ipAddress());
        assertThat(eventCaptor.getValue().changedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsInvalidCurrentPasswordWithGenericAuthenticationErrorAndAuditFailure() {
        User user = user();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword123!", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(USER_ID, SESSION_ID, "WrongPassword123!", "NewPassword123!", CLIENT))
                .isInstanceOf(AuthenticationFailedException.class);

        verify(auditService).logFailure(USER_ID, AuditAction.AUTH_FAIL, CLIENT.ipAddress(), CLIENT.userAgent());
        verify(userRepository, never()).save(user);
        verify(activeSessionService, never()).revokeOtherSessions(USER_ID, SESSION_ID, CLIENT);
        verify(eventPublisher, never()).publishEvent(org.mockito.Mockito.any());
    }

    @Test
    void rejectsReusingSamePassword() {
        User user = user();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CurrentPassword123!", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("CurrentPassword123!", "old-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(USER_ID, SESSION_ID, "CurrentPassword123!", "CurrentPassword123!", CLIENT))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Nova senha deve ser diferente da senha atual.");

        verify(userRepository, never()).save(user);
        verify(activeSessionService, never()).revokeOtherSessions(USER_ID, SESSION_ID, CLIENT);
    }

    private User user() {
        return User.register(USER_ID, "user@example.com", "old-hash", NOW.minusSeconds(3600));
    }
}
