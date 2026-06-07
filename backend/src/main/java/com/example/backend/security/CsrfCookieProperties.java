package com.example.backend.security;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CsrfCookieProperties {

	private final String cookieName;
	private final String headerName;
	private final boolean secure;
	private final String sameSite;
	private final String path;

	public CsrfCookieProperties(Environment environment) {
		this.cookieName = readRequired(environment, "CSRF_COOKIE_NAME");
		this.headerName = readRequired(environment, "CSRF_HEADER_NAME");
		this.secure = readBoolean(environment, "CSRF_COOKIE_SECURE");
		this.sameSite = readRequired(environment, "CSRF_COOKIE_SAME_SITE");
		this.path = readRequired(environment, "CSRF_COOKIE_PATH");

		if (isProduction(environment.getProperty("APP_ENV")) && !secure) {
			throw new IllegalStateException("CSRF_COOKIE_SECURE must be true in production.");
		}
	}

	public String cookieName() {
		return cookieName;
	}

	public String headerName() {
		return headerName;
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
}
