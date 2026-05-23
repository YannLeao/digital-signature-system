package com.example.backend.security;

import com.example.backend.domain.JwtDenylistReason;
import com.example.backend.service.auth.RefreshTokenService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JwtLogoutService {

	private final JwtDenylistService jwtDenylistService;
	private final RefreshTokenService refreshTokenService;

	public JwtLogoutService(JwtDenylistService jwtDenylistService, RefreshTokenService refreshTokenService) {
		this.jwtDenylistService = jwtDenylistService;
		this.refreshTokenService = refreshTokenService;
	}

	@Transactional
	public void logout(Jwt jwt) {
		jwtDenylistService.denylist(jwt, JwtDenylistReason.LOGOUT);
		refreshTokenService.revokeSession(jwtDenylistService.sessionId(jwt));
	}
}
