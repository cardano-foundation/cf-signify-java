package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.coring.Oobis;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.generated.keria.model.EndRole;
import org.cardanofoundation.signify.generated.keria.model.LocSchemeMetadata;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import org.cardanofoundation.signify.app.aiding.IdentifierController;
import org.cardanofoundation.signify.app.aiding.LocSchemeArgs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    @DisplayName("Can retrieve location schemes for a given EID")
    void testLocschemes() throws Exception {
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Oobis oobis = client.oobis();
        String eid = "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei";

        List<LocSchemeMetadata> result = oobis.locschemes(eid);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(url + "/locschemes/" + eid, request.getRequestUrl().toString());
        assertEquals(2, result.size());
        assertEquals("http", result.get(0).getScheme());
        assertEquals("http://indexer.example.com", result.get(0).getUrl());
        assertEquals("https", result.get(1).getScheme());
        assertEquals("https://indexer.example.com", result.get(1).getUrl());
    }

    @Test
    @DisplayName("Adding a new location scheme")
    void testAddLocScheme() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        IdentifierController identifierController = client.identifiers();

        String eid = "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei";
        String urlEndpoint = "http://example.com/endpoint";
        String scheme = "http";

        LocSchemeArgs args = LocSchemeArgs.builder()
                .url(urlEndpoint)
                .scheme(scheme)
                .eid(eid)
                .build();

        var result = identifierController.addLocScheme("aid1", args);

        assertNotNull(result);
        assertNotNull(result.op());

        RecordedRequest getRequest = mockWebServer.takeRequest();
        assertEquals("GET", getRequest.getMethod());

        RecordedRequest postRequest = mockWebServer.takeRequest();
        assertEquals("POST", postRequest.getMethod());
        assertEquals("/identifiers/aid1/locschemes", postRequest.getPath());

        Map<String, Object> body = objectMapper.readValue(postRequest.getBody().readUtf8(), new TypeReference<>() {});
        assertNotNull(body.get("rpy"));
        assertNotNull(body.get("sigs"));
        assertTrue(body.get("sigs") instanceof java.util.List);

        Map<String, Object> rpy = (Map<String, Object>) body.get("rpy");
        assertEquals("rpy", rpy.get("t"));
        assertEquals("/loc/scheme", rpy.get("r"));

        Map<String, Object> a = (Map<String, Object>) rpy.get("a");
        assertEquals(urlEndpoint, a.get("url"));
        assertEquals(scheme, a.get("scheme"));
        assertEquals(eid, a.get("eid"));
    }

}
