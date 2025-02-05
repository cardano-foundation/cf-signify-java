package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialFilter;
import org.cardanofoundation.signify.app.credentialing.credentials.Credentials;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Authenticater;
import org.cardanofoundation.signify.core.Httping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CredentialingTest extends BaseMockServerTest {


    @Override
    public MockResponse mockAllRequests(RecordedRequest req) throws LibsodiumException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("signify-resource", "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei");
        headers.put(Httping.HEADER_SIG_TIME, new Date().toInstant().toString().replace("Z", "000+00:00"));
        headers.put("content-type", "application/json");

        String reqUrl = req.getRequestUrl().toString();
        Salter salter = new Salter("0AAwMTIzNDU2Nzg5YWJjZGVm");
        Signer signer = salter.signer(
                "A",
                true,
                "agentagent-ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
                Salter.Tier.low,
                false
        );

        Authenticater authn = new Authenticater(signer, signer.getVerfer());
        Map<String, String> signedHeaderMap = authn.sign(
                headers,
                req.getMethod(),
                req.getPath().split("\\?")[0],
                null
        );

        String body = reqUrl.startsWith(url + "/credentials")
                ? MOCK_CREDENTIAL
                : MOCK_GET_AID;

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(202)
                .setBody(body);

        signedHeaderMap.forEach(mockResponse::addHeader);
        return mockResponse;
    }

    @Test
    @DisplayName("Test Credentialing")
    void testCredentialing() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Credentials credentials = client.credentials();

        // Create the CredentialFilter object
        CredentialFilter kargs = CredentialFilter.builder()
                .filter(new HashMap<>() {{
                    put("-i", Collections.singletonMap("$eq", "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX"));
                }})
                .sort(Collections.singletonList(new HashMap<>() {{
                    put("-s", 1);
                }}))
                .limit(25)
                .skip(5)
                .build();

        credentials.list(kargs);
        RecordedRequest lastCall = mockWebServer.takeRequest();
        assertEquals("POST", lastCall.getMethod());
        assertEquals("/credentials/query", lastCall.getPath());
        assertEquals(Utils.jsonStringify(kargs), lastCall.getBody().readUtf8());


        credentials.get("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", true);
        lastCall = mockWebServer.takeRequest();
        assertEquals("GET", lastCall.getMethod());
        assertEquals(url + "/credentials/EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", lastCall.getRequestUrl().toString());

        String registry = "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX";
        String schema = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
        String isuee = "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p";

        CredentialData.CredentialSubject subject = CredentialData.CredentialSubject.builder()
                .i(isuee)
                .additionalProperties(new LinkedHashMap<>() {{
                    put("LEI", "1234");
                }})
                .build();

        CredentialData credentialData = CredentialData.builder()
                .ri(registry)
                .s(schema)
                .a(subject)
                .build();


        // test issue
        credentials.issue("aid1", credentialData);
        lastCall = getRecordedRequests().getLast();

        Map<String, Object> lastBody = Utils.fromJson(lastCall.getBody().readUtf8(), Map.class);
        Map<String, Object> acdc = (Map<String, Object>) lastBody.get("acdc");
        Map<String, Object> iss = (Map<String, Object>) lastBody.get("iss");
        Map<String, Object> ixn = (Map<String, Object>) lastBody.get("ixn");
        List<String> sigs = (List<String>) lastBody.get("sigs");

        assertEquals("POST", lastCall.getMethod());
        assertEquals("/identifiers/aid1/credentials", lastCall.getPath());
        assertEquals(acdc.get("ri"), registry);
        assertEquals(acdc.get("s"), schema);
        assertEquals(((Map<?, ?>) acdc.get("a")).get("i"), isuee);
        assertEquals(((Map<?, ?>) acdc.get("a")).get("LEI"), "1234");

        assertEquals(iss.get("s"), "0");
        assertEquals(iss.get("t"), "iss");
        assertEquals(iss.get("ri"), registry);
        assertEquals(iss.get("i"), acdc.get("d"));

        assertEquals(ixn.get("t"), "ixn");
        assertEquals(ixn.get("i"), acdc.get("i"));
        assertEquals(ixn.get("p"), acdc.get("i"));

        assertEquals(sigs.size(), 1);
        assertEquals(sigs.get(0).substring(0, 2), "AA");
        assertEquals(sigs.get(0).length(), 88);

        // test revoke
        String credential = acdc.get("i").toString();
        credentials.revoke("aid1", credential, null);

        lastCall = getRecordedRequests().getLast();
        assertEquals("DELETE", lastCall.getMethod());
        assertEquals(url + "/identifiers/aid1/credentials/" + credential, lastCall.getRequestUrl().toString());

        lastBody = Utils.fromJson(lastCall.getBody().readUtf8(), Map.class);

        Map<String, Object> rev = (Map<String, Object>) lastBody.get("rev");
        ixn = (Map<String, Object>) lastBody.get("ixn");
        sigs = (List<String>) lastBody.get("sigs");

        assertEquals(rev.get("t"), "rev");
        assertEquals(rev.get("s"), "1");
        assertEquals(rev.get("ri"), "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df");
        assertEquals(rev.get("i"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");

        assertEquals(ixn.get("t"), "ixn");
        assertEquals(ixn.get("i"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");
        assertEquals(ixn.get("p"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");

        assertEquals(sigs.size(), 1);
        assertEquals(sigs.get(0).substring(0, 2), "AA");
        assertEquals(sigs.get(0).length(), 88);

        // test state
        credentials.state("EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df", "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo");
        lastCall = getRecordedRequests().getLast();
        assertEquals("GET", lastCall.getMethod());
        assertEquals(url + "/registries/EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df/EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo", lastCall.getRequestUrl().toString());
        assertEquals(lastCall.getBody().readUtf8(), "");
    }
}
