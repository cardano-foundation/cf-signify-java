package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid, Unknown, or unrecognized code encountered during crypto material init
 * <p>
 * Usage: throw InvalidCodeException("error message")
 */
public class InvalidCodeException extends MaterialException {

    public InvalidCodeException(String message) {
        super(message);
    }
}
