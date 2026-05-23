package com.example.backend.controller.auth;

import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.domain.User;
import com.example.backend.exception.AuthenticationFailedException;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.exception.RateLimitExceededException;
import com.example.backend.security.AccessToken;
import com.example.backend.security.ClientContext;
import com.example.backend.security.JwtService;
import com.example.backend.service.auth.LoginRateLimiter;
import com.example.backend.service.auth.UserLoginService;
import com.example.backend.service.auth.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTests {

	private UserRegistrationService userRegistrationService;
	private UserLoginService userLoginService;
	private LoginRateLimiter loginRateLimiter;
	private JwtService jwtService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		userRegistrationService = mock(UserRegistrationService.class);
		userLoginService = mock(UserLoginService.class);
		loginRateLimiter = mock(LoginRateLimiter.class);
		jwtService = mock(JwtService.class);

		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(
						userRegistrationService,
						userLoginService,
						loginRateLimiter,
						jwtService
				))
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

	@Test
	void logsInValidUserWithoutReturningPasswordOrHash() throws Exception {
		String password = "StrongPassword123!";
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				Instant.parse("2026-05-22T12:00:00Z")
		);
		when(userLoginService.login(new LoginRequest("user@example.com", password))).thenReturn(user);
		when(jwtService.issueAccessToken(eq(user), eq(new ClientContext("203.0.113.10", "JUnit/5"))))
				.thenReturn(new AccessToken("jwt-token", "Bearer", 900));

		String response = mockMvc.perform(post("/auth/login")
						.with(request -> {
							request.setRemoteAddr("203.0.113.10");
							return request;
						})
						.header("User-Agent", "JUnit/5")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(response).doesNotContain(password, "password", "passwordHash", "$argon2id$");
		verify(loginRateLimiter).consume("203.0.113.10");
		verify(userLoginService).login(new LoginRequest("user@example.com", password));
		verify(jwtService).issueAccessToken(user, new ClientContext("203.0.113.10", "JUnit/5"));
	}

	@Test
	void returnsGenericAuthenticationErrorForLoginFailure() throws Exception {
		doThrow(new AuthenticationFailedException())
				.when(userLoginService)
				.login(new LoginRequest("user@example.com", "WrongPassword123!"));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"WrongPassword123!"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTH_001"))
				.andExpect(jsonPath("$.message").value("Credenciais invalidas."));

		verify(jwtService, never()).issueAccessToken(any(), any());
	}

	@Test
	void returnsTooManyRequestsWhenLoginRateLimitIsExceeded() throws Exception {
		doThrow(new RateLimitExceededException())
				.when(loginRateLimiter)
				.consume("127.0.0.1");

		mockMvc.perform(post("/auth/login")
						.with(request -> {
							request.setRemoteAddr("127.0.0.1");
							return request;
						})
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@example.com","password":"StrongPassword123!"}
								"""))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.code").value("SEC_001"));
	}
}
