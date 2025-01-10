package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Not Enough bytes in buffer for complete message or material
 * <p>
 * Usage: throw ShortageException("error message")
 */
public class ShortageException extends ExtractionException {

    public ShortageException(String message) {
        super(message);
    }
}
