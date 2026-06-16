package com.example.backend.controller.auth;

import com.example.backend.domain.AuditAction;
import com.example.backend.event.TwoFactorChangedEvent;
import com.example.backend.dto.auth.CsrfTokenResponse;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.LoginResponse;
import com.example.backend.dto.auth.LogoutResponse;
import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.dto.auth.RegisterUserResponse;
import com.example.backend.dto.auth.TotpSetupResponse;
import com.example.backend.dto.auth.TotpVerifyRequest;
import com.example.backend.dto.auth.TotpVerifyResponse;
import com.example.backend.domain.User;
import com.example.backend.exception.InvalidAccessTokenException;
import com.example.backend.exception.InvalidRefreshTokenException;
import com.example.backend.security.AccessToken;
import com.example.backend.security.ClientContext;
import com.example.backend.security.CsrfTokenService;
import com.example.backend.security.JwtLogoutService;
import com.example.backend.security.JwtService;
import com.example.backend.security.JwtValidator;
import com.example.backend.security.RefreshTokenCookieFactory;
import com.example.backend.security.RefreshTokenPair;
import com.example.backend.security.RefreshTokenResult;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.session.ActiveSessionService;
import com.example.backend.service.auth.LoginRateLimiter;
import org.springframework.context.ApplicationEventPublisher;
import com.example.backend.service.auth.RefreshTokenService;
import com.example.backend.service.auth.TotpSetupService;
import com.example.backend.service.auth.TotpVerifyService;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.auth.UserRegistrationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRegistrationService userRegistrationService;
    private final UserLoginService userLoginService;
    private final LoginRateLimiter loginRateLimiter;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final CsrfTokenService csrfTokenService;
    private final JwtLogoutService jwtLogoutService;
    private final JwtValidator jwtValidator;
    private final TotpSetupService totpSetupService;
    private final TotpVerifyService totpVerifyService;
    private final AuditService auditService;
    private final ActiveSessionService activeSessionService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthController(
            UserRegistrationService userRegistrationService,
            UserLoginService userLoginService,
            LoginRateLimiter loginRateLimiter,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            RefreshTokenCookieFactory refreshTokenCookieFactory,
            CsrfTokenService csrfTokenService,
            JwtLogoutService jwtLogoutService,
            JwtValidator jwtValidator,
            TotpSetupService totpSetupService,
            TotpVerifyService totpVerifyService,
            AuditService auditService,
            ActiveSessionService activeSessionService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRegistrationService = userRegistrationService;
        this.userLoginService = userLoginService;
        this.loginRateLimiter = loginRateLimiter;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenCookieFactory = refreshTokenCookieFactory;
        this.csrfTokenService = csrfTokenService;
        this.jwtLogoutService = jwtLogoutService;
        this.jwtValidator = jwtValidator;
        this.totpSetupService = totpSetupService;
        this.totpVerifyService = totpVerifyService;
        this.auditService = auditService;
        this.activeSessionService = activeSessionService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        userRegistrationService.register(request);
        return new RegisterUserResponse("Usuario registrado com sucesso.");
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        loginRateLimiter.consume(servletRequest.getRemoteAddr());
        ClientContext clientContext = clientContext(servletRequest);

        User user;
        try {
            user = userLoginService.login(request, clientContext.ipAddress());
        } catch (Exception ex) {
            auditService.logFailure(null, AuditAction.AUTH_FAIL, clientContext.ipAddress(), clientContext.userAgent());
            throw ex;
        }

        if (user.isTotpEnabled()) {
            AccessToken halfSession = jwtService.issueHalfSessionToken(user, clientContext);
            auditService.logSuccess(user.getId(), AuditAction.TOKEN_ISSUED, clientContext.ipAddress(), clientContext.userAgent());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, csrfCookie().toString())
                    .body(LoginResponse.requiresTwoFactor(halfSession.token(), halfSession.tokenType(), halfSession.expiresIn()));
        }

        UUID sessionId = UUID.randomUUID();
        AccessToken accessToken = jwtService.issueAccessToken(user, clientContext, sessionId);
        RefreshTokenPair refreshToken = refreshTokenService.issueForLogin(user, clientContext, sessionId);
        activeSessionService.register(sessionId, user.getId(), clientContext);
        auditService.logSuccess(user.getId(), AuditAction.LOGIN, clientContext.ipAddress(), clientContext.userAgent());
        return accessTokenResponse(accessToken, refreshToken.rawToken());
    }

    @GetMapping("/csrf")
    ResponseEntity<CsrfTokenResponse> csrf() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, csrfCookie().toString())
                .body(new CsrfTokenResponse("CSRF token issued."));
    }

    @PostMapping("/2fa/setup")
    ResponseEntity<TotpSetupResponse> setup2fa(@AuthenticationPrincipal Jwt jwt, HttpServletRequest servletRequest) {
        if (jwt == null) {
            throw new InvalidAccessTokenException();
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        TotpSetupResponse response = totpSetupService.setup(userId);
        eventPublisher.publishEvent(new TwoFactorChangedEvent(
                userId, jwt.getSubject(), true,
                servletRequest.getRemoteAddr(), java.time.Instant.now()
        ));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/verify")
    ResponseEntity<TotpVerifyResponse> verify2fa(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TotpVerifyRequest request,
            HttpServletRequest servletRequest
    ) {
        if (jwt == null) {
            throw new InvalidAccessTokenException();
        }
        ClientContext clientContext = clientContext(servletRequest);
        Jwt halfSession = jwtValidator.validateTotpChallengeToken(jwt.getTokenValue());
        UUID userId = UUID.fromString(halfSession.getSubject());

        RefreshTokenResult result;
        try {
            result = totpVerifyService.verify(userId, request.code(), clientContext);
        } catch (Exception ex) {
            auditService.logFailure(userId, AuditAction.AUTH_FAIL, clientContext.ipAddress(), clientContext.userAgent());
            throw ex;
        }

        jwtLogoutService.logout(jwt);
        activeSessionService.register(result.sessionId(), userId, clientContext);
        auditService.logSuccess(userId, AuditAction.LOGIN, clientContext.ipAddress(), clientContext.userAgent());
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookieFactory.create(result.refreshToken()).toString(),
                        csrfCookie().toString()
                )
                .body(new TotpVerifyResponse(
                        result.accessToken().token(),
                        result.accessToken().tokenType(),
                        result.accessToken().expiresIn()
                ));
    }

    @PostMapping("/refresh")
    ResponseEntity<?> refresh(HttpServletRequest servletRequest) {
        RefreshTokenResult result = refreshTokenService.rotate(
                refreshTokenFromCookie(servletRequest), clientContext(servletRequest));
        return accessTokenResponse(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/logout")
    ResponseEntity<?> logout(@AuthenticationPrincipal Jwt jwt, HttpServletRequest servletRequest) {
        if (jwt == null) {
            throw new InvalidAccessTokenException();
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        ClientContext clientContext = clientContext(servletRequest);
        jwtLogoutService.logout(jwt);
        auditService.logSuccess(userId, AuditAction.LOGOUT, clientContext.ipAddress(), clientContext.userAgent());
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookieFactory.clear().toString(),
                        csrfTokenService.clearCookie().toString()
                )
                .body(new LogoutResponse("Logout realizado com sucesso."));
    }

    private ClientContext clientContext(HttpServletRequest request) {
        return new ClientContext(request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    private ResponseEntity<?> accessTokenResponse(AccessToken accessToken, String refreshToken) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookieFactory.create(refreshToken).toString(),
                        csrfCookie().toString()
                )
                .body(new LoginResponse(accessToken.token(), accessToken.tokenType(), accessToken.expiresIn()));
    }

    private org.springframework.http.ResponseCookie csrfCookie() {
        return csrfTokenService.createCookie(csrfTokenService.generateToken());
    }

    private String refreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InvalidRefreshTokenException();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> refreshTokenCookieFactory.cookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElseThrow(InvalidRefreshTokenException::new);
    }
}