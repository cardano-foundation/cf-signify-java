package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Salter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DelegatingTest extends BaseMockServerTest {

    @Test
    @DisplayName("Test approve delegation")
    void testApproveDelegation() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Delegating.Delegations delegations = client.delegations();
        delegations.approve("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao");

        RecordedRequest request = getRecordedRequests().getLast();
        assertEquals("POST", request.getMethod());
        assertEquals(
            url + "/identifiers/EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao/delegation",
            request.getRequestUrl().toString()
        );

        String expectedBody = """
            {
                "ixn": {
                    "v": "KERI10JSON0000cf_",
                    "t": "ixn",
                    "d": "EBPt7hivibUQN-dlRyE9x_Y5LgFCGJ8QoNLSJrIkBYIg",
                    "i": "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK",
                    "s": "1",
                    "p": "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK",
                    "a": [null]
                },
                "sigs": [
                    "AAC4StAw-0IiV_LujceAXB3tnkaK011rPYPBKLgz-u6jI7hwfWGTCu5LDvBUsON4CqXbZAwPgIv6JqYjIusWKv0G"
                ],
                "salty": {
                    "sxlt": "1AAHnNQTkD0yxOC9tSz_ukbB2e-qhDTStH18uCsi5PCwOyXLONDR3MeKwWv_AVJKGKGi6xiBQH25_R1RXLS2OuK3TN3ovoUKH7-A",
                    "pidx": 0,
                    "kidx": 0,
                    "stem": "signify:aid",
                    "tier": "low",
                    "icodes": ["A"],
                    "ncodes": ["A"],
                    "dcode": "E",
                    "transferable": true
                }
            }""";

        assertEquals(
            objectMapper.readTree(expectedBody),
            objectMapper.readTree(request.getBody().readUtf8())
        );
    }
}
