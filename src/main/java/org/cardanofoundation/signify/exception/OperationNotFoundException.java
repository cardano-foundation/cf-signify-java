package org.cardanofoundation.signify.exception;

/**
 * Thrown when a long-running operation disappears from the agent while being waited on.
 */
public class OperationNotFoundException extends SignifyException {
    public OperationNotFoundException(String name) {
        super("Operation not found: " + name);
    }
}
