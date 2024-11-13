package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Invalid raw material
 * <p>
 * Usage: throw RawMaterialException("error message")
 */
public class RawMaterialException extends MaterialException {

    public RawMaterialException(String message) {
        super(message);
    }
}
