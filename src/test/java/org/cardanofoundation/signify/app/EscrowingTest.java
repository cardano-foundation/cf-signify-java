package org.cardanofoundation.signify.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

public class EscrowingTest extends BaseMockServerTest {

    @Override
    public HttpResponse<String> mockFetch(String path) {
        String body = path.startsWith("/identifiers/aid1/credentials")
            ? MOCK_CREDENTIAL
            : MOCK_GET_AID;

        return createMockResponse(body);
    }

    @Test
    @DisplayName("Test Escrows")
    void testEscrows() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Escrowing.Escrows escrows = client.getEscrows();
        escrows.listReply("/presentation/request");

        Mockito.verify(client).fetch(
            eq("/escrows/rpy?route=%2Fpresentation%2Frequest"),
            eq("GET"),
            isNull(),
            any()
        );
    }
}