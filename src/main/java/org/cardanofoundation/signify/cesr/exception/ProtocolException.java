package org.cardanofoundation.signify.cesr.exception;

/**
 * Bad or unsupported protocol type.
 */
public class ProtocolException extends ExtractionException {

    public ProtocolException(String message) {
        super(message);
    }
}
