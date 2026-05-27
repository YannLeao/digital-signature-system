package com.example.backend.controller;

import com.example.backend.dto.auth.LoginResponse;
import com.example.backend.dto.passkey.PasskeyAuthenticationFinishRequest;
import com.example.backend.dto.passkey.PasskeyFinishRequest;
import com.example.backend.dto.passkey.PasskeyResponse;
import com.example.backend.dto.passkey.PasskeyStartRequest;
import com.example.backend.security.ClientContext;
import com.example.backend.security.RefreshTokenCookieFactory;
import com.example.backend.security.RefreshTokenResult;
import com.example.backend.service.PasskeyService;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/passkey")
@RequiredArgsConstructor
public class PasskeyController {

	private final PasskeyService passkeyService;
	private final RefreshTokenCookieFactory refreshTokenCookieFactory;

	@PostMapping("/register/start")
	public ResponseEntity<PublicKeyCredentialCreationOptions> startRegistration(
			@Valid @RequestBody PasskeyStartRequest request) {
		PublicKeyCredentialCreationOptions options = passkeyService.startRegistration(request.email());
		return ResponseEntity.ok(options);
	}

	@PostMapping("/register/finish")
	public ResponseEntity<Void> finishRegistration(
			@Valid @RequestBody PasskeyFinishRequest request) {
		passkeyService.finishRegistration(
				request.email(),
				request.credential(),
				request.deviceName()
		);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/devices")
	public ResponseEntity<List<PasskeyResponse>> getUserDevices(@RequestParam String email) {
		List<PasskeyResponse> devices = passkeyService.getUserPasskeys(email);
		return ResponseEntity.ok(devices);
	}

	@DeleteMapping("/device/{id}")
	public ResponseEntity<Void> deleteDevice(
			@PathVariable Long id,
			@RequestParam String email) {
		passkeyService.deletePasskey(id, email);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/start")
	public ResponseEntity<PublicKeyCredentialRequestOptions> startAuthentication(
			@Valid @RequestBody PasskeyStartRequest request) {
		PublicKeyCredentialRequestOptions options = passkeyService.startAuthentication(request.email());
		return ResponseEntity.ok(options);
	}

	@PostMapping("/auth/finish")
	public ResponseEntity<LoginResponse> finishAuthentication(
			@Valid @RequestBody PasskeyAuthenticationFinishRequest request,
			HttpServletRequest httpRequest) {
		RefreshTokenResult result = passkeyService.finishAuthentication(
				request.email(),
				request.credential(),
				new ClientContext(httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
		);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(result.refreshToken()).toString())
				.body(new LoginResponse(
						result.accessToken().token(),
						result.accessToken().tokenType(),
						result.accessToken().expiresIn()
				));
	}
}
