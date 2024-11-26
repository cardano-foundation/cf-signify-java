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

public class ContactingTest extends BaseMockServerTest {

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