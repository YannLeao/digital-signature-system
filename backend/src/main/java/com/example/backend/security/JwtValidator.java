package com.example.backend.security;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.beans.factory.annotation.Autowired;
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
			"token_use",
			"ip",
			"ua_hash"
	);

	private static final List<String> REQUIRED_HALF_SESSION_CLAIMS = List.of(
			"sub",
			"jti",
			"iat",
			"exp",
			"session_id",
			"token_use",
			"scope",
			"ip",
			"ua_hash"
	);

	private final JwtDecoder jwtDecoder;
	private final JwtDenylistService jwtDenylistService;

	@Autowired
	public JwtValidator(JwtDecoder jwtDecoder, JwtDenylistService jwtDenylistService) {
		this.jwtDecoder = jwtDecoder;
		this.jwtDenylistService = jwtDenylistService;
	}

	JwtValidator(JwtDecoder jwtDecoder) {
		this.jwtDecoder = jwtDecoder;
		this.jwtDenylistService = null;
	}

	public Jwt validate(String token) {
		return validateAccessToken(token);
	}

	public Jwt validateAccessToken(String token) {
		Jwt jwt = jwtDecoder.decode(token);
		validateRequiredClaims(jwt, REQUIRED_CLAIMS);
		validateClaim(jwt, "token_use", JwtClaimsFactory.TOKEN_USE_ACCESS);
		validateDenylist(jwt);
		return jwt;
	}

	public Jwt validateTotpChallengeToken(String token) {
		Jwt jwt = jwtDecoder.decode(token);
		validateRequiredClaims(jwt, REQUIRED_HALF_SESSION_CLAIMS);
		validateClaim(jwt, "token_use", JwtClaimsFactory.TOKEN_USE_TOTP_CHALLENGE);
		validateClaim(jwt, "scope", JwtClaimsFactory.SCOPE_TOTP_VERIFY);
		validateDenylist(jwt);
		return jwt;
	}

	private void validateRequiredClaims(Jwt jwt, List<String> requiredClaims) {
		for (String claim : requiredClaims) {
			Object value = jwt.getClaims().get(claim);
			if (value == null || value.toString().isBlank()) {
				throw new BadJwtException("JWT missing required claim: " + claim);
			}
		}
	}

	private void validateClaim(Jwt jwt, String claim, String expectedValue) {
		String value = jwt.getClaimAsString(claim);
		if (!expectedValue.equals(value)) {
			throw new BadJwtException("JWT has invalid " + claim + ".");
		}
	}

	private void validateDenylist(Jwt jwt) {
		if (jwtDenylistService != null && jwtDenylistService.isDenylisted(jwt.getId())) {
			throw new BadJwtException("JWT has been revoked.");
		}
	}
}
