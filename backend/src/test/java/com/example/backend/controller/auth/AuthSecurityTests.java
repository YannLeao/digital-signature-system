package com.example.backend.controller.auth;

import com.example.backend.domain.User;
import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.JwtDenylistRepository;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.TotpBackupCodeRepository;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.AccessToken;
import com.example.backend.security.JwtLogoutService;
import com.example.backend.security.JwtService;
import com.example.backend.security.JwtValidator;
import com.example.backend.security.RefreshTokenPair;
import com.example.backend.service.document.PdfSigningService;
import com.example.backend.service.document.PdfVerificationService;
import com.example.backend.service.PasskeyService;
import com.example.backend.service.auth.RefreshTokenService;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.auth.UserRegistrationService;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.session.ActiveSessionService;
import com.example.backend.dto.document.SignedPdfResult;
import com.example.backend.dto.document.VerifyDocumentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"APP_ENV=test",
		"APP_SECRET=0123456789abcdef0123456789abcdef",
		"DB_HOST=localhost",
		"DB_PORT=5432",
		"DB_NAME=projeto_3_seguranca",
		"DB_USERNAME=postgres",
		"DB_PASSWORD=postgres",
		"CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173",
		"CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS",
		"CORS_ALLOWED_HEADERS=Authorization,Content-Type,X-Request-ID",
		"CORS_ALLOW_CREDENTIALS=true",
		"CORS_MAX_AGE=3600",
		"WEBAUTHN_RP_ID=localhost",
		"WEBAUTHN_RP_NAME=Projeto_Seguranca",
		"WEBAUTHN_ORIGIN=http://localhost:5173",
		"USER_KEY_ENCRYPTION_KEY_BASE64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
		"app.user-key.encryption-key-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
		"PDF_SANDBOX_ENABLED=true",
		"PDF_SANDBOX_TIMEOUT_MS=5000",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class AuthSecurityTests {

	private final MockMvc mockMvc;

	@MockitoBean
	@SuppressWarnings("unused")
	private UserRegistrationService userRegistrationService;

	@MockitoBean
	@SuppressWarnings("unused")
	private UserLoginService userLoginService;

	@MockitoBean
	@SuppressWarnings("unused")
	private PasskeyService passkeyService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private RefreshTokenService refreshTokenService;

	@MockitoBean
	private JwtValidator jwtValidator;

	@MockitoBean
	private JwtLogoutService jwtLogoutService;

	@MockitoBean
	@SuppressWarnings("unused")
	private JwtDenylistRepository jwtDenylistRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private UserRepository userRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private PasskeyRepository passkeyRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private TotpBackupCodeRepository totpBackupCodeRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private UserKeyRepository userKeyRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private DocumentSignatureRepository documentSignatureRepository;

	@MockitoBean
	private PdfSigningService pdfSigningService;

	@MockitoBean
	private PdfVerificationService pdfVerificationService;

	@MockitoBean
	@SuppressWarnings("unused")
	private AuditService auditService;

	@MockitoBean
	@SuppressWarnings("unused")
	private ActiveSessionService activeSessionService;

	@Autowired
	AuthSecurityTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void permitsVersionedRegistrationWithoutAuthenticationOrCsrfToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.servletPath("/api/v1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("Usuario registrado com sucesso."));
	}

	@Test
	void permitsVersionedLoginWithoutAuthenticationOrCsrfToken() throws Exception {
		when(userLoginService.login(any(), any())).thenReturn(User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				Instant.parse("2026-05-22T12:00:00Z")
		));
		when(jwtService.issueAccessToken(any(), any(), any())).thenReturn(new AccessToken("jwt-token", "Bearer", 900));
		when(refreshTokenService.issueForLogin(any(), any(), any())).thenReturn(new RefreshTokenPair("refresh-token", null));

		mockMvc.perform(post("/api/v1/auth/login")
						.servletPath("/api/v1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")));
	}

	@Test
	void permitsVersionedRefreshWithValidCsrfToken() throws Exception {
		when(refreshTokenService.rotate(any(), any()))
				.thenReturn(new com.example.backend.security.RefreshTokenResult(
						new AccessToken("new-jwt-token", "Bearer", 900),
						"new-refresh-token"
				));

		mockMvc.perform(post("/api/v1/auth/refresh")
						.servletPath("/api/v1")
						.header("X-CSRF-Token", "csrf-token")
						.cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf-token"))
						.cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("new-jwt-token"))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=new-refresh-token")));
	}

	@Test
	void rejectsVersionedRefreshWithoutCsrfToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/refresh")
						.servletPath("/api/v1")
						.cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("SEC_001"));
	}

	@Test
	void protectsVersionedLogoutWithBearerTokenAndClearsCookie() throws Exception {
		Jwt jwt = jwt();
		when(jwtValidator.validateAccessToken("access-token")).thenReturn(jwt);

		mockMvc.perform(post("/api/v1/auth/logout")
						.servletPath("/api/v1")
						.header("Authorization", "Bearer access-token")
						.header("X-CSRF-Token", "csrf-token")
						.cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf-token")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Logout realizado com sucesso."))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=")))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")));

		org.mockito.Mockito.verify(jwtLogoutService).logout(jwt);
	}

	@Test
	void rejectsVersionedDocumentSigningWithoutCsrfToken() throws Exception {
		Jwt jwt = jwt();
		when(jwtValidator.validateAccessToken("access-token")).thenReturn(jwt);

		mockMvc.perform(multipart("/api/v1/documents/sign")
						.file("file", "%PDF-".getBytes())
						.file("request", """
								{"sealPage":1,"sealX":10,"sealY":70}
								""".getBytes())
						.servletPath("/api/v1")
						.header("Authorization", "Bearer access-token"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("SEC_001"));
	}

	@Test
	void versionedDocumentSigningWithCsrfTokenReachesAuthenticationLayer() throws Exception {
		mockMvc.perform(multipart("/api/v1/documents/sign")
						.file("file", "%PDF-".getBytes())
						.file("request", """
								{"sealPage":1,"sealX":10,"sealY":70}
								""".getBytes())
						.servletPath("/api/v1")
						.header("X-CSRF-Token", "csrf-token")
						.cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf-token")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void permitsVersionedPublicVerifyWithoutAuthenticationOrCsrfToken() throws Exception {
		when(pdfVerificationService.verify(any(), any())).thenReturn(VerifyDocumentResponse.notFound());

		mockMvc.perform(multipart("/api/v1/verify")
						.file("file", "%PDF-".getBytes())
						.servletPath("/api/v1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("NOT_FOUND"));
	}

	@Test
	void signsVersionedDocumentWithBearerTokenAndCsrfToken() throws Exception {
		Jwt jwt = jwt();
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				Instant.parse("2026-05-22T12:00:00Z")
		);
		when(jwtValidator.validateAccessToken("access-token")).thenReturn(jwt);
		when(userRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111111")))
				.thenReturn(Optional.of(user));
		when(pdfSigningService.sign(any(), any(), any(), any(), any()))
				.thenReturn(new SignedPdfResult(
						"%PDF-signed".getBytes(),
						UUID.fromString("22222222-2222-2222-2222-222222222222"),
						"a".repeat(64),
						"b".repeat(64),
						Instant.parse("2026-06-09T12:00:00Z")
				));

		mockMvc.perform(multipart("/api/v1/documents/sign")
						.file("file", "%PDF-".getBytes())
						.file("request", """
								{"sealPage":1,"sealX":10,"sealY":70}
								""".getBytes())
						.servletPath("/api/v1")
						.header("Authorization", "Bearer access-token")
						.header("X-CSRF-Token", "csrf-token")
						.cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "csrf-token")))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Signature-Id", "22222222-2222-2222-2222-222222222222"))
				.andExpect(header().string("X-Original-Hash", "a".repeat(64)))
				.andExpect(header().string("X-Signed-Hash", "b".repeat(64)));
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
				.claim("token_use", "access")
				.claim("ip", "ip-hash")
				.claim("ua_hash", "ua-hash")
				.build();
	}
}
