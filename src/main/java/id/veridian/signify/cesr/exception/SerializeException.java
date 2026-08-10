package org.cardanofoundation.signify.cesr.exception;

/**
 * Message creation or serialization error.
 */
public class SerializeException extends CesrException {

    public SerializeException(String message) {
        super(message);
    }
}
