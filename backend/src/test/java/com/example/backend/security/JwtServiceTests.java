package com.example.backend.security;

import com.example.backend.domain.User;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {

	private static final String ISSUER = "projeto-3-seguranca-test";
	private static final Instant NOW = Instant.now().minusSeconds(60).truncatedTo(ChronoUnit.SECONDS);
	private static final ClientContext CLIENT_CONTEXT = new ClientContext("203.0.113.10", "JUnit/5");

	@Test
	void issuesAndValidatesRs256TokenWithRequiredClaims() {
		KeyPair keyPair = keyPair();
		JwtService service = jwtService(keyPair, Clock.fixed(NOW, ZoneOffset.UTC));
		JwtValidator validator = jwtValidator(keyPair);
		User user = user();

		AccessToken accessToken = service.issueAccessToken(user, CLIENT_CONTEXT);
		Jwt jwt = validator.validate(accessToken.token());

		assertThat(accessToken.tokenType()).isEqualTo("Bearer");
		assertThat(accessToken.expiresIn()).isEqualTo(900);
		assertThat(jwt.getHeaders()).containsEntry("alg", "RS256");
		assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
		assertThat(jwt.getId()).isNotBlank();
		assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
		assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
		assertThat(jwt.getClaimAsString("session_id")).isNotBlank();
		assertThat(jwt.getClaimAsString("ip")).isEqualTo("631f08140b24b7274d12df3c37a1a80ce5876dafd7007d772e0114fddf88b682");
		assertThat(jwt.getClaimAsString("ua_hash")).isEqualTo("ea2ca7aa052e3b590ab76bf113f8b50894d5c697081fc789f41e6551b9b2df50");
		assertThat(jwt.getClaimAsString("ip")).doesNotContain(CLIENT_CONTEXT.ipAddress());
		assertThat(jwt.getClaimAsString("ua_hash")).doesNotContain(CLIENT_CONTEXT.userAgent());
	}

	@Test
	void rejectsExpiredToken() {
		KeyPair keyPair = keyPair();
		JwtService service = jwtService(keyPair, Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC));
		JwtValidator validator = jwtValidator(keyPair);

		AccessToken accessToken = service.issueAccessToken(user(), CLIENT_CONTEXT);

		assertThatThrownBy(() -> validator.validate(accessToken.token()))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void rejectsTokenSignedWithDifferentKey() {
		JwtService service = jwtService(keyPair(), Clock.fixed(NOW, ZoneOffset.UTC));
		JwtValidator validator = jwtValidator(keyPair());

		AccessToken accessToken = service.issueAccessToken(user(), CLIENT_CONTEXT);

		assertThatThrownBy(() -> validator.validate(accessToken.token()))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void rejectsUnsignedToken() {
		JwtValidator validator = jwtValidator(keyPair());
		String unsignedToken = encodeJson("{\"alg\":\"none\",\"typ\":\"JWT\"}")
				+ "."
				+ encodeJson("{\"sub\":\"user\",\"jti\":\"token\",\"iat\":1,\"exp\":9999999999,\"session_id\":\"session\",\"ip\":\"ip\",\"ua_hash\":\"ua\"}")
				+ ".";

		assertThatThrownBy(() -> validator.validate(unsignedToken))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void rejectsTamperedToken() {
		KeyPair keyPair = keyPair();
		JwtService service = jwtService(keyPair, Clock.fixed(NOW, ZoneOffset.UTC));
		JwtValidator validator = jwtValidator(keyPair);
		AccessToken accessToken = service.issueAccessToken(user(), CLIENT_CONTEXT);
		String tampered = accessToken.token().substring(0, accessToken.token().length() - 2) + "xx";

		assertThatThrownBy(() -> validator.validate(tampered))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void rejectsTokenWithoutRequiredClaim() {
		KeyPair keyPair = keyPair();
		JwtService service = jwtService(keyPair, Clock.fixed(NOW, ZoneOffset.UTC));
		JwtValidator validator = jwtValidator(keyPair);
		AccessToken accessToken = service.issueAccessToken(user(), new ClientContext("203.0.113.10", null));
		Jwt jwt = validator.validate(accessToken.token());

		assertThat(jwt.getClaimAsString("ua_hash")).isNotBlank();
		assertThatThrownBy(() -> new JwtValidator(token -> jwtWithoutClaim(jwt, "ua_hash")).validate(accessToken.token()))
				.isInstanceOf(BadJwtException.class)
				.hasMessage("JWT missing required claim: ua_hash");
	}

	private JwtService jwtService(KeyPair keyPair, Clock clock) {
		return new JwtService(jwtEncoder(keyPair), new JwtClaimsFactory(clock, ISSUER));
	}

	private JwtValidator jwtValidator(KeyPair keyPair) {
		return new JwtValidator(jwtDecoder(keyPair));
	}

	private JwtEncoder jwtEncoder(KeyPair keyPair) {
		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate())
				.build();
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
		return new NimbusJwtEncoder(jwkSource);
	}

	private JwtDecoder jwtDecoder(KeyPair keyPair) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder
				.withPublicKey((RSAPublicKey) keyPair.getPublic())
				.signatureAlgorithm(SignatureAlgorithm.RS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
		return decoder;
	}

	private KeyPair keyPair() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			return generator.generateKeyPair();
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to generate test RSA key pair.", exception);
		}
	}

	private User user() {
		return User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				NOW.minusSeconds(3600)
		);
	}

	private Jwt jwtWithoutClaim(Jwt jwt, String claim) {
		var claims = new LinkedHashMap<>(jwt.getClaims());
		claims.remove(claim);
		return new Jwt(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getHeaders(), claims);
	}

	private String encodeJson(String json) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
}
