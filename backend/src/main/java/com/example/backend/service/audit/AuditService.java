package com.example.backend.service.audit;

import com.example.backend.domain.AuditAction;
import com.example.backend.domain.AuditLog;
import com.example.backend.domain.User;
import com.example.backend.dto.audit.AuditLogResponse;
import com.example.backend.repository.AuditLogRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {

    static final String RESULT_SUCCESS = "SUCCESS";
    static final String RESULT_FAILURE = "FAILURE";
    static final String METADATA_EMPTY = "{}";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this(auditLogRepository, userRepository, Clock.systemUTC());
    }

    AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository, Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(UUID userId, AuditAction action, String ip, String userAgent) {
        log(userId, action, RESULT_SUCCESS, ip, userAgent, METADATA_EMPTY);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(UUID userId, AuditAction action, String ip, String userAgent, String metadata) {
        log(userId, action, RESULT_SUCCESS, ip, userAgent, metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(UUID userId, AuditAction action, String ip, String userAgent) {
        log(userId, action, RESULT_FAILURE, ip, userAgent, METADATA_EMPTY);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(UUID userId, AuditAction action, String ip, String userAgent, String metadata) {
        log(userId, action, RESULT_FAILURE, ip, userAgent, metadata);
    }

    private void log(UUID userId, AuditAction action, String result, String ip, String userAgent, String metadata) {
        User user = userId != null
                ? userRepository.findById(userId).orElse(null)
                : null;

        AuditLog entry = AuditLog.create(
                UUID.randomUUID(),
                user,
                Instant.now(clock),
                ip,
                userAgent,
                action,
                result,
                metadata != null ? metadata : METADATA_EMPTY
        );

        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> listForUser(
            UUID userId,
            AuditAction action,
            String result,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        return auditLogRepository.findAll(activitySpecification(userId, action, result, from, to), pageable)
                .map(this::toResponse);
    }

    private Specification<AuditLog> activitySpecification(
            UUID userId,
            AuditAction action,
            String result,
            Instant from,
            Instant to
    ) {
        return (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            if (action != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            if (result != null) {
                predicates.add(criteriaBuilder.equal(root.get("result"), result));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestampUtc"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("timestampUtc"), to));
            }

            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getTimestampUtc(),
                entry.getIp(),
                entry.getUserAgent(),
                entry.getAction().name(),
                entry.getResult(),
                entry.getMetadata()
        );
    }
}
