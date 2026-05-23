package com.example.backend.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

	@Bean
	RSAPrivateKey jwtPrivateKey(@Value("${JWT_PRIVATE_KEY_BASE64}") String encodedPrivateKey) {
		try {
			byte[] decoded = Base64.getDecoder().decode(encodedPrivateKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
		} catch (Exception exception) {
			throw new IllegalStateException("Invalid JWT private key configuration.", exception);
		}
	}

	@Bean
	RSAPublicKey jwtPublicKey(@Value("${JWT_PUBLIC_KEY_BASE64}") String encodedPublicKey) {
		try {
			byte[] decoded = Base64.getDecoder().decode(encodedPublicKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
		} catch (Exception exception) {
			throw new IllegalStateException("Invalid JWT public key configuration.", exception);
		}
	}

	@Bean
	JwtEncoder jwtEncoder(RSAPrivateKey jwtPrivateKey, RSAPublicKey jwtPublicKey) {
		RSAKey rsaKey = new RSAKey.Builder(jwtPublicKey)
				.privateKey(jwtPrivateKey)
				.build();
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
		return new NimbusJwtEncoder(jwkSource);
	}

	@Bean
	JwtDecoder jwtDecoder(RSAPublicKey jwtPublicKey, @Value("${JWT_ISSUER}") String issuer) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder
				.withPublicKey(jwtPublicKey)
				.signatureAlgorithm(SignatureAlgorithm.RS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
		return decoder;
	}
}
