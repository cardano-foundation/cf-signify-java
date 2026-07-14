package org.cardanofoundation.signify.exception;

/**
 * The agent answered with a 5xx status: the request reached the agent but failed on the
 * agent's side. Unlike a 4xx a retry may succeed once the agent recovers — but for
 * KEL-appending calls the failure may have occurred after the event was applied, so
 * verify agent state before retrying those.
 *
 * <p>Construct via {@link SignifyAgentException#from}.
 */
public class SignifyServerException extends SignifyAgentException {

    protected SignifyServerException(String message, String method, String path,
                                     int statusCode, String rawBody, String title, String description) {
        super(message, method, path, statusCode, rawBody, title, description);
    }
}
