package com.example.backend.controller.audit;

import com.example.backend.domain.AuditAction;
import com.example.backend.dto.audit.AuditLogResponse;
import com.example.backend.service.audit.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogControllerTests {

    private final AuditService auditService = mock(AuditService.class);
    private final AuditLogController controller = new AuditLogController(auditService);

    @Test
    void controllerMappingUsesServletPrefixOnlyOnce() {
        RequestMapping mapping = AuditLogController.class.getAnnotation(RequestMapping.class);

        assertThat(mapping.value()).containsExactly("/audit-log");
    }

    @Test
    void listsCurrentUserActivityWithFiltersAndPagination() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AuditLogResponse entry = new AuditLogResponse(
                UUID.randomUUID(),
                Instant.parse("2026-06-18T12:00:00Z"),
                "203.0.113.10",
                "JUnit/5",
                "LOGIN",
                "SUCCESS",
                "{}"
        );
        PageRequest expectedPage = PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "timestampUtc"));
        when(auditService.listForUser(
                org.mockito.Mockito.eq(userId),
                org.mockito.Mockito.eq(AuditAction.LOGIN),
                org.mockito.Mockito.eq("SUCCESS"),
                org.mockito.Mockito.eq(Instant.parse("2026-06-01T00:00:00Z")),
                org.mockito.Mockito.eq(Instant.parse("2026-06-19T00:00:00Z")),
                org.mockito.Mockito.eq(expectedPage)
        )).thenReturn(new PageImpl<>(List.of(entry), expectedPage, 1));

        var response = controller.listCurrentUserActivity(
                jwt(userId),
                AuditAction.LOGIN,
                "success",
                java.time.LocalDate.parse("2026-06-01"),
                java.time.LocalDate.parse("2026-06-18"),
                1,
                20
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(entry);
        verify(auditService).listForUser(
                org.mockito.Mockito.eq(userId),
                org.mockito.Mockito.eq(AuditAction.LOGIN),
                org.mockito.Mockito.eq("SUCCESS"),
                org.mockito.Mockito.eq(Instant.parse("2026-06-01T00:00:00Z")),
                org.mockito.Mockito.eq(Instant.parse("2026-06-19T00:00:00Z")),
                org.mockito.Mockito.eq(expectedPage)
        );
    }

    @Test
    void clampsPaginationSize() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        PageRequest expectedPage = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "timestampUtc"));
        when(auditService.listForUser(
                org.mockito.Mockito.eq(userId),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.eq(expectedPage)
        )).thenReturn(new PageImpl<>(List.of(), expectedPage, 0));

        controller.listCurrentUserActivity(jwt(userId), null, "", null, null, -1, 999);

        verify(auditService).listForUser(
                org.mockito.Mockito.eq(userId),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.isNull(),
                org.mockito.Mockito.eq(expectedPage)
        );
    }

    private Jwt jwt(UUID userId) {
        Instant issuedAt = Instant.parse("2026-06-18T12:00:00Z");
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(900))
                .build();
    }
}
