package com.example.backend.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void returnsFieldErrorsForBeanValidationFailures() throws Exception {
		String response = mockMvc.perform(post("/test/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"invalid","name":""}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VAL_001"))
				.andExpect(jsonPath("$.message").value("Dados invalidos."))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.fields.length()").value(2))
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(response).doesNotContain("stackTrace", "exception", "org.springframework");
	}

	@Test
	void returnsNotFoundResponseForMissingResources() throws Exception {
		mockMvc.perform(get("/test/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("SYS_002"))
				.andExpect(jsonPath("$.message").value("Recurso nao encontrado."))
				.andExpect(jsonPath("$.fields").doesNotExist());
	}

	@Test
	void returnsConflictResponseForBusinessRules() throws Exception {
		mockMvc.perform(get("/test/business"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("VAL_003"))
				.andExpect(jsonPath("$.message").value("Operacao nao permitida."));
	}

	@Test
	void returnsSafeResponseForUnexpectedErrors() throws Exception {
		String response = mockMvc.perform(get("/test/unexpected"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("SYS_001"))
				.andExpect(jsonPath("$.message").value("Erro interno inesperado."))
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode json = objectMapper.readTree(response);

		assertThat(json.has("fields")).isFalse();
		assertThat(response).doesNotContain("select * from users", "IllegalStateException", "stackTrace");
	}

	@RestController
	@RequestMapping("/test")
	static class TestController {

		@PostMapping("/validation")
		void validation(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/not-found")
		void notFound() {
			throw new ResourceNotFoundException();
		}

		@GetMapping("/business")
		void business() {
			throw new BusinessException("Operacao nao permitida.");
		}

		@GetMapping("/unexpected")
		void unexpected() {
			throw new IllegalStateException("select * from users where password_hash = 'secret'");
		}
	}

	record TestRequest(
			@Email(message = "E-mail invalido.")
			String email,

			@NotBlank(message = "Nome obrigatorio.")
			String name
	) {
	}
}
