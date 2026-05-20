package com.example.backend.controller;

import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.PasskeyService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasskeyControllerTests {

	private final PasskeyService passkeyService = mock(PasskeyService.class);
	private final MockMvc mockMvc = MockMvcBuilders
			.standaloneSetup(new PasskeyController(passkeyService))
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
}
