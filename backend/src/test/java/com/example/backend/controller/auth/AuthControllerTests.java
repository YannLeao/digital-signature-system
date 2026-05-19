package com.example.backend.controller.auth;

import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.auth.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTests {

	private UserRegistrationService userRegistrationService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		userRegistrationService = mock(UserRegistrationService.class);

		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userRegistrationService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void registersValidUserWithoutReturningPassword() throws Exception {
		String password = "StrongPassword123!";

		String response = mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("Usuario registrado com sucesso."))
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(response).doesNotContain(password, "password", "passwordHash");
		verify(userRegistrationService).register(new RegisterUserRequest("user@example.com", password));
	}

	@Test
	void rejectsInvalidEmailWithGlobalValidationError() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"invalid","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field").value("email"));
	}

	@Test
	void rejectsWeakPasswordWithoutNumberWithGlobalValidationError() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword!"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field").value("password"));
	}

	@Test
	void rejectsWeakPasswordWithoutSymbolWithGlobalValidationError() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field").value("password"));
	}

	@Test
	void rejectsWeakPasswordWithoutUppercaseWithGlobalValidationError() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"strongpassword123!"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field").value("password"));
	}

	@Test
	void rejectsShortPasswordWithGlobalValidationError() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"Short1!"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.fields[0].field").value("password"));
	}

	@Test
	void returnsControlledErrorForDuplicateEmail() throws Exception {
		doThrow(new BusinessException("E-mail ja cadastrado."))
				.when(userRegistrationService)
				.register(new RegisterUserRequest("user@example.com", "StrongPassword123!"));

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("VAL_003"))
				.andExpect(jsonPath("$.message").value("E-mail ja cadastrado."));
	}
}
