package com.example.backend;

import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.JwtDenylistRepository;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.TotpBackupCodeRepository;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtLogoutService;
import com.example.backend.security.JwtService;
import com.example.backend.security.JwtValidator;
import com.example.backend.service.PasskeyService;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.auth.RefreshTokenService;
import com.example.backend.service.auth.TotpSetupService;
import com.example.backend.service.auth.TotpVerifyService;
import com.example.backend.service.auth.UserRegistrationService;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.document.PdfSigningService;
import com.example.backend.service.document.PdfVerificationService;
import com.example.backend.service.session.ActiveSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
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
class BackendApplicationTests {

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
	@SuppressWarnings("unused")
	private JwtService jwtService;

	@MockitoBean
	@SuppressWarnings("unused")
	private RefreshTokenService refreshTokenService;

	@MockitoBean
	@SuppressWarnings("unused")
	private JwtValidator jwtValidator;

	@MockitoBean
	@SuppressWarnings("unused")
	private JwtLogoutService jwtLogoutService;

	@MockitoBean
	@SuppressWarnings("unused")
	private TotpSetupService totpSetupService;

	@MockitoBean
	@SuppressWarnings("unused")
	private TotpVerifyService totpVerifyService;

	@MockitoBean
	@SuppressWarnings("unused")
	private AuditService auditService;

	@MockitoBean
	@SuppressWarnings("unused")
	private ActiveSessionService activeSessionService;

	@MockitoBean
	@SuppressWarnings("unused")
	private PdfSigningService pdfSigningService;

	@MockitoBean
	@SuppressWarnings("unused")
	private PdfVerificationService pdfVerificationService;

	@MockitoBean
	@SuppressWarnings("unused")
	private UserRepository userRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private PasskeyRepository passkeyRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private RefreshTokenRepository refreshTokenRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private JwtDenylistRepository jwtDenylistRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private TotpBackupCodeRepository totpBackupCodeRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private UserKeyRepository userKeyRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private DocumentSignatureRepository documentSignatureRepository;

	@Test
	void contextLoads() {
	}

}
