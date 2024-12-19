package org.cardanofoundation.signify.app.clienting.exception;

/**
 * Unexpected Response Status Exception
 * <p>
 * Usage: throw UnexpectedResponseStatusException("error message")
 */
public class UnexpectedResponseStatusException extends ClientingException{
    public UnexpectedResponseStatusException(String message) {
        super(message);
    }
}
