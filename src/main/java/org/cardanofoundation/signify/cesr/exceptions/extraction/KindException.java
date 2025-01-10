package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Bad or Unsupported Serialization Kind
 * <p>
 * Usage: throw KindException("error message")
 */
public class KindException extends ExtractionException {

    public KindException(String message) {
        super(message);
    }
}
