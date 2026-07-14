package org.cardanofoundation.signify.exception;

/**
 * Root of the Signify exception hierarchy. Every runtime failure this library raises —
 * transport, agent, crypto, serialization, protocol — is a subtype, so application
 * boundaries can {@code catch (SignifyException e)} to handle library failures
 * uniformly. API misuse (invalid arguments, calling out of lifecycle order) surfaces
 * as standard JDK unchecked exceptions such as {@link IllegalArgumentException} and
 * {@link IllegalStateException} instead; these indicate caller bugs, not runtime
 * conditions to handle.
 *
 * <p>Unchecked by design: transport and agent failures are typically handled at
 * application boundaries (retry policies, schedulers) rather than at each call site.
 * Subtypes encode recoverability:
 * <ul>
 *   <li>{@link SignifyTransportException} — no HTTP response was received, so the
 *       request outcome is unknown.</li>
 *   <li>{@link SignifyAgentException} — the agent answered with an error status;
 *       its subtype {@link SignifyServerException} marks the 5xx subset where a
 *       retry may succeed.</li>
 *   <li>{@link SignifyInterruptedException} — cancellation of the calling thread,
 *       never an error to retry.</li>
 *   <li>Everything else (crypto, serialization, protocol validation) indicates a bug
 *       or bad input — fail fast.</li>
 * </ul>
 */
public class SignifyException extends RuntimeException {

    public SignifyException(String message) {
        super(message);
    }

    public SignifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
