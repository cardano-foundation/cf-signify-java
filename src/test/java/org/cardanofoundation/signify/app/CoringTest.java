package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.coring.KeyStates;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.cesr.Salter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CoringTest extends BaseMockServerTest {

    @Test
    public void testRandomPasscode() {
        final String passcode = Coring.randomPasscode();
        assertEquals(passcode.length(), 21);

        final String passcode2 = Coring.randomPasscode();
        assertEquals(passcode2.length(), 21);

        // passcode should be unique
        assertNotEquals(passcode, passcode2);
    }

    @Test
    @DisplayName("Events and States")
    void testEventsAndStates() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Coring.KeyEvents keyEvents = client.keyEvents();
        KeyStates keyStates = client.keyStates();

        keyEvents.get("EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX");
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(
            url + "/events?pre=EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            request.getRequestUrl().toString()
        );

        keyStates.get("EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX");
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(
            url + "/states?pre=EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            request.getRequestUrl().toString()
        );

        keyStates.list(List.of(
            "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK"
        ));
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(
            url + "/states?pre=EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX&pre=ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK",
            request.getRequestUrl().toString()
        );

        keyStates.query(
            "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            "1",
            "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao"
        );
        request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals(url + "/queries", request.getRequestUrl().toString());
        
        Map<String, Object> data = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
        assertEquals("EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX", data.get("pre"));
        assertEquals("1", data.get("sn"));
        assertEquals("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", data.get("anchor"));
    }

    @Test
    @DisplayName("Agent configuration")
    void testAgentConfiguration() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Coring.Config config = client.config();

        config.get();
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(url + "/config", request.getRequestUrl().toString());
    }

    @Test
    public void testRandomNonce() {
        final String nonce = Coring.randomNonce();
        assertEquals(nonce.length(), 44);

        final String nonce2 = Coring.randomNonce();
        assertEquals(nonce2.length(), 44);

        // nonce should be unique
        assertNotEquals(nonce, nonce2);
    }
}
