package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid variable size encountered during crypto material init
 * <p>
 * Usage: throw InvalidVarSizeException("error message")
 */
public class InvalidVarSizeException extends InvalidSizeException {

    public InvalidVarSizeException(String message) {
        super(message);
    }
}
