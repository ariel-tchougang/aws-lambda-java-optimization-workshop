package com.atn.digital.user.domain.validation;

import com.atn.digital.user.domain.exceptions.ConstraintViolationException;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    private ValidationUtils() {}

    public static void validateNotNullNotBlank(String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.isBlank()) {
            throw new ConstraintViolationException(fieldName + " expected to be not null and not blank");
        }
    }

    public static void validateEmail(String fieldName, String fieldValue) {
        if (!Pattern.compile(EMAIL_REGEX).matcher(fieldValue).matches()) {
            throw new ConstraintViolationException(fieldName + " expected to be a valid email");
        }
    }
}
