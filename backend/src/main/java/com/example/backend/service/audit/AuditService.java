package com.example.backend.service.audit;

import com.example.backend.domain.AuditAction;
import com.example.backend.domain.AuditLog;
import com.example.backend.domain.User;
import com.example.backend.repository.AuditLogRepository;
import com.example.backend.repository.UserRepository;
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
}
