package org.cardanofoundation.signify.app.clienting.exception;

/**
 *  Base Class for clienting exceptions
 *  <p>
 *  To use throw ClientingException("Error: message")
 */
public class ClientingException extends RuntimeException {
    private final String message;

    public ClientingException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
