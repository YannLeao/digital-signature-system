package com.example.backend.controller.auth;

import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.dto.auth.TotpSetupResponse;
import com.example.backend.domain.User;
import com.example.backend.exception.AuthenticationFailedException;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.exception.RateLimitExceededException;
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
import com.example.backend.service.auth.LoginRateLimiter;
import com.example.backend.service.auth.RefreshTokenService;
import com.example.backend.service.auth.TotpSetupService;
import com.example.backend.service.auth.TotpVerifyService;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.auth.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTests {

    private UserRegistrationService userRegistrationService;
    private UserLoginService userLoginService;
    private LoginRateLimiter loginRateLimiter;
    private JwtService jwtService;
    private RefreshTokenService refreshTokenService;
    private RefreshTokenCookieFactory refreshTokenCookieFactory;
    private CsrfTokenService csrfTokenService;
    private JwtLogoutService jwtLogoutService;
    private JwtValidator jwtValidator;
    private TotpSetupService totpSetupService;
    private TotpVerifyService totpVerifyService;
    private AuditService auditService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRegistrationService = mock(UserRegistrationService.class);
        userLoginService = mock(UserLoginService.class);
        loginRateLimiter = mock(LoginRateLimiter.class);
        jwtService = mock(JwtService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        refreshTokenCookieFactory = mock(RefreshTokenCookieFactory.class);
        csrfTokenService = mock(CsrfTokenService.class);
        jwtLogoutService = mock(JwtLogoutService.class);
        jwtValidator = mock(JwtValidator.class);
        totpSetupService = mock(TotpSetupService.class);
        totpVerifyService = mock(TotpVerifyService.class);
        auditService = mock(AuditService.class);

        when(refreshTokenCookieFactory.cookieName()).thenReturn("refresh_token");
        when(refreshTokenCookieFactory.create(any())).thenAnswer(invocation -> ResponseCookie.from("refresh_token", invocation.getArgument(0))
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth/refresh")
                .maxAge(604800)
                .build());
        when(refreshTokenCookieFactory.clear()).thenReturn(ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .build());
        when(csrfTokenService.generateToken()).thenReturn("csrf-token");
        when(csrfTokenService.createCookie("csrf-token")).thenReturn(ResponseCookie.from("XSRF-TOKEN", "csrf-token")
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .build());
        when(csrfTokenService.clearCookie()).thenReturn(ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build());

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(
                        userRegistrationService,
                        userLoginService,
                        loginRateLimiter,
                        jwtService,
                        refreshTokenService,
                        refreshTokenCookieFactory,
                        csrfTokenService,
                        jwtLogoutService,
                        jwtValidator,
                        totpSetupService,
                        totpVerifyService,
                        auditService
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    void registersValidUserWithoutReturningPassword() throws Exception {
        String password = "StrongPassword123!";

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"StrongPassword123!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuario registrado com sucesso."))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain(password, "password", "passwordHash");
        verify(userRegistrationService).register(new RegisterUserRequest("user@example.com", password));
    }

    @Test
    void rejectsInvalidEmailWithGlobalValidationError() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"invalid","password":"StrongPassword123!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_001"))
                .andExpect(jsonPath("$.fields[0].field").value("email"));
    }

    @Test
    void rejectsWeakPasswordWithoutNumberWithGlobalValidationError() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"StrongPassword!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_001"))
                .andExpect(jsonPath("$.fields[0].field").value("password"));
    }

    @Test
    void rejectsWeakPasswordWithoutSymbolWithGlobalValidationError() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"StrongPassword123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_001"))
                .andExpect(jsonPath("$.fields[0].field").value("password"));
    }

    @Test
    void rejectsWeakPasswordWithoutUppercaseWithGlobalValidationError() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"strongpassword123!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_001"))
                .andExpect(jsonPath("$.fields[0].field").value("password"));
    }

    @Test
    void rejectsShortPasswordWithGlobalValidationError() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"Short1!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VAL_001"))
                .andExpect(jsonPath("$.fields[0].field").value("password"));
    }

    @Test
    void returnsControlledErrorForDuplicateEmail() throws Exception {
        doThrow(new BusinessException("E-mail ja cadastrado."))
                .when(userRegistrationService)
                .register(new RegisterUserRequest("user@example.com", "StrongPassword123!"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"StrongPassword123!"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VAL_003"))
                .andExpect(jsonPath("$.message").value("E-mail ja cadastrado."));
    }

    @Test
    void logsInValidUserWithoutReturningPasswordOrHash() throws Exception {
        String password = "StrongPassword123!";
        User user = User.register(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "user@example.com",
                "password-hash",
                Instant.parse("2026-05-22T12:00:00Z")
        );
        when(userLoginService.login(new LoginRequest("user@example.com", password))).thenReturn(user);
        when(jwtService.issueAccessToken(eq(user), eq(new ClientContext("203.0.113.10", "JUnit/5")), any()))
                .thenReturn(new AccessToken("jwt-token", "Bearer", 900));
        when(refreshTokenService.issueForLogin(eq(user), eq(new ClientContext("203.0.113.10", "JUnit/5")), any()))
                .thenReturn(new RefreshTokenPair("refresh-token-raw", null));

        String response = mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"StrongPassword123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refresh-token-raw")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Path=/api/v1/auth/refresh")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain(password, "password", "passwordHash", "$argon2id$", "refresh-token-raw", "refreshToken");
        verify(loginRateLimiter).consume("203.0.113.10");
        verify(userLoginService).login(new LoginRequest("user@example.com", password));
        verify(jwtService).issueAccessToken(eq(user), eq(new ClientContext("203.0.113.10", "JUnit/5")), any());
        verify(refreshTokenService).issueForLogin(eq(user), eq(new ClientContext("203.0.113.10", "JUnit/5")), any());
    }

    @Test
    void loginWithTotpEnabledReturnsOnlyHalfSessionTokenWithoutRefreshCookie() throws Exception {
        String password = "StrongPassword123!";
        User user = User.register(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "user@example.com",
                "password-hash",
                Instant.parse("2026-05-22T12:00:00Z")
        );
        user.enableTotp("encrypted-secret", Instant.parse("2026-05-22T12:01:00Z"));
        when(userLoginService.login(new LoginRequest("user@example.com", password))).thenReturn(user);
        when(jwtService.issueHalfSessionToken(eq(user), eq(new ClientContext("203.0.113.10", "JUnit/5"))))
                .thenReturn(new AccessToken("half-session-token", "Bearer", 300));

        mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"StrongPassword123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("half-session-token"))
                .andExpect(jsonPath("$.requiresTwoFactor").value(true))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("XSRF-TOKEN=csrf-token")));

        verify(jwtService).issueHalfSessionToken(user, new ClientContext("203.0.113.10", "JUnit/5"));
        verify(jwtService, never()).issueAccessToken(any(), any(), any());
        verify(refreshTokenService, never()).issueForLogin(any(), any(), any());
    }

    @Test
    void csrfEndpointIssuesReadableCookie() throws Exception {
        mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("CSRF token issued."))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("XSRF-TOKEN=csrf-token")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Path=/")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("HttpOnly"))));
    }

    @Test
    void setup2faUsesAuthenticatedSubjectOnly() throws Exception {
        Jwt jwt = jwt();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(jwt, null));
        when(totpSetupService.setup(UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenReturn(new TotpSetupResponse("otpauth://totp/projeto:user@example.com", List.of("ABCDEF1234567890ABCD")));

        mockMvc.perform(post("/auth/2fa/setup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otpauthUrl").value("otpauth://totp/projeto:user@example.com"))
                .andExpect(jsonPath("$.backupCodes[0]").value("ABCDEF1234567890ABCD"));

        verify(totpSetupService).setup(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        SecurityContextHolder.clearContext();
    }

    @Test
    void verify2faIssuesFinalJwtAndRefreshCookieAfterHalfSessionValidation() throws Exception {
        Jwt halfSession = halfSessionJwt();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(halfSession, null));
        when(jwtValidator.validateTotpChallengeToken("half-session-token")).thenReturn(halfSession);
        when(totpVerifyService.verify(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "123456",
                new ClientContext("203.0.113.10", "JUnit/5")
        )).thenReturn(new RefreshTokenResult(new AccessToken("final-jwt-token", "Bearer", 900), "refresh-token-raw"));

        String response = mockMvc.perform(post("/auth/2fa/verify")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("final-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refresh-token-raw")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain("refresh-token-raw", "refreshToken");
        verify(jwtValidator).validateTotpChallengeToken("half-session-token");
        verify(jwtLogoutService).logout(halfSession);
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsGenericAuthenticationErrorForLoginFailure() throws Exception {
        doThrow(new AuthenticationFailedException())
                .when(userLoginService)
                .login(new LoginRequest("user@example.com", "WrongPassword123!"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"WrongPassword123!"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_001"))
                .andExpect(jsonPath("$.message").value("Credenciais invalidas."));

        verify(jwtService, never()).issueAccessToken(any(), any());
        verify(refreshTokenService, never()).issueForLogin(any(), any(), any());
    }

    @Test
    void refreshWithCookieReturnsNewAccessTokenAndRotatedCookie() throws Exception {
        when(refreshTokenService.rotate("old-refresh-token", new ClientContext("203.0.113.10", "JUnit/5")))
                .thenReturn(new RefreshTokenResult(new AccessToken("new-jwt-token", "Bearer", 900), "new-refresh-token"));

        String response = mockMvc.perform(post("/auth/refresh")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit/5")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=new-refresh-token")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain("old-refresh-token", "new-refresh-token", "refreshToken");
        verify(refreshTokenService).rotate("old-refresh-token", new ClientContext("203.0.113.10", "JUnit/5"));
    }

    @Test
    void refreshWithoutCookieReturnsSafeAuthenticationError() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_003"))
                .andExpect(jsonPath("$.message").value("Sessao invalida ou expirada."));

        verify(refreshTokenService, never()).rotate(any(), any());
    }

    @Test
    void logoutDenylistsAccessTokenAndClearsRefreshCookie() throws Exception {
        Jwt jwt = jwt();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(jwt, null));

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout realizado com sucesso."))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")));

        verify(jwtLogoutService).logout(jwt);
        SecurityContextHolder.clearContext();
    }

    private Jwt jwt() {
        Instant issuedAt = Instant.parse("2026-05-22T12:00:00Z");
        return Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .subject("11111111-1111-1111-1111-111111111111")
                .claim("jti", "22222222-2222-2222-2222-222222222222")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(900))
                .claim("session_id", "33333333-3333-3333-3333-333333333333")
                .claim("ip", "ip-hash")
                .claim("ua_hash", "ua-hash")
                .build();
    }

    private Jwt halfSessionJwt() {
        Instant issuedAt = Instant.parse("2026-05-22T12:00:00Z");
        return Jwt.withTokenValue("half-session-token")
                .header("alg", "RS256")
                .subject("11111111-1111-1111-1111-111111111111")
                .claim("jti", "44444444-4444-4444-4444-444444444444")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(300))
                .claim("session_id", "55555555-5555-5555-5555-555555555555")
                .claim("token_use", "totp_challenge")
                .claim("scope", "2fa:verify")
                .claim("ip", "ip-hash")
                .claim("ua_hash", "ua-hash")
                .build();
    }

    @Test
    void returnsTooManyRequestsWhenLoginRateLimitIsExceeded() throws Exception {
        doThrow(new RateLimitExceededException())
                .when(loginRateLimiter)
                .consume("127.0.0.1");

        mockMvc.perform(post("/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"StrongPassword123!"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("SEC_001"));
    }
}