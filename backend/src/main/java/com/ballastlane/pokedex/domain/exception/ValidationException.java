package com.ballastlane.pokedex.domain.exception;

import java.util.Map;

/**
 * Raised when input violates a business rule. Carries field level errors.
 */
public class ValidationException extends DomainException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
