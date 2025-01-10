package org.cardanofoundation.signify.app.clienting.exception;

/**
 * Signed Header verification errors
 * <p>
 * Usage: throw HeaderVerificationException("error message")
 */
public class HeaderVerificationException extends ClientingException{

    public HeaderVerificationException(String message) {
        super(message);
    }
}
