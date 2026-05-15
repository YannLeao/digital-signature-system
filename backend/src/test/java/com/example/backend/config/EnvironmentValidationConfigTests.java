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
						"APP_SECRET=0123456789abcdef0123456789abcdef"
				)
				.run(context -> assertThat(context).hasNotFailed());
	}

	@Test
	void failsWhenRequiredEnvironmentVariablesAreMissing() {
		contextRunner.run(context -> assertThat(context)
				.hasFailed()
				.getFailure()
				.hasRootCauseInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Missing required environment variables: APP_ENV, APP_SECRET"));
	}
}
