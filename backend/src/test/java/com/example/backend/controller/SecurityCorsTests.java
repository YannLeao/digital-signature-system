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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
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
		"CORS_ALLOWED_HEADERS=Authorization,Content-Type,X-Request-ID,X-CSRF-Token",
		"CORS_ALLOW_CREDENTIALS=true",
		"CORS_MAX_AGE=3600",
		"WEBAUTHN_RP_ID=localhost",
		"WEBAUTHN_RP_NAME=Projeto_Seguranca",
		"WEBAUTHN_ORIGIN=http://localhost:5173",
		"USER_KEY_ENCRYPTION_KEY_BASE64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
		"app.user-key.encryption-key-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class SecurityCorsTests {

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
	SecurityCorsTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Test
	void returnsCorsHeadersForAllowedOriginPreflight() throws Exception {
		mockMvc.perform(options("/api/v1/health")
						.servletPath("/api/v1")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
				.andExpect(header().string("Access-Control-Allow-Credentials", "true"))
				.andExpect(header().string("Access-Control-Max-Age", "3600"))
				.andExpect(header().string("Vary", "Origin"));
	}

	@Test
	void returnsCorsHeadersForAllowedCsrfHeaderPreflight() throws Exception {
		mockMvc.perform(options("/api/v1/auth/logout")
						.servletPath("/api/v1")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "POST")
						.header("Access-Control-Request-Headers", "X-CSRF-Token, Content-Type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
				.andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("X-CSRF-Token")));
	}

	@Test
	void exposesDocumentSignatureHeadersToBrowser() throws Exception {
		mockMvc.perform(options("/api/v1/documents/sign")
						.servletPath("/api/v1")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "POST")
						.header("Access-Control-Request-Headers", "Authorization, X-CSRF-Token, Content-Type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("X-Signature-Id")))
				.andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("X-Original-Hash")))
				.andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("X-Signed-Hash")))
				.andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("X-Signed-At")));
	}

	@Test
	void doesNotReturnPermissiveCorsHeadersForUnknownOriginPreflight() throws Exception {
		mockMvc.perform(options("/api/v1/health")
						.servletPath("/api/v1")
						.header("Origin", "http://malicious.example.com")
						.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}
}
