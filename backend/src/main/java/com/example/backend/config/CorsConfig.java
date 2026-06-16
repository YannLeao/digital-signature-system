package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

	private static final List<String> EXPOSED_SIGNATURE_HEADERS = List.of(
			"X-Signature-Id",
			"X-Original-Hash",
			"X-Signed-Hash",
			"X-Signed-At"
	);

	@Bean
	CorsConfigurationSource corsConfigurationSource(Environment environment) {
		List<String> allowedOrigins = readCsv(environment, "CORS_ALLOWED_ORIGINS");
		List<String> allowedMethods = readCsv(environment, "CORS_ALLOWED_METHODS");
		List<String> allowedHeaders = readCsv(environment, "CORS_ALLOWED_HEADERS");
		boolean allowCredentials = readBoolean(environment, "CORS_ALLOW_CREDENTIALS");
		long maxAge = readLong(environment, "CORS_MAX_AGE");

		if (allowCredentials && allowedOrigins.contains("*")) {
			throw new IllegalStateException("CORS_ALLOW_CREDENTIALS cannot be true when CORS_ALLOWED_ORIGINS contains wildcard.");
		}

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(allowedMethods);
		configuration.setAllowedHeaders(allowedHeaders);
		configuration.setExposedHeaders(EXPOSED_SIGNATURE_HEADERS);
		configuration.setAllowCredentials(allowCredentials);
		configuration.setMaxAge(maxAge);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private List<String> readCsv(Environment environment, String propertyName) {
		String value = readRequiredString(environment, propertyName);

		List<String> values = Arrays.stream(value.split(","))
				.map(String::trim)
				.filter(item -> !item.isBlank())
				.toList();

		if (values.isEmpty()) {
			throw new IllegalStateException("Environment variable must contain at least one value: " + propertyName);
		}

		return values;
	}

	private boolean readBoolean(Environment environment, String propertyName) {
		String value = readRequiredString(environment, propertyName);

		if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
			throw new IllegalStateException("Environment variable must be true or false: " + propertyName);
		}

		return Boolean.parseBoolean(value);
	}

	private long readLong(Environment environment, String propertyName) {
		String value = readRequiredString(environment, propertyName);

		try {
			return Long.parseLong(value);
		} catch (NumberFormatException exception) {
			throw new IllegalStateException("Environment variable must be a valid long: " + propertyName);
		}
	}

	private String readRequiredString(Environment environment, String propertyName) {
		String value = environment.getProperty(propertyName);

		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing required environment variable: " + propertyName);
		}

		return value.trim();
	}
}
