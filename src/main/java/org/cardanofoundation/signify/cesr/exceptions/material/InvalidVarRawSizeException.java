package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid raw size encountered during crypto material init
 * <p>
 * Usage: throw InvalidVarRawSizeException("error message")
 */
public class InvalidVarRawSizeException extends InvalidSizeException {

    public InvalidVarRawSizeException(String message) {
        super(message);
    }
}
