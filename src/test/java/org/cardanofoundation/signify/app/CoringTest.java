package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.cardanofoundation.signify.app.coring.KeyStates;
import org.cardanofoundation.signify.app.coring.Coring;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.*;

public class CoringTest extends BaseMockServerTest {

    @Override
    public HttpResponse<String> mockFetch(String path) {
        String body = path.startsWith("/identifiers/aid1/credentials")
            ? MOCK_CREDENTIAL
            : MOCK_GET_AID;

        return createMockResponse(body);
    }

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
        client.boot();
        client.connect();
        cleanUpRequest();

        Coring.KeyEvents keyEvents = client.getKeyEvents();
        KeyStates keyStates = client.getKeyStates();

        keyEvents.get("EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX");
        Mockito.verify(client).fetch(
            eq("/events?pre=EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX"),
            eq("GET"),
            isNull(),
            any()
        );

        keyStates.get("EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX");
        Mockito.verify(client).fetch(
            eq("/states?pre=EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX"),
            eq("GET"),
            isNull(),
            any()
        );

        keyStates.list(List.of(
            "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK"
        ));
        Mockito.verify(client).fetch(
            eq("/states?pre=EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX&pre=ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK"),
            eq("GET"),
            isNull(),
            any()
        );

        Map<String, Object> queryData = new LinkedHashMap<>();
        queryData.put("pre", "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX");
        queryData.put("sn", "1");
        queryData.put("anchor", "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao");

        keyStates.query(
            "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            1,
            "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao"
        );
        Mockito.verify(client).fetch(
            eq("/queries"),
            eq("POST"),
            argThat(arg -> {
                try {
                    Map<String, Object> data = objectMapper.readValue(objectMapper.writeValueAsString(arg), new TypeReference<>() {});
                    return data.equals(queryData);
                } catch (JsonProcessingException e) {
                    return false;
                }
            }),
            any()
        );
    }

    @Test
    @DisplayName("Agent configuration")
    void testAgentConfiguration() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Coring.Config config = client.getConfig();

        config.get();
        Mockito.verify(client).fetch(
            eq("/config"),
            eq("GET"),
            isNull(),
            any()
        );
    }
}
