package org.cardanofoundation.signify.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.fasterxml.jackson.core.JsonProcessingException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;

public class DelegatingTest extends BaseMockServerTest {

    @Test
    @DisplayName("Test approve delegation")
    void testApproveDelegation() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Delegating.Delegations delegations = client.getDelegations();
        delegations.approve("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao");

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

        Mockito.verify(client).fetch(
            eq("/identifiers/EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao/delegation"),
            eq("POST"),
            argThat(arg -> {
                try {
                    return objectMapper.readTree(objectMapper.writeValueAsString(arg))
                            .equals(objectMapper.readTree(expectedBody));
                } catch (JsonProcessingException e) {
                    return false;
                }
            }),
            any()
        );
    }
}
