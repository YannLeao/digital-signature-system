package com.example.backend.controller.auth;

import com.example.backend.repository.PasskeyRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PasskeyService;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.auth.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
	@SuppressWarnings("unused")
	private UserRepository userRepository;

	@MockitoBean
	@SuppressWarnings("unused")
	private PasskeyRepository passkeyRepository;

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
		mockMvc.perform(post("/api/v1/auth/login")
						.servletPath("/api/v1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Login realizado com sucesso."));
	}
}
