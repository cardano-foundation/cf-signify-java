package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Bad or Unsupported Protocol type
 * <p>
 * Usage: throw ProtocolException("error message")
 */
public class ProtocolException extends ExtractionException {

    public ProtocolException(String message) {
        super(message);
    }
}
