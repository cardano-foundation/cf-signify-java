package id.veridian.signify.cesr.exceptions.serialize;

import id.veridian.signify.cesr.exceptions.KeriException;

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
