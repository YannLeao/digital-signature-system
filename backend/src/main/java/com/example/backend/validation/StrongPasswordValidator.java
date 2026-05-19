package com.example.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

	private static final int MIN_LENGTH = 12;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.length() < MIN_LENGTH) {
			return false;
		}

		boolean hasUppercase = false;
		boolean hasLowercase = false;
		boolean hasDigit = false;
		boolean hasSpecial = false;

		for (int index = 0; index < value.length(); index++) {
			char current = value.charAt(index);

			if (Character.isUpperCase(current)) {
				hasUppercase = true;
			} else if (Character.isLowerCase(current)) {
				hasLowercase = true;
			} else if (Character.isDigit(current)) {
				hasDigit = true;
			} else {
				hasSpecial = true;
			}
		}

		return hasUppercase && hasLowercase && hasDigit && hasSpecial;
	}
}
