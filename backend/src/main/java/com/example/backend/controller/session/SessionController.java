package com.example.backend.controller.session;

import com.example.backend.dto.session.SessionResponse;
import com.example.backend.exception.InvalidAccessTokenException;
import com.example.backend.security.ClientContext;
import com.example.backend.security.JwtDenylistService;
import com.example.backend.service.session.ActiveSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final ActiveSessionService activeSessionService;
    private final JwtDenylistService jwtDenylistService;

    public SessionController(ActiveSessionService activeSessionService, JwtDenylistService jwtDenylistService) {
        this.activeSessionService = activeSessionService;
        this.jwtDenylistService = jwtDenylistService;
    }

    @GetMapping
    ResponseEntity<List<SessionResponse>> listSessions(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new InvalidAccessTokenException();
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(activeSessionService.listActive(userId));
    }

    @DeleteMapping("/{sessionId}")
    ResponseEntity<Map<String, String>> revokeSession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sessionId,
            HttpServletRequest servletRequest
    ) {
        if (jwt == null) {
            throw new InvalidAccessTokenException();
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID currentSessionId = jwtDenylistService.sessionId(jwt);
        ClientContext clientContext = clientContext(servletRequest);

        activeSessionService.revokeSession(userId, sessionId, clientContext);

        if (sessionId.equals(currentSessionId)) {
            jwtDenylistService.denylist(jwt, com.example.backend.domain.JwtDenylistReason.LOGOUT);
        }

        return ResponseEntity.ok(Map.of("message", "Sessao encerrada com sucesso."));
    }

    @DeleteMapping("/all")
    ResponseEntity<Map<String, String>> revokeAllSessions(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest servletRequest
    ) {
        if (jwt == null) {
            throw new InvalidAccessTokenException();
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID currentSessionId = jwtDenylistService.sessionId(jwt);
        ClientContext clientContext = clientContext(servletRequest);

        activeSessionService.revokeAllSessions(userId, currentSessionId, clientContext);
        jwtDenylistService.denylist(jwt, com.example.backend.domain.JwtDenylistReason.LOGOUT);

        return ResponseEntity.ok(Map.of("message", "Todas as sessoes encerradas com sucesso."));
    }

    private ClientContext clientContext(HttpServletRequest request) {
        return new ClientContext(request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}
