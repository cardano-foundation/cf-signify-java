package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Encountered opcode code start char "_" unexpectantly
 * <p>
 * Usage: throw UnexpectedOpCodeException("error message")
 */
public class UnexpectedOpCodeException extends DerivationCodeException {
    
    public UnexpectedOpCodeException(String message) {
        super(message);
    }
}
