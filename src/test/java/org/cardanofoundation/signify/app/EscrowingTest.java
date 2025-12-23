package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscrowingTest extends BaseMockServerTest {

    @Test
    @DisplayName("Test Escrows")
    void testEscrows() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Escrowing.Escrows escrows = client.escrows();
        escrows.listReply("/presentation/request");

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(
            url + "/escrows/rpy?route=%2Fpresentation%2Frequest",
            request.getRequestUrl().toString()
        );
    }
}