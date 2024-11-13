package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid material value encountered during crypto material init
 * <p>
 * Usage: throw InvalidValueException("error message")
 */
public class InvalidValueException extends MaterialException {

    public InvalidValueException(String message) {
        super(message);
    }
}
