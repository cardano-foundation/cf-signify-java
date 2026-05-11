package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.coring.Oobis;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.generated.keria.model.EndRole;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OobisTest extends BaseMockServerTest {

    @Test
    @DisplayName("Test Oobis")
    void testOobis() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Oobis oobis = client.oobis();

        // Test get
        oobis.get("aid", "agent");
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(url + "/identifiers/aid/oobis?role=agent", request.getRequestUrl().toString());

        // Test resolve
        oobis.resolve("http://oobiurl.com", null);
        request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals(url + "/oobis", request.getRequestUrl().toString());
        Map<String, Object> data = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {
        });
        assertTrue(data.containsKey("url"));
        assertEquals("http://oobiurl.com", data.get("url"));

        oobis.resolve("http://oobiurl.com", "witness");
        request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals(url + "/oobis", request.getRequestUrl().toString());
        data = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {
        });
        assertTrue(data.containsKey("url"));
        assertEquals("http://oobiurl.com", data.get("url"));
        assertTrue(data.containsKey("oobialias"));
        assertEquals("witness", data.get("oobialias"));
    }

    @Test
    @DisplayName("Test endroles without role filter")
    void testEndroles_withoutRole() throws Exception {
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Oobis oobis = client.oobis();
        String aid = "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK";

        List<EndRole> result = oobis.endroles(aid, null);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(url + "/endroles/" + aid, request.getRequestUrl().toString());
        assertEquals(2, result.size());
        assertEquals(aid, result.get(0).getCid());
        assertEquals("agent", result.get(0).getRole());
        assertEquals("EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei", result.get(0).getEid());
    }

    @Test
    @DisplayName("Test endroles with role filter")
    void testEndroles_withRole() throws Exception {
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Oobis oobis = client.oobis();
        String aid = "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK";

        List<EndRole> result = oobis.endroles(aid, "agent");

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(url + "/endroles/" + aid + "/agent", request.getRequestUrl().toString());
        assertEquals(2, result.size());
    }

}
