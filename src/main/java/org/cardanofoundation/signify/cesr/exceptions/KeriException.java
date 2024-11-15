package org.cardanofoundation.signify.cesr.exceptions;

/**
 *  Base Class for keri exceptions
 *  <p>
 *  To use throw KeriException("Error: message")
 */
public class KeriException extends RuntimeException {
    private final String message;

    public KeriException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
