package com.example.backend.config;

import com.example.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private static final String CONTENT_SECURITY_POLICY = "default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none';";
	private static final String STRICT_TRANSPORT_SECURITY = "max-age=63072000; includeSubDomains; preload";
	private static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=()";

	private static final int ARGON2_SALT_LENGTH = 16;
	private static final int ARGON2_HASH_LENGTH = 32;
	private static final int ARGON2_PARALLELISM = 4;
	private static final int ARGON2_MEMORY = 65536;
	private static final int ARGON2_ITERATIONS = 3;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
		return http
				.cors(Customizer.withDefaults())
				.headers(headers -> headers
						.contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
						.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable)
						.addHeaderWriter(new StaticHeadersWriter("Strict-Transport-Security", STRICT_TRANSPORT_SECURITY))
						.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
						.contentTypeOptions(Customizer.withDefaults())
						.referrerPolicy(referrerPolicy -> referrerPolicy
								.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
						)
						.permissionsPolicyHeader(permissionsPolicy -> permissionsPolicy.policy(PERMISSIONS_POLICY))
						.cacheControl(Customizer.withDefaults())
						.xssProtection(xssProtection -> xssProtection
								.headerValue(XXssProtectionHeaderWriter.HeaderValue.DISABLED)
						)
				)
				.csrf(csrf -> csrf.ignoringRequestMatchers(
						"/auth/register",
						"/api/v1/auth/register",
						"/auth/login",
						"/api/v1/auth/login",
						"/auth/refresh",
						"/api/v1/auth/refresh",
						"/auth/logout",
						"/api/v1/auth/logout",
						"/auth/passkey/register/start",
						"/api/v1/auth/passkey/register/start",
						"/auth/passkey/register/finish",
						"/api/v1/auth/passkey/register/finish"
				))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(
								"/health",
								"/api/v1/health",
								"/auth/register",
								"/api/v1/auth/register",
								"/auth/login",
								"/api/v1/auth/login",
								"/auth/refresh",
								"/api/v1/auth/refresh",
								"/auth/passkey/register/start",
								"/api/v1/auth/passkey/register/start",
								"/auth/passkey/register/finish",
								"/api/v1/auth/passkey/register/finish"
						).permitAll()
						.anyRequest().authenticated()
				)
				.httpBasic(Customizer.withDefaults())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new Argon2PasswordEncoder(
				ARGON2_SALT_LENGTH,
				ARGON2_HASH_LENGTH,
				ARGON2_PARALLELISM,
				ARGON2_MEMORY,
				ARGON2_ITERATIONS
		);
	}
}
