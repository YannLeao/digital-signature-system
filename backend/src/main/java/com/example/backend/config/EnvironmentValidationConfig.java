package com.example.backend.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
public class EnvironmentValidationConfig {

	static final List<String> REQUIRED_VARIABLES = List.of(
			"APP_ENV",
			"APP_SECRET",
			"DB_HOST",
			"DB_PORT",
			"DB_NAME",
			"DB_USERNAME",
			"DB_PASSWORD"
	);

	@Bean
	InitializingBean validateRequiredEnvironmentVariables(Environment environment) {
		return () -> {
			List<String> missingVariables = REQUIRED_VARIABLES.stream()
					.filter(variable -> isBlank(environment.getProperty(variable)))
					.toList();

			if (!missingVariables.isEmpty()) {
				throw new IllegalStateException("Missing required environment variables: " + String.join(", ", missingVariables));
			}
		};
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
