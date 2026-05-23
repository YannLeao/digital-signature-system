package com.example.backend.security;

import com.example.backend.domain.User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final String TOKEN_TYPE = "Bearer";

	private final JwtEncoder jwtEncoder;
	private final JwtClaimsFactory jwtClaimsFactory;

	public JwtService(JwtEncoder jwtEncoder, JwtClaimsFactory jwtClaimsFactory) {
		this.jwtEncoder = jwtEncoder;
		this.jwtClaimsFactory = jwtClaimsFactory;
	}

	public AccessToken issueAccessToken(User user, ClientContext clientContext) {
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
		JwtClaimsSet claims = jwtClaimsFactory.createAccessTokenClaims(user, clientContext);
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

		return new AccessToken(token, TOKEN_TYPE, jwtClaimsFactory.accessTokenExpiresInSeconds());
	}
}
