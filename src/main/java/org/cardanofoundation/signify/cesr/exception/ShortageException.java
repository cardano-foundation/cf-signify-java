package org.cardanofoundation.signify.cesr.exception;

/**
 * Not enough bytes in the buffer for a complete message or primitive.
 */
public class ShortageException extends ExtractionException {

    public ShortageException(String message) {
        super(message);
    }
}
