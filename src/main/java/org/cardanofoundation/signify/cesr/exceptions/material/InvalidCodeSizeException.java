package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid code size encountered during crypto material init
 * <p>
 * Usage: throw InvalidCodeSizeException("error message")
 */
public class InvalidCodeSizeException extends InvalidSizeException {

    public InvalidCodeSizeException(String message) {
        super(message);
    }
}
