package org.cardanofoundation.signify.exception;

/**
 * The calling thread was interrupted while waiting on the agent (an HTTP round-trip or
 * operation polling). This is a cancellation signal, not a failure — never retry.
 *
 * <p>Throw sites restore the thread's interrupt flag before raising this, so
 * {@code Thread.currentThread().isInterrupted()} stays {@code true} for cooperative
 * cancellation further up the stack.
 */
public class SignifyInterruptedException extends SignifyException {

    public SignifyInterruptedException(InterruptedException cause) {
        super("Interrupted while waiting on the agent", cause);
    }
}
