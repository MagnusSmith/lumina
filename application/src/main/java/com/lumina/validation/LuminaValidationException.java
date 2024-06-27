package com.lumina.validation;

import jakarta.validation.ValidationException;
import java.util.Set;

public class LuminaValidationException extends ValidationException {
    private final Errors errors;

    public LuminaValidationException(Errors errors) {
        this.errors = errors;
    }

    public Set<Error> validationErrors() {
        return errors.fieldErrors();
    }
}
