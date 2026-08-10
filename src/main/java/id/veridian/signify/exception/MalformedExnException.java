package org.cardanofoundation.signify.exception;

/**
 * Thrown when an exchange (exn) message matches a known route but its payload does not
 * have the shape that route requires. This signals bad wire data from a peer, not a
 * caller bug — catch it to skip or quarantine the message rather than abort, e.g. in a
 * notification-processing loop.
 */
public class MalformedExnException extends SignifyException {
    private final String route;
    private final String said;

    public MalformedExnException(String route, String said, RuntimeException cause) {
        super("Malformed " + route + " message" + (said == null ? "" : " (d=" + said + ")")
                + ": " + cause.getMessage(), cause);
        this.route = route;
        this.said = said;
    }

    public String getRoute() {
        return route;
    }

    /** The exn message's own SAID ({@code d} field), when present. */
    public String getSaid() {
        return said;
    }
}
