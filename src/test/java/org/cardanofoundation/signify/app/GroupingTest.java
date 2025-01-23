package org.cardanofoundation.signify.app;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;

public class GroupingTest extends BaseMockServerTest {

    @Override
    public HttpResponse<String> mockFetch(String path) {
        String body = path.startsWith("/identifiers/aid1/credentials")
            ? MOCK_CREDENTIAL
            : MOCK_GET_AID;

        return createMockResponse(body);
    }

    @Test
    void testGroups() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Grouping.Groups groups = client.getGroups();

        groups.sendRequest("aid1", new HashMap<>(), List.of(), "");
        Mockito.verify(client).fetch(
            eq("/identifiers/aid1/multisig/request"),
            eq("POST"),
            any(),
            any()
        );

        groups.getRequest("ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00");
        Mockito.verify(client).fetch(
            eq("/multisig/request/ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00"),
            eq("GET"),
            isNull(),
            any()
        );

        groups.join(
            "aid1",
            new HashMap<>().put("ked", new HashMap<>()),
            List.of("sig"),
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
            List.of("1", "2", "3"),
            List.of("a", "b", "c")
        );
        Mockito.verify(client).fetch(
            eq("/identifiers/aid1/multisig/join"),
            eq("POST"),
            any(),
            any()
        );
    }
}