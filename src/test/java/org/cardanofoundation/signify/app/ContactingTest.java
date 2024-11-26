package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Authenticater;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.core.Httping;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.cardanofoundation.signify.app.ClientingTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContactingTest {
    private MockWebServer mockWebServer;
    private String url = "http://127.0.0.1:3901";
    private String bootUrl = "http://127.0.0.1:3903";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        bootUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        url = mockWebServer.url("/").toString().replaceAll("/$", "");
        setUpDispatcher();
    }

    void setUpDispatcher() {
        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String requestUrl = request.getRequestUrl().toString();

                if (requestUrl.startsWith(url + "/agent")) {
                    return mockConnect();
                } else if (requestUrl.equals(bootUrl + "/boot")) {
                    return new MockResponse()
                            .setResponseCode(202)
                            .setBody("");
                } else {
                    try {
                        return mockAllRequests(request);
                    } catch (SodiumException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    MockResponse mockConnect() {
        return new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(MOCK_CONNECT);
    }

    private MockResponse mockAllRequests(RecordedRequest req) throws SodiumException {
        HttpHeaders headers = new HttpHeaders();

        headers.set("Signify-Resource", "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei");
        headers.set(Httping.HEADER_SIG_TIME, new Date().toInstant().toString().replace("Z", "000+00:00"));
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);


        String reqUrl = req.getRequestUrl().toString();
        Salter salter = new Salter("0AAwMTIzNDU2Nzg5YWJjZGVm");
        Signer signer = salter.signer(
            "A",
            true,
            "agentagent-ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
            Tier.low,
            false
        );

        Authenticater authn = new Authenticater(signer, signer.getVerfer());
        Map<String, String> signedHeaderMap = authn.sign(
            headers.toSingleValueMap(),
            req.getMethod(),
            reqUrl.split("\\?")[0],
            null
        );

        String body = reqUrl.startsWith(url + "/identifiers/aid1/credentials")
            ? MOCK_CREDENTIAL
            : MOCK_GET_AID;

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202)
            .setBody(body);

        signedHeaderMap.forEach(mockResponse::addHeader);
        return mockResponse;
    }

    void cleanUpRequest() throws InterruptedException {
        while (true) {
            if (mockWebServer.takeRequest(200, TimeUnit.MILLISECONDS) == null) {
                break;
            }
        }
    }

    @Test
    void testContacts() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Contacting.Contacts contacts = client.getContacts();

        // Test list
        contacts.list("mygroup", "company", "mycompany");
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/contacts?group=mygroup&filter_field=company&filter_value=mycompany", request.getPath());

        // Test get
        String prefix = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
        contacts.get(prefix);
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/contacts/" + prefix, request.getPath());

        // Test add
        Map<String, Object> info = new HashMap<>();
        info.put("name", "John Doe");
        info.put("company", "My Company");
        contacts.add(prefix, info);
        request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/contacts/" + prefix, request.getPath());
        assertEquals(objectMapper.writeValueAsString(info), request.getBody().readUtf8());

        // Test update
        contacts.update(prefix, info);
        request = mockWebServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/contacts/" + prefix, request.getPath());
        assertEquals(objectMapper.writeValueAsString(info), request.getBody().readUtf8());

        // Test delete
        contacts.delete(prefix);
        request = mockWebServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/contacts/" + prefix, request.getPath());
    }
}