package org.cardanofoundation.signify.cesr.exception;

/**
 * Op code start character "_" encountered unexpectedly.
 */
public class UnexpectedOpCodeException extends DerivationCodeException {

    public UnexpectedOpCodeException(String message) {
        super(message);
    }
}
