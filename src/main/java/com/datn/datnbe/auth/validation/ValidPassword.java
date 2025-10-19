package com.datn.datnbe.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a password meets the following criteria:
 * - At least 8 characters long
 * - Contains at least one special character
 * - Contains at least one number
 * - Contains at least one uppercase letter
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be at least 8 characters long, contain at least one uppercase letter, one number, and one special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
