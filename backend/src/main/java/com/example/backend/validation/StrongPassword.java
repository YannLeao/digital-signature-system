package com.example.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {

	String message() default "Senha deve ter no minimo 12 caracteres, letra maiuscula, letra minuscula, " +
			"numero e simbolo especial.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
