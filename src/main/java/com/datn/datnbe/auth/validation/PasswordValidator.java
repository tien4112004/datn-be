package com.datn.datnbe.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for {@link ValidPassword} annotation.
 *
 * Validates that a password meets the following criteria:
 * - At least 8 characters long
 * - Contains at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
 * - Contains at least one number (0-9)
 * - Contains at least one uppercase letter (A-Z)
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;

    // Pattern to check for at least one uppercase letter
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");

    // Pattern to check for at least one digit
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");

    // Pattern to check for at least one special character
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Null values are considered valid (use @NotNull for null checks)
        if (password == null) {
            return true;
        }

        // Check minimum length
        if (password.length() < MIN_LENGTH) {
            buildConstraintViolation(context, "Password must be at least 8 characters long");
            return false;
        }

        // Check for at least one uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            buildConstraintViolation(context, "Password must contain at least one uppercase letter");
            return false;
        }

        // Check for at least one digit
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            buildConstraintViolation(context, "Password must contain at least one number");
            return false;
        }

        // Check for at least one special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            buildConstraintViolation(context,
                    "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)");
            return false;
        }

        return true;
    }

    /**
     * Builds a custom constraint violation message
     */
    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
