package com.example.backend.controller;

import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.JwtDenylistRepository;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.TotpBackupCodeRepository;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PasskeyService;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.auth.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class SecurityHeadersTests {

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

	@Autowired
	SecurityHeadersTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void returnsRequiredSecurityHeadersForApiResponses() throws Exception {
		mockMvc.perform(get("/api/v1/health").servletPath("/api/v1"))
				.andExpect(status().isOk())
				.andExpect(header().string(
						"Content-Security-Policy",
						"default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none';"
				))
				.andExpect(header().string(
						"Strict-Transport-Security",
						"max-age=63072000; includeSubDomains; preload"
				))
				.andExpect(header().string("X-Frame-Options", "DENY"))
				.andExpect(header().string("X-Content-Type-Options", "nosniff"))
				.andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
				.andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
				.andExpect(header().string("X-XSS-Protection", "0"))
				.andExpect(header().string("Cache-Control", containsString("no-store")));
	}
}
