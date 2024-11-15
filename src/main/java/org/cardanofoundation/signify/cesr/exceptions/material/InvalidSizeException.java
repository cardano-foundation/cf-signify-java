package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid size encountered during crypto material init
 * <p>
 * Usage: throw InvalidSizeException("error message")
 */
public class InvalidSizeException extends MaterialException {

    public InvalidSizeException(String message) {
        super(message);
    }
}
