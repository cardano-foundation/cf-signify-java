package org.cardanofoundation.signify.exception;

/**
 * Thrown when waiting on a long-running operation is aborted via its
 * {@code AbortSignal}. This is a caller-initiated cancellation, not a failure.
 */
public class OperationAbortedException extends SignifyException {
    private final transient Object reason;

    public OperationAbortedException(Object reason) {
        super("Operation aborted: " + reason);
        this.reason = reason;
    }

    public Object getReason() {
        return reason;
    }
}
