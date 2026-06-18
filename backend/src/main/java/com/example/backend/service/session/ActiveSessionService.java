package com.example.backend.service.session;

import com.example.backend.domain.ActiveSession;
import com.example.backend.domain.AuditAction;
import com.example.backend.domain.User;
import com.example.backend.dto.session.SessionResponse;
import com.example.backend.event.NewLoginEvent;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ActiveSessionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.ClientContext;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.auth.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ActiveSessionService {

    private final ActiveSessionRepository activeSessionRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public ActiveSessionService(
            ActiveSessionRepository activeSessionRepository,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher
    ) {
        this(activeSessionRepository, userRepository, refreshTokenService, auditService, eventPublisher, Clock.systemUTC());
    }

    ActiveSessionService(
            ActiveSessionRepository activeSessionRepository,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher,
            Clock clock
    ) {
        this.activeSessionRepository = activeSessionRepository;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public void register(UUID sessionId, UUID userId, ClientContext clientContext) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado."));
        boolean unknownIp = !activeSessionRepository.existsByUserIdAndIp(userId, clientContext.ipAddress());
        ActiveSession session = ActiveSession.create(sessionId, user, clientContext.ipAddress(), clientContext.userAgent(), Instant.now(clock));
        activeSessionRepository.save(session);
        if (unknownIp) {
            eventPublisher.publishEvent(new NewLoginEvent(user.getId(), user.getEmail(), clientContext.ipAddress(), session.getCreatedAt()));
        }
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listActive(UUID userId) {
        return activeSessionRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId, ClientContext clientContext) {
        ActiveSession session = activeSessionRepository.findBySessionIdAndUserIdAndIsActiveTrue(sessionId, userId)
                .orElseThrow(ResourceNotFoundException::new);

        Instant now = Instant.now(clock);
        session.deactivate(now);
        activeSessionRepository.save(session);

        refreshTokenService.revokeSession(sessionId);
        auditService.logSuccess(userId, AuditAction.LOGOUT, clientContext.ipAddress(), clientContext.userAgent());
    }

    @Transactional
    public void revokeAllSessions(UUID userId, UUID currentSessionId, ClientContext clientContext) {
        Instant now = Instant.now(clock);
        List<ActiveSession> sessions = activeSessionRepository.findByUserIdAndIsActiveTrue(userId);

        for (ActiveSession session : sessions) {
            session.deactivate(now);
            refreshTokenService.revokeSession(session.getSessionId());
        }

        activeSessionRepository.saveAll(sessions);
        auditService.logSuccess(userId, AuditAction.LOGOUT, clientContext.ipAddress(), clientContext.userAgent());
    }

    @Transactional
    public void revokeOtherSessions(UUID userId, UUID currentSessionId, ClientContext clientContext) {
        Instant now = Instant.now(clock);
        List<ActiveSession> sessions = activeSessionRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .filter(session -> !session.getSessionId().equals(currentSessionId))
                .toList();

        for (ActiveSession session : sessions) {
            session.deactivate(now);
            refreshTokenService.revokeSession(session.getSessionId());
        }

        activeSessionRepository.saveAll(sessions);
    }

    private SessionResponse toResponse(ActiveSession session) {
        return new SessionResponse(
                session.getSessionId(),
                session.getDeviceInfo(),
                session.getIp(),
                session.getUserAgent(),
                session.getCreatedAt(),
                session.getLastSeenAt()
        );
    }
}
