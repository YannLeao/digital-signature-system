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
						"CORS_ALLOWED_HEADERS=Authorization,Content-Type,X-Request-ID",
						"CORS_ALLOW_CREDENTIALS=true",
						"CORS_MAX_AGE=3600",
						"WEBAUTHN_RP_ID=localhost",
						"WEBAUTHN_RP_NAME=Projeto Seguranca",
						"WEBAUTHN_ORIGIN=http://localhost:5173"
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
						"WEBAUTHN_ORIGIN="
				)
				.run(context -> assertThat(context)
						.hasFailed()
						.getFailure()
						.hasRootCauseInstanceOf(IllegalStateException.class)
						.hasMessageContaining("Missing required environment variables: APP_ENV, APP_SECRET, DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, CORS_ALLOWED_ORIGINS, CORS_ALLOWED_METHODS, CORS_ALLOWED_HEADERS, CORS_ALLOW_CREDENTIALS, CORS_MAX_AGE, WEBAUTHN_RP_ID, WEBAUTHN_RP_NAME, WEBAUTHN_ORIGIN"));
	}
}
