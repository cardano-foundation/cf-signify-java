package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import org.cardanofoundation.signify.app.coring.Oobis;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

public class OobisTest extends BaseMockServerTest {

    @Override
    public HttpResponse<String> mockFetch(String path) {
        String body = path.startsWith("/identifiers/aid1/credentials")
            ? MOCK_CREDENTIAL
            : MOCK_GET_AID;

        return createMockResponse(body);
    }

    @Test
    @DisplayName("Test Oobis")
    void testOobis() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Oobis oobis = client.getOobis();

        // Test get
        oobis.get("aid", "agent");
        Mockito.verify(client).fetch(
            eq("/identifiers/aid/oobis?role=agent"),
            eq("GET"),
            isNull(),
            any()
        );

        // Test resolve without alias
        oobis.resolve("http://oobiurl.com", null);
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(client, Mockito.times(1)).fetch(
            eq("/oobis"),
            eq("POST"),
            bodyCaptor.capture(),
            any()
        );
        Map<String, Object> data = Utils.fromJson(Utils.jsonStringify(bodyCaptor.getValue()), new TypeReference<>() {});
        assertEquals("http://oobiurl.com", data.get("url"));

        // Test resolve with alias
        oobis.resolve("http://oobiurl.com", "witness");
        bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(client, Mockito.times(2)).fetch(
            eq("/oobis"),
            eq("POST"),
            bodyCaptor.capture(),
            any()
        );
        data = Utils.fromJson(Utils.jsonStringify(bodyCaptor.getValue()), new TypeReference<>() {});
        assertEquals("http://oobiurl.com", data.get("url"));
        assertEquals("witness", data.get("oobialias"));
    }
}
