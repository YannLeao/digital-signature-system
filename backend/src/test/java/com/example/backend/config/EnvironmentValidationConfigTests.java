package com.example.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentValidationConfigTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(EnvironmentValidationConfig.class);

	@Test
	void startsWhenRequiredEnvironmentVariablesArePresent() {
		contextRunner
				.withPropertyValues(
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
						"WEBAUTHN_RP_NAME=Projeto Seguranca",
						"WEBAUTHN_ORIGIN=http://localhost:5173",
						"JWT_PRIVATE_KEY_BASE64=private-key",
						"JWT_PUBLIC_KEY_BASE64=public-key",
						"JWT_ISSUER=projeto-3-seguranca",
						"REFRESH_TOKEN_COOKIE_NAME=refresh_token",
						"REFRESH_TOKEN_COOKIE_SECURE=false",
						"REFRESH_TOKEN_COOKIE_SAME_SITE=Strict",
						"REFRESH_TOKEN_COOKIE_PATH=/api/v1/auth/refresh",
						"REFRESH_TOKEN_EXPIRATION_DAYS=7",
						"TOTP_ENCRYPTION_KEY_BASE64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
						"USER_KEY_ENCRYPTION_KEY_BASE64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
						"CSRF_COOKIE_NAME=XSRF-TOKEN",
						"CSRF_HEADER_NAME=X-CSRF-Token",
						"CSRF_COOKIE_SECURE=false",
						"CSRF_COOKIE_SAME_SITE=Strict",
						"CSRF_COOKIE_PATH=/",
						"PDF_SANDBOX_ENABLED=true",
						"PDF_SANDBOX_TIMEOUT_MS=5000"
				)
				.run(context -> assertThat(context).hasNotFailed());
	}

	@Test
	void failsWhenRequiredEnvironmentVariablesAreMissing() {
		contextRunner
				.withPropertyValues(
						"APP_ENV=",
						"APP_SECRET=",
						"DB_HOST=",
						"DB_PORT=",
						"DB_NAME=",
						"DB_USERNAME=",
						"DB_PASSWORD=",
						"CORS_ALLOWED_ORIGINS=",
						"CORS_ALLOWED_METHODS=",
						"CORS_ALLOWED_HEADERS=",
						"CORS_ALLOW_CREDENTIALS=",
						"CORS_MAX_AGE=",
						"WEBAUTHN_RP_ID=",
						"WEBAUTHN_RP_NAME=",
						"WEBAUTHN_ORIGIN=",
						"JWT_PRIVATE_KEY_BASE64=",
						"JWT_PUBLIC_KEY_BASE64=",
						"JWT_ISSUER=",
						"REFRESH_TOKEN_COOKIE_NAME=",
						"REFRESH_TOKEN_COOKIE_SECURE=",
						"REFRESH_TOKEN_COOKIE_SAME_SITE=",
						"REFRESH_TOKEN_COOKIE_PATH=",
						"REFRESH_TOKEN_EXPIRATION_DAYS=",
						"TOTP_ENCRYPTION_KEY_BASE64=",
						"USER_KEY_ENCRYPTION_KEY_BASE64=",
						"CSRF_COOKIE_NAME=",
						"CSRF_HEADER_NAME=",
						"CSRF_COOKIE_SECURE=",
						"CSRF_COOKIE_SAME_SITE=",
						"CSRF_COOKIE_PATH=",
						"PDF_SANDBOX_ENABLED=",
						"PDF_SANDBOX_TIMEOUT_MS="
				)
				.run(context -> assertThat(context)
						.hasFailed()
						.getFailure()
						.hasRootCauseInstanceOf(IllegalStateException.class)
						.hasMessageContaining("Missing required environment variables: APP_ENV, APP_SECRET, DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, CORS_ALLOWED_ORIGINS, CORS_ALLOWED_METHODS, CORS_ALLOWED_HEADERS, CORS_ALLOW_CREDENTIALS, CORS_MAX_AGE, WEBAUTHN_RP_ID, WEBAUTHN_RP_NAME, WEBAUTHN_ORIGIN, JWT_PRIVATE_KEY_BASE64, JWT_PUBLIC_KEY_BASE64, JWT_ISSUER, REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_COOKIE_SECURE, REFRESH_TOKEN_COOKIE_SAME_SITE, REFRESH_TOKEN_COOKIE_PATH, REFRESH_TOKEN_EXPIRATION_DAYS, TOTP_ENCRYPTION_KEY_BASE64, USER_KEY_ENCRYPTION_KEY_BASE64, CSRF_COOKIE_NAME, CSRF_HEADER_NAME, CSRF_COOKIE_SECURE, CSRF_COOKIE_SAME_SITE, CSRF_COOKIE_PATH, PDF_SANDBOX_ENABLED, PDF_SANDBOX_TIMEOUT_MS"));
	}
}
