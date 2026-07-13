package org.cardanofoundation.signify.app.coring.exception;

/**
 * Thrown when a long-running operation disappears from the agent while being waited on.
 */
public class OperationNotFoundException extends RuntimeException {
    public OperationNotFoundException(String name) {
        super("Operation not found: " + name);
    }
}
