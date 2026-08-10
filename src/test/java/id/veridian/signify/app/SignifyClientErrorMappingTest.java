package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.exception.SignifyAgentException;
import org.cardanofoundation.signify.exception.SignifyInterruptedException;
import org.cardanofoundation.signify.exception.SignifyServerException;
import org.cardanofoundation.signify.exception.SignifyTransportException;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SignifyClientErrorMappingTest extends BaseMockServerTest {

    private SignifyClient connectedClient() throws InterruptedException {
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.connect();
        cleanUpRequest();
        return client;
    }

    private void setUpErrorDispatcher(int status, String body) {
        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse().setResponseCode(status).setBody(body);
            }
        });
    }

    @Test
    @DisplayName("4xx maps to SignifyAgentException with parsed KERIA error body")
    void testAgentErrorParsesStructuredBody() throws InterruptedException {
        SignifyClient client = connectedClient();
        setUpErrorDispatcher(400, "{\"title\": \"400 Bad Request\", \"description\": \"name is required\"}");

        SignifyAgentException exception = assertThrows(
                SignifyAgentException.class,
                () -> client.fetch("/identifiers", "POST", Map.of())
        );

        assertEquals(400, exception.getStatusCode());
        assertEquals("POST", exception.getMethod());
        assertEquals("/identifiers", exception.getPath());
        assertEquals(Optional.of("400 Bad Request"), exception.getTitle());
        assertEquals(Optional.of("name is required"), exception.getDescription());
        assertEquals("HTTP POST /identifiers - 400 - name is required", exception.getMessage());
        assertFalse(exception instanceof SignifyServerException);
    }

    @Test
    @DisplayName("5xx maps to SignifyServerException")
    void testServerErrorMapsToServerException() throws InterruptedException {
        SignifyClient client = connectedClient();
        setUpErrorDispatcher(500, "{\"description\": \"database is locked\"}");

        SignifyServerException exception = assertThrows(
                SignifyServerException.class,
                () -> client.fetch("/identifiers", "POST", Map.of())
        );

        assertEquals(500, exception.getStatusCode());
        assertEquals(Optional.of("database is locked"), exception.getDescription());
    }

    @Test
    @DisplayName("Non-JSON error body is kept raw")
    void testNonJsonErrorBodyKeptRaw() throws InterruptedException {
        SignifyClient client = connectedClient();
        setUpErrorDispatcher(400, "plain text failure");

        SignifyAgentException exception = assertThrows(
                SignifyAgentException.class,
                () -> client.fetch("/identifiers", "POST", Map.of())
        );

        assertEquals("plain text failure", exception.getRawBody());
        assertEquals(Optional.empty(), exception.getTitle());
        assertEquals(Optional.empty(), exception.getDescription());
        assertEquals("HTTP POST /identifiers - 400 - plain text failure", exception.getMessage());
    }

    @Test
    @DisplayName("GET 404 is an expected outcome, returned rather than thrown")
    void testGet404IsReturnedNotThrown() throws InterruptedException {
        SignifyClient client = connectedClient();
        setUpErrorDispatcher(404, "");

        HttpResponse<String> response = client.fetch("/contacts/unknown", "GET", null);

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("I/O failure without a response maps to SignifyTransportException")
    void testTransportFailure() throws InterruptedException, IOException {
        SignifyClient client = connectedClient();
        mockWebServer.shutdown();

        SignifyTransportException exception = assertThrows(
                SignifyTransportException.class,
                () -> client.fetch("/contacts", "GET", null)
        );

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    @DisplayName("Interruption maps to SignifyInterruptedException and restores the interrupt flag")
    void testInterruptionRestoresFlag() throws InterruptedException {
        SignifyClient client = connectedClient();

        Thread.currentThread().interrupt();
        try {
            assertThrows(
                    SignifyInterruptedException.class,
                    () -> client.fetch("/contacts", "GET", null)
            );
            assertTrue(Thread.currentThread().isInterrupted(), "interrupt flag must be restored");
        } finally {
            // clear the flag so it cannot leak into other tests
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("boot() failure maps to SignifyAgentException")
    void testBootError() {
        setUpErrorDispatcher(400, "{\"title\": \"400 Bad Request\", \"description\": \"agent already exists\"}");
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);

        SignifyAgentException exception = assertThrows(SignifyAgentException.class, client::boot);

        assertEquals(400, exception.getStatusCode());
        assertEquals(Optional.of("agent already exists"), exception.getDescription());
    }

    @Test
    @DisplayName("state() on a missing agent maps to SignifyAgentException with context")
    void testStateAgentMissing() {
        setUpErrorDispatcher(404, "");
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);

        SignifyAgentException exception = assertThrows(SignifyAgentException.class, client::state);

        assertEquals(404, exception.getStatusCode());
        assertTrue(exception.getMessage().startsWith("Agent does not exist for controller "));
    }
}
