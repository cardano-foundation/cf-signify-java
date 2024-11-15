package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Derivation Code crypto material conversion errors
 * <p>
 * Usage: throw DerivationCodeException("error message")
 */
public class DerivationCodeException extends ExtractionException {

    public DerivationCodeException(String message) {
        super(message);
    }
}
