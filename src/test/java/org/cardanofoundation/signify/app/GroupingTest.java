package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.signify.generated.keria.model.Exn;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupingTest extends BaseMockServerTest {

    @Test
    void testGroups() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Grouping.Groups groups = client.groups();

        groups.sendRequest("aid1", new Exn(), List.of(), "");
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals(url + "/identifiers/aid1/multisig/request", request.getRequestUrl().toString());

        groups.getRequest("ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00");
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(
            url + "/multisig/request/ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
            request.getRequestUrl().toString()
        );

        // Mock returns a /multisig/iss request — type it with the route parsers
        var requests = groups.getRequest("ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00").orElseThrow();
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(
            url + "/multisig/request/ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
            request.getRequestUrl().toString()
        );
        assertEquals(1, requests.size());

        // route mismatch — asMultisigIcpGroup is empty
        assertTrue(ExnMessageTypes.asGroup(requests.getFirst(), ExnMessageTypes.MultisigIcpGroup.class).isEmpty());

        // route match — asMultisigIssGroup parses the typed group
        var issGroup = ExnMessageTypes.asGroup(requests.getFirst(), ExnMessageTypes.MultisigIssGroup.class).orElseThrow();
        assertEquals("multisig", issGroup.metadata().groupName());
        assertEquals("member1", issGroup.metadata().memberName());

        groups.join(
            "aid1",
            new HashMap<>().put("ked", new HashMap<>()),
            List.of("sig"),
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
            List.of("1", "2", "3"),
            List.of("a", "b", "c")
        );
        request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals(url + "/identifiers/aid1/multisig/join", request.getRequestUrl().toString());
    }
}