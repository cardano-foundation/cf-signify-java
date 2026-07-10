package org.cardanofoundation.signify.cesr.exception;

/**
 * Bad or unsupported serialization kind.
 */
public class KindException extends ExtractionException {

    public KindException(String message) {
        super(message);
    }
}
