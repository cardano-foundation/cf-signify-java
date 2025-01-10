package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Unexpected or unknown or unsupported derivation code during extraction
 * <p>
 * Usage: throw UnexpectedCodeException("error message")
 */
public class UnexpectedCodeException extends ExtractionException {

    public UnexpectedCodeException(String message) {
        super(message);
    }
}
