package org.cardanofoundation.signify.cesr.exceptions.serialize;

import org.cardanofoundation.signify.cesr.exceptions.KeriException;

/**
 * Message creation and serialization errors
 * <p>
 * Usage: throw SerializeException("error message")
 */
public class SerializeException extends KeriException {

    public SerializeException(String message) {
        super(message);
    }
}
