package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Bad or Unsupported Message Type (Ilk)
 * <p>
 * Usage: throw IlkException("error message")
 */
public class IlkException extends ExtractionException {

    public IlkException(String message) {
        super(message);
    }
}
