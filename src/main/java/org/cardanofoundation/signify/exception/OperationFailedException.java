package org.cardanofoundation.signify.exception;

import org.cardanofoundation.signify.generated.keria.model.FailedOperation;

/**
 * Thrown when a long-running operation, or a dependent operation it relies on,
 * completes as failed while being waited on.
 */
public class OperationFailedException extends SignifyException {
    private final transient FailedOperation operation;

    public OperationFailedException(FailedOperation operation) {
        super("Operation failed: " + operation.getName()
                + (operation.getError() != null ? " - " + operation.getError().getMessage() : ""));
        this.operation = operation;
    }

    public FailedOperation getOperation() {
        return operation;
    }
}
