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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserPasswordService {

    private static final String ARGON2ID_PREFIX = "$argon2id$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActiveSessionService activeSessionService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public UserPasswordService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ActiveSessionService activeSessionService,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher
    ) {
        this(userRepository, passwordEncoder, activeSessionService, auditService, eventPublisher, Clock.systemUTC());
    }

    UserPasswordService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ActiveSessionService activeSessionService,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activeSessionService = activeSessionService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public void changePassword(
            UUID userId,
            UUID currentSessionId,
            String currentPassword,
            String newPassword,
            ClientContext clientContext
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthenticationFailedException::new);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            auditService.logFailure(userId, AuditAction.AUTH_FAIL, clientContext.ipAddress(), clientContext.userAgent());
            throw new AuthenticationFailedException(userId);
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException("Nova senha deve ser diferente da senha atual.");
        }

        String passwordHash = passwordEncoder.encode(newPassword);
        if (!passwordHash.startsWith(ARGON2ID_PREFIX)) {
            throw new IllegalStateException("Configured password encoder did not produce an Argon2id hash.");
        }

        Instant now = Instant.now(clock);
        user.changePassword(passwordHash, now);
        userRepository.save(user);

        activeSessionService.revokeOtherSessions(userId, currentSessionId, clientContext);
        auditService.logSuccess(userId, AuditAction.PASSWORD_CHANGED, clientContext.ipAddress(), clientContext.userAgent());
        eventPublisher.publishEvent(new PasswordChangedEvent(userId, user.getEmail(), clientContext.ipAddress(), now));
    }
}
