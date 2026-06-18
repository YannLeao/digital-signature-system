package com.example.backend.service.audit;

import com.example.backend.domain.AuditAction;
import com.example.backend.domain.AuditLog;
import com.example.backend.domain.User;
import com.example.backend.repository.AuditLogRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class AuditServiceTests {

    private static final Instant NOW = Instant.parse("2026-06-18T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuditService service = new AuditService(auditLogRepository, userRepository, CLOCK);

    @Test
    void writesAuditEntryWithRequiredFieldsInUtc() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = User.register(userId, "user@example.com", "password-hash", NOW.minusSeconds(3600));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.logSuccess(userId, AuditAction.LOGIN, "203.0.113.10", "JUnit/5", "{\"flow\":\"password\"}");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog entry = captor.getValue();
        assertThat(entry.getId()).isNotNull();
        assertThat(entry.getUser()).isSameAs(user);
        assertThat(entry.getTimestampUtc()).isEqualTo(NOW);
        assertThat(entry.getIp()).isEqualTo("203.0.113.10");
        assertThat(entry.getUserAgent()).isEqualTo("JUnit/5");
        assertThat(entry.getAction()).isEqualTo(AuditAction.LOGIN);
        assertThat(entry.getResult()).isEqualTo("SUCCESS");
        assertThat(entry.getMetadata()).isEqualTo("{\"flow\":\"password\"}");
    }

    @Test
    void writesFailureEntryWithoutUserWhenUserCannotBeResolved() {
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        service.logFailure(userId, AuditAction.AUTH_FAIL, "203.0.113.20", "JUnit/5");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog entry = captor.getValue();
        assertThat(entry.getUser()).isNull();
        assertThat(entry.getResult()).isEqualTo("FAILURE");
        assertThat(entry.getMetadata()).isEqualTo("{}");
    }

    @Test
    void listsAuditEntriesForUserAsResponses() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = User.register(userId, "user@example.com", "password-hash", NOW.minusSeconds(3600));
        AuditLog entry = AuditLog.create(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                user,
                NOW,
                "203.0.113.10",
                "JUnit/5",
                AuditAction.LOGIN,
                "SUCCESS",
                "{\"flow\":\"password\"}"
        );
        PageRequest pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entry), pageable, 1));

        var page = service.listForUser(userId, AuditAction.LOGIN, "SUCCESS", null, null, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().id()).isEqualTo(entry.getId());
        assertThat(page.getContent().getFirst().action()).isEqualTo("LOGIN");
        assertThat(page.getContent().getFirst().metadata()).isEqualTo("{\"flow\":\"password\"}");
    }
}
