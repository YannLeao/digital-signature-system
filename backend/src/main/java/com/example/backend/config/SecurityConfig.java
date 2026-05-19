package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	private static final int ARGON2_SALT_LENGTH = 16;
	private static final int ARGON2_HASH_LENGTH = 32;
	private static final int ARGON2_PARALLELISM = 4;
	private static final int ARGON2_MEMORY = 65536;
	private static final int ARGON2_ITERATIONS = 3;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.ignoringRequestMatchers("/auth/register", "/api/v1/auth/register"))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/health", "/api/v1/health", "/auth/register", "/api/v1/auth/register").permitAll()
						.anyRequest().authenticated()
				)
				.httpBasic(Customizer.withDefaults())
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
