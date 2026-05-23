package com.example.backend.controller.auth;

import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.LoginResponse;
import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.dto.auth.RegisterUserResponse;
import com.example.backend.domain.User;
import com.example.backend.security.AccessToken;
import com.example.backend.security.ClientContext;
import com.example.backend.security.JwtService;
import com.example.backend.service.auth.LoginRateLimiter;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.auth.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UserRegistrationService userRegistrationService;
	private final UserLoginService userLoginService;
	private final LoginRateLimiter loginRateLimiter;
	private final JwtService jwtService;

	public AuthController(
			UserRegistrationService userRegistrationService,
			UserLoginService userLoginService,
			LoginRateLimiter loginRateLimiter,
			JwtService jwtService
	) {
		this.userRegistrationService = userRegistrationService;
		this.userLoginService = userLoginService;
		this.loginRateLimiter = loginRateLimiter;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
		userRegistrationService.register(request);
		return new RegisterUserResponse("Usuario registrado com sucesso.");
	}

	@PostMapping("/login")
	LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
		loginRateLimiter.consume(servletRequest.getRemoteAddr());
		User user = userLoginService.login(request);
		AccessToken accessToken = jwtService.issueAccessToken(user, clientContext(servletRequest));
		return new LoginResponse(accessToken.token(), accessToken.tokenType(), accessToken.expiresIn());
	}

	private ClientContext clientContext(HttpServletRequest request) {
		return new ClientContext(request.getRemoteAddr(), request.getHeader("User-Agent"));
	}
}
