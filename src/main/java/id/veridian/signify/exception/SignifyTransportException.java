package org.cardanofoundation.signify.exception;

/**
 * An I/O failure prevented an HTTP response from being received from the agent
 * (connection refused, connection reset, request timeout, ...).
 *
 * <p>Because no response was received, the outcome of the request is unknown: for
 * idempotent reads a retry is safe, but for state-changing calls — especially
 * KEL-appending events (incept, rotate, interact, issue, revoke) — the request may
 * still have been applied by the agent. Verify agent state before retrying those.
 */
public class SignifyTransportException extends SignifyException {

    public SignifyTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
