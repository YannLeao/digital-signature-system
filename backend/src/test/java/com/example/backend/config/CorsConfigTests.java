package com.example.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsConfigTests {

	private final CorsConfig corsConfig = new CorsConfig();

	@Test
	void createsRestrictiveCorsConfigurationFromEnvironment() {
		CorsConfigurationSource source = corsConfig.corsConfigurationSource(validEnvironment());
		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/health");

		CorsConfiguration configuration = source.getCorsConfiguration(request);

		assertThat(configuration).isNotNull();
		assertThat(configuration.getAllowedOrigins()).containsExactly("http://localhost:5173", "http://127.0.0.1:5173");
		assertThat(configuration.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS");
		assertThat(configuration.getAllowedHeaders()).containsExactly("Authorization", "Content-Type", "X-Request-ID");
		assertThat(configuration.getAllowCredentials()).isTrue();
		assertThat(configuration.getMaxAge()).isEqualTo(3600);
	}

	@Test
	void rejectsWildcardOriginsWhenCredentialsAreAllowed() {
		MockEnvironment environment = validEnvironment()
				.withProperty("CORS_ALLOWED_ORIGINS", "*");

		assertThatThrownBy(() -> corsConfig.corsConfigurationSource(environment))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("CORS_ALLOW_CREDENTIALS cannot be true when CORS_ALLOWED_ORIGINS contains wildcard.");
	}

	@Test
	void rejectsInvalidCredentialsFlag() {
		MockEnvironment environment = validEnvironment()
				.withProperty("CORS_ALLOW_CREDENTIALS", "maybe");

		assertThatThrownBy(() -> corsConfig.corsConfigurationSource(environment))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Environment variable must be true or false: CORS_ALLOW_CREDENTIALS");
	}

	private MockEnvironment validEnvironment() {
		return new MockEnvironment()
				.withProperty("CORS_ALLOWED_ORIGINS", "http://localhost:5173,http://127.0.0.1:5173")
				.withProperty("CORS_ALLOWED_METHODS", "GET,POST,PUT,DELETE,OPTIONS")
				.withProperty("CORS_ALLOWED_HEADERS", "Authorization,Content-Type,X-Request-ID")
				.withProperty("CORS_ALLOW_CREDENTIALS", "true")
				.withProperty("CORS_MAX_AGE", "3600");
	}
}
