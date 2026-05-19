package com.example.backend.controller.auth;

import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.dto.auth.RegisterUserResponse;
import com.example.backend.service.auth.UserRegistrationService;
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

	public AuthController(UserRegistrationService userRegistrationService) {
		this.userRegistrationService = userRegistrationService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
		userRegistrationService.register(request);
		return new RegisterUserResponse("Usuario registrado com sucesso.");
	}
}
