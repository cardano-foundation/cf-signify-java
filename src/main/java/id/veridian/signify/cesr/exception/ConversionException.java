package org.cardanofoundation.signify.cesr.exception;

/**
 * Base64 to binary conversion failed during extraction.
 */
public class ConversionException extends ExtractionException {

    public ConversionException(String message) {
        super(message);
    }
}
