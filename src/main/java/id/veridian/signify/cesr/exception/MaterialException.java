package org.cardanofoundation.signify.cesr.exception;

/**
 * Base class for errors initializing a cryptographic material primitive.
 */
public class MaterialException extends CesrException {

    public MaterialException(String message) {
        super(message);
    }
}
