package com.example.backend.security;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtValidator {

	private static final List<String> REQUIRED_CLAIMS = List.of(
			"sub",
			"jti",
			"iat",
			"exp",
			"session_id",
			"ip",
			"ua_hash"
	);

	private final JwtDecoder jwtDecoder;

	public JwtValidator(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
	}

	public Jwt validate(String token) {
		Jwt jwt = jwtDecoder.decode(token);
		validateRequiredClaims(jwt);
		return jwt;
	}

	private void validateRequiredClaims(Jwt jwt) {
		for (String claim : REQUIRED_CLAIMS) {
			Object value = jwt.getClaims().get(claim);
			if (value == null || value.toString().isBlank()) {
				throw new BadJwtException("JWT missing required claim: " + claim);
			}
		}
	}
}
