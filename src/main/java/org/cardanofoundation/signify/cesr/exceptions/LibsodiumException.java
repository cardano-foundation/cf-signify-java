package org.cardanofoundation.signify.cesr.exceptions;

/**
 *  Used to re-throw SodiumException("Error: message")
 */
public class LibsodiumException extends RuntimeException {

    public LibsodiumException(String message) {
        super(message);
    }

    public LibsodiumException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibsodiumException(Throwable cause) {
        super(cause);
    }
}
