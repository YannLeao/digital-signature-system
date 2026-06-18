package com.example.backend.controller.audit;

import com.example.backend.domain.AuditAction;
import com.example.backend.dto.audit.AuditLogResponse;
import com.example.backend.exception.InvalidAccessTokenException;
import com.example.backend.service.audit.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequestMapping("/audit-log")
public class AuditLogController {

    private static final int MAX_PAGE_SIZE = 50;

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/me")
    ResponseEntity<Page<AuditLogResponse>> listCurrentUserActivity(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (jwt == null) {
            throw new InvalidAccessTokenException();
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "timestampUtc")
        );

        return ResponseEntity.ok(auditService.listForUser(
                userId,
                action,
                normalizeResult(result),
                from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null,
                to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null,
                pageable
        ));
    }

    private String normalizeResult(String result) {
        if (result == null || result.isBlank()) {
            return null;
        }
        return result.trim().toUpperCase();
    }
}
