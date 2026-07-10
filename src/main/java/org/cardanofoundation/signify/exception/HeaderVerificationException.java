package org.cardanofoundation.signify.exception;

/**
 * The signed headers of an agent response failed verification, or the response came
 * from a different remote agent than expected. Security-relevant — never blind-retry.
 */
public class HeaderVerificationException extends SignifyException {

    public HeaderVerificationException(String message) {
        super(message);
    }
}
