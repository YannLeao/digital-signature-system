package com.example.backend.security;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieProperties {

	private final String name;
	private final boolean secure;
	private final String sameSite;
	private final String path;
	private final Duration maxAge;

	public RefreshTokenCookieProperties(Environment environment) {
		this.name = readRequired(environment, "REFRESH_TOKEN_COOKIE_NAME");
		this.secure = readBoolean(environment, "REFRESH_TOKEN_COOKIE_SECURE");
		this.sameSite = readRequired(environment, "REFRESH_TOKEN_COOKIE_SAME_SITE");
		this.path = readRequired(environment, "REFRESH_TOKEN_COOKIE_PATH");
		this.maxAge = Duration.ofDays(readLong(environment, "REFRESH_TOKEN_EXPIRATION_DAYS"));

		if (isProduction(environment.getProperty("APP_ENV")) && !secure) {
			throw new IllegalStateException("REFRESH_TOKEN_COOKIE_SECURE must be true in production.");
		}
	}

	public String name() {
		return name;
	}

	public boolean secure() {
		return secure;
	}

	public String sameSite() {
		return sameSite;
	}

	public String path() {
		return path;
	}

	public Duration maxAge() {
		return maxAge;
	}

	private boolean isProduction(String appEnv) {
		return appEnv != null && (appEnv.equalsIgnoreCase("prod") || appEnv.equalsIgnoreCase("production"));
	}

	private String readRequired(Environment environment, String name) {
		String value = environment.getProperty(name);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing required environment variable: " + name);
		}
		return value.trim();
	}

	private boolean readBoolean(Environment environment, String name) {
		String value = readRequired(environment, name);
		if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
			throw new IllegalStateException("Environment variable must be true or false: " + name);
		}
		return Boolean.parseBoolean(value);
	}

	private long readLong(Environment environment, String name) {
		String value = readRequired(environment, name);
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException exception) {
			throw new IllegalStateException("Environment variable must be a valid long: " + name);
		}
	}
}
