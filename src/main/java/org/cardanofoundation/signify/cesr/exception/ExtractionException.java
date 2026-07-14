package org.cardanofoundation.signify.cesr.exception;

/**
 * Base class for errors extracting messages and attachments from a CESR stream —
 * raised when extracted data does not meet expectations.
 */
public class ExtractionException extends CesrException {

    public ExtractionException(String message) {
        super(message);
    }
}
