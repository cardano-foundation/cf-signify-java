package org.cardanofoundation.signify.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * The KERIA agent answered with a non-2xx status. A response was received, so — unlike
 * {@link SignifyTransportException} — the outcome is known: for 4xx the agent rejected
 * the request and retrying the same request will fail again. 5xx responses are raised
 * as {@link SignifyServerException}.
 *
 * <p>KERIA errors usually carry a Falcon-style JSON body; its {@code title} and
 * {@code description} fields are parsed when present, and the raw body is always
 * retained via {@link #getRawBody()}.
 */
public class SignifyAgentException extends SignifyException {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String method;
    private final String path;
    private final int statusCode;
    private final String rawBody;
    private final String title;
    private final String description;

    protected SignifyAgentException(String message, String method, String path,
                                    int statusCode, String rawBody, String title, String description) {
        super(message);
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.rawBody = rawBody;
        this.title = title;
        this.description = description;
    }

    public static SignifyAgentException from(String method, String path, int statusCode, String rawBody) {
        return from(method, path, statusCode, rawBody, null);
    }

    /**
     * @param context optional caller context prepended to the standard message,
     *                e.g. {@code "Agent does not exist for controller <caid>"}
     */
    public static SignifyAgentException from(String method, String path, int statusCode,
                                             String rawBody, String context) {
        String title = null;
        String description = null;
        if (rawBody != null && !rawBody.isBlank()) {
            try {
                JsonNode node = MAPPER.readTree(rawBody);
                title = node.path("title").isTextual() ? node.path("title").textValue() : null;
                description = node.path("description").isTextual() ? node.path("description").textValue() : null;
            } catch (Exception e) {
                // agent error bodies are not guaranteed to be JSON; the raw body is kept instead
            }
        }

        String detail = description != null ? description : (title != null ? title : rawBody);
        String message = (context != null ? context + ": " : "")
                + String.format("HTTP %s %s - %d - %s", method, path, statusCode, detail);

        return statusCode >= 500
                ? new SignifyServerException(message, method, path, statusCode, rawBody, title, description)
                : new SignifyAgentException(message, method, path, statusCode, rawBody, title, description);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** The response body exactly as returned by the agent. */
    public String getRawBody() {
        return rawBody;
    }

    /** The {@code title} of the agent's structured error body, when one was present. */
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    /** The {@code description} of the agent's structured error body, when one was present. */
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }
}
