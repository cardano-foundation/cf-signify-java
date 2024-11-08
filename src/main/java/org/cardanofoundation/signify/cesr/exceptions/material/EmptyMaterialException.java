package org.cardanofoundation.signify.cesr.exceptions.material;

/**
 * Empty or Missing Crypto Material
 * <p>
 * Usage: throw EmptyMaterialException("error message")
 */
public class EmptyMaterialException extends RuntimeException {
    private final String message;

    public EmptyMaterialException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
