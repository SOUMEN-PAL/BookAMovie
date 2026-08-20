package org.devbot.bookmymovie.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private int min;
    private int max;

    @Override
    public void initialize(ValidPassword annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        boolean valid = true;

        if (password == null || password.isBlank()) {
            addViolation(context, "Password must not be blank");
            return false;
        }

        if (password.length() < min) {
            addViolation(context, "Password must be at least " + min + " characters");
            valid = false;
        }

        if (password.length() > max) {
            addViolation(context, "Password must be at most " + max + " characters");
            valid = false;
        }

        if (!UPPERCASE.matcher(password).find()) {
            addViolation(context, "Password must contain at least one uppercase letter");
            valid = false;
        }

        if (!LOWERCASE.matcher(password).find()) {
            addViolation(context, "Password must contain at least one lowercase letter");
            valid = false;
        }

        if (!DIGIT.matcher(password).find()) {
            addViolation(context, "Password must contain at least one number");
            valid = false;
        }

        if (!SPECIAL.matcher(password).find()) {
            addViolation(context, "Password must contain at least one special character");
            valid = false;
        }

        return valid;
    }

    private static void addViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
