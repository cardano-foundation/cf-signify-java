package id.veridian.signify.cesr.exceptions.validation;

import id.veridian.signify.cesr.exceptions.KeriException;

/**
 * Validation related errors
 * Usage:
 * throw new ValidationException("error message")
 */
public class ValidationException extends KeriException {
    
    public ValidationException(String message) {
        super(message);
    }
}
