package org.cardanofoundation.signify.cesr.exceptions.extraction;


/**
 * Problem with Base64 to Binary conversion
 * <p>
 * Usage: throw ConversionException("error message")
 */
public class ConversionException extends ExtractionException {

    public ConversionException(String message) {
        super(message);
    }
}
