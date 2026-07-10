package org.cardanofoundation.signify.cesr.exception;

/**
 * Empty or missing cryptographic material.
 */
public class EmptyMaterialException extends MaterialException {

    public EmptyMaterialException(String message) {
        super(message);
    }
}
