package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Encountered count code start char "-" unexpectantly
 * <p>
 * Usage: throw UnexpectedCountCodeException("error message")
 */
public class UnexpectedCountCodeException extends DerivationCodeException {

    public UnexpectedCountCodeException(String message) {
        super(message);
    }
}
