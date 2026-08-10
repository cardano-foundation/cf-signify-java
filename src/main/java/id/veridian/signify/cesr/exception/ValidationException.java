package org.cardanofoundation.signify.cesr.exception;

/**
 * Message or attachment validation error.
 */
public class ValidationException extends CesrException {

    public ValidationException(String message) {
        super(message);
    }
}
