package com.example.backend.controller;

import com.example.backend.security.AccessToken;
import com.example.backend.security.RefreshTokenCookieFactory;
import com.example.backend.security.RefreshTokenResult;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.PasskeyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasskeyControllerTests {

	private final PasskeyService passkeyService = mock(PasskeyService.class);
	private final RefreshTokenCookieFactory refreshTokenCookieFactory = mock(RefreshTokenCookieFactory.class);
	private final MockMvc mockMvc = MockMvcBuilders
			.standaloneSetup(new PasskeyController(passkeyService, refreshTokenCookieFactory))
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();

	@Test
	void rejectsInvalidStartRequest() throws Exception {
		mockMvc.perform(post("/auth/passkey/register/start")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"invalid-email"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field").value("email"));
	}

	@Test
	void delegatesStartRegistrationWithoutSensitiveCredentialResponse() throws Exception {
		mockMvc.perform(post("/auth/passkey/register/start")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").doesNotExist());

		verify(passkeyService).startRegistration("user@example.com");
	}

	@Test
	void rejectsFinishRequestWhenCredentialIsMissing() throws Exception {
		mockMvc.perform(post("/auth/passkey/register/finish")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","credential":"","deviceName":"Notebook"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field", containsString("credential")));
	}

	@Test
	void rejectsAuthenticationFinishRequestWhenCredentialIsMissing() throws Exception {
		mockMvc.perform(post("/auth/passkey/auth/finish")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","credential":""}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field", containsString("credential")));
	}

	@Test
	void finishAuthenticationReturnsAccessTokenAndHttpOnlyRefreshCookie() throws Exception {
		when(passkeyService.finishAuthentication(eq("user@example.com"), eq("{}"), any()))
				.thenReturn(new RefreshTokenResult(new AccessToken("jwt-token", "Bearer", 900), "refresh-token"));
		when(refreshTokenCookieFactory.create("refresh-token")).thenReturn(ResponseCookie.from("refresh_token", "refresh-token")
				.httpOnly(true)
				.secure(false)
				.sameSite("Strict")
				.path("/api/v1/auth/refresh")
				.maxAge(604800)
				.build());

		mockMvc.perform(post("/auth/passkey/auth/finish")
						.with(request -> {
							request.setRemoteAddr("203.0.113.10");
							return request;
						})
						.header("User-Agent", "JUnit/5")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","credential":"{}"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andExpect(header().string("Set-Cookie", containsString("refresh_token=refresh-token")))
				.andExpect(header().string("Set-Cookie", containsString("HttpOnly")));
	}
}
