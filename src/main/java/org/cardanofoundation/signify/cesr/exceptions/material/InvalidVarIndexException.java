package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid code index encountered during crypto material init
 * <p>
 * Usage: throw InvalidVarIndexException("error message")
 */
public class InvalidVarIndexException extends InvalidSizeException {

    public InvalidVarIndexException(String message) {
        super(message);
    }

}
