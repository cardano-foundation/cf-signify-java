package org.cardanofoundation.signify.cesr.exception;

/**
 * Count code start character "-" encountered unexpectedly.
 */
public class UnexpectedCountCodeException extends DerivationCodeException {

    public UnexpectedCountCodeException(String message) {
        super(message);
    }
}
