package org.cardanofoundation.signify.app.coring.exception;

/**
 * Thrown when a long-running operation does not complete within the configured timeout.
 */
public class OperationTimeoutException extends RuntimeException {
    public OperationTimeoutException(String name, long timeoutMs) {
        super("Operation " + name + " timed out after " + timeoutMs + " ms");
    }
}
