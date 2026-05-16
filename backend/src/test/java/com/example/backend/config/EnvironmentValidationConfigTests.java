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
						"DB_PASSWORD=postgres"
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
						"DB_PASSWORD="
				)
				.run(context -> assertThat(context)
						.hasFailed()
						.getFailure()
						.hasRootCauseInstanceOf(IllegalStateException.class)
						.hasMessageContaining("Missing required environment variables: APP_ENV, APP_SECRET, DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD"));
	}
}
