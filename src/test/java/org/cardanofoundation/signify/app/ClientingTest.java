package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClientingTest {
    private MockWebServer mockWebServer;
    private String url = "http://127.0.0.1:3901";
    private String bootUrl = "http://127.0.0.1:3903";
    private final String bran = "0123456789abcdefghijk";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        bootUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        url = mockWebServer.url("/").toString().replaceAll("/$", "");

        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String requestUrl = request.getRequestUrl().toString();

                // Handle /agent endpoints
                if (requestUrl.startsWith(url + "/agent")) {
                    return mockConnect();
                }
                // Handle /boot endpoint
                else if (requestUrl.equals(bootUrl + "/boot")) {
                    return new MockResponse()
                        .setResponseCode(202)
                        .setBody("");
                }
                // Handle all other endpoints
                else {
                    // TODO add default mock response
                    throw new IllegalArgumentException("Unsupported request URL: ");
                }
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    private static final String MOCK_CONNECT = """
        {
            "agent": {
                "vn": [1, 0],
                "i": "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "s": "0",
                "p": "",
                "d": "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "f": "0",
                "dt": "2023-08-19T21:04:57.948863+00:00",
                "et": "dip",
                "kt": "1",
                "k": ["DMZh_y-H5C3cSbZZST-fqnsmdNTReZxIh0t2xSTOJQ8a"],
                "nt": "1",
                "n": ["EM9M2EQNCBK0MyAhVYBvR98Q0tefpvHgE-lHLs82XgqC"],
                "bt": "0",
                "b": [],
                "c": [],
                "ee": {
                    "s": "0",
                    "d": "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                    "br": [],
                    "ba": []
                },
                "di": "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            },
            "controller": {
                "state": {
                    "vn": [1, 0],
                    "i": "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
                    "s": "0",
                    "p": "",
                    "d": "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
                    "f": "0",
                    "dt": "2023-08-19T21:04:57.959047+00:00",
                    "et": "icp",
                    "kt": "1",
                    "k": ["DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"],
                    "nt": "1",
                    "n": ["EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"],
                    "bt": "0",
                    "b": [],
                    "c": [],
                    "ee": {
                        "s": "0",
                        "d": "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
                        "br": [],
                        "ba": []
                    },
                    "di": ""
                },
                "ee": {
                    "v": "KERI10JSON00012b_",
                    "t": "icp",
                    "d": "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
                    "i": "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
                    "s": "0",
                    "kt": "1",
                    "k": ["DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"],
                    "nt": "1",
                    "n": ["EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"],
                    "bt": "0",
                    "b": [],
                    "c": [],
                    "a": []
                }
            },
            "ridx": 0,
            "pidx": 0
        }""";

    private static final String MOCK_GET_AID = """
        {
            "name": "aid1",
            "prefix": "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK",
            "salty": {
                "sxlt": "1AAHnNQTkD0yxOC9tSz_ukbB2e-qhDTStH18uCsi5PCwOyXLONDR3MeKwWv_AVJKGKGi6xiBQH25_R1RXLS2OuK3TN3ovoUKH7-A",
                "pidx": 0,
                "kidx": 0,
                "stem": "signify:aid",
                "tier": "low",
                "dcode": "E",
                "icodes": ["A"],
                "ncodes": ["A"],
                "transferable": true
            },
            "transferable": true,
            "state": {
                "vn": [1, 0],
                "i": "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK",
                "s": "0",
                "p": "",
                "d": "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK",
                "f": "0",
                "dt": "2023-08-21T22:30:46.473545+00:00",
                "et": "icp",
                "kt": "1",
                "k": ["DPmhSfdhCPxr3EqjxzEtF8TVy0YX7ATo0Uc8oo2cnmY9"],
                "nt": "1",
                "n": ["EAORnRtObOgNiOlMolji-KijC_isa3lRDpHCsol79cOc"],
                "bt": "0",
                "b": [],
                "c": [],
                "ee": {
                    "s": "0",
                    "d": "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK",
                    "br": [],
                    "ba": []
                },
                "di": ""
            },
            "windexes": []
        }""";

    private static final String MOCK_CREDENTIAL = """
        {
            "sad": {
                "v": "ACDC10JSON000197_",
                "d": "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",
                "i": "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",
                "ri": "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df",
                "s": "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao",
                "a": {
                    "d": "EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P",
                    "i": "EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby",
                    "dt": "2023-08-23T15:16:07.553000+00:00",
                    "LEI": "5493001KJTIIGC8Y1R17"
                }
            },
            "pre": "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",
            "sadsigers": [{
                "path": "-",
                "pre": "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",
                "sn": 0,
                "d": "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1"
            }],
            "sadcigars": [],
            "chains": [],
            "status": {
                "v": "KERI10JSON000135_",
                "i": "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",
                "s": "0",
                "d": "ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW",
                "ri": "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df",
                "ra": {},
                "a": {
                    "s": 2,
                    "d": "EIpgyKVF0z0Pcn2_HgbWhEKmJhOXFeD4SA62SrxYXOLt"
                },
                "dt": "2023-08-23T15:16:07.553000+00:00",
                "et": "iss"
            }
        }""";

    MockResponse mockConnect() {
        return new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(MOCK_CONNECT);
    }

    MockResponse mockGetAID() {
        return new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(MOCK_GET_AID);
    }

    MockResponse mockCredential() {
        return new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody(MOCK_CREDENTIAL);
    }

    @Test
    @DisplayName("SignifyClient initialization")
    void testSignifyClientInitialization() throws Exception {
        InvalidValueException exception = assertThrows(
            InvalidValueException.class,
            () -> new SignifyClient(url, "short", Tier.low, bootUrl, null)
        );
        assertEquals("bran must be 21 characters", exception.getMessage());

        SignifyClient client = new SignifyClient(
            url,
            bran,
            Tier.low,
            bootUrl,
            null
        );

        assertEquals(bran, client.getBran());
        assertEquals(url, client.getUrl());
        assertEquals(bootUrl, client.getBootUrl());
        assertEquals(Tier.low, client.getTier());
        assertEquals(0, client.getPidx());
        assertEquals(
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
            client.getController().getPre()
        );
        assertEquals("signify:controller", client.getController().getStem());
        assertEquals(Tier.low, client.getController().getTier());

        String expectedSerderRaw = """
            {"v":"KERI10JSON00012b_","t":"icp",\
            "d":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",\
            "i":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose","s":"0",\
            "kt":"1","k":["DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"],\
            "nt":"1","n":["EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"],\
            "bt":"0","b":[],"c":[],"a":[]}""";
        assertEquals(expectedSerderRaw, client.getController().getSerder().getRaw());
        assertEquals("0", client.getController().getSerder().getKed().get("s"));

        client.boot();

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/boot", request.getPath());
        assertEquals("application/json", request.getHeader("Content-Type"));

        String expectedRequestBody = """
            {"icp":{"v":"KERI10JSON00012b_","t":"icp",\
            "d":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",\
            "i":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose","s":"0",\
            "kt":"1","k":["DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"],\
            "nt":"1","n":["EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"],\
            "bt":"0","b":[],"c":[],"a":[]},\
            "sig":"AACJwsJ0mvb4VgxD87H4jIsiT1QtlzznUy9zrX3lGdd48jjQRTv8FxlJ8ClDsGtkvK4Eekg5p-oPYiPvK_1eTXEG",\
            "stem":"signify:controller","pidx":1,"tier":"low"}""";

        assertEquals(
            objectMapper.readTree(expectedRequestBody),
            objectMapper.readTree(request.getBody().readUtf8())
        );

        client.connect();

        // Verify the state HTTP request
        RecordedRequest stateRequest = mockWebServer.takeRequest();
        assertEquals("GET", stateRequest.getMethod());
        assertTrue(stateRequest.getPath().startsWith("/agent"));
//        assertEquals("application/json", stateRequest.getHeader("Content-Type"));

        // Validate agent
        assertEquals(
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
            client.getAgent().getPre()
        );
        assertEquals(
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
            client.getAgent().getAnchor()
        );
        assertEquals(
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
            client.getAgent().getSaid()
        );
        assertEquals("0", client.getAgent().getState().get("s"));
        assertEquals(
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
            client.getAgent().getState().get("d")
        );

        // Validate approve delegation
        assertEquals("1", client.getController().getSerder().getKed().get("s"));
        assertEquals("ixn", client.getController().getSerder().getKed().get("t"));

        @SuppressWarnings("unchecked")
        List<Object> actions = (List<Object>) client.getController().getSerder().getKed().get("a");
        Map<String, Object> actionMap = Utils.toMap(actions.getFirst());
        assertEquals(
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
            actionMap.get("i")
        );
        assertEquals(
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
            actionMap.get("d")
        );
        assertEquals("0", actionMap.get("s"));

        // Validate data
        Object[] data = client.getData();
        assertEquals(url, data[0]);
        assertEquals(bran, data[1]);

        // Validate service instances
        assertInstanceOf(Aiding.Identifier.class, client.getIdentifier());
        assertInstanceOf(Coring.Operations.class, client.getOperations());
        assertInstanceOf(Coring.KeyEvents.class, client.getKeyEvents());
        assertInstanceOf(Coring.KeyStates.class, client.getKeyStates());
        assertInstanceOf(Credentialing.Credentials.class, client.getCredentials());
        assertInstanceOf(Credentialing.Registries.class, client.getRegistries());
        assertInstanceOf(Credentialing.Schemas.class, client.getSchemas());
        assertInstanceOf(Contacting.Challenges.class, client.getChallenges());
        assertInstanceOf(Contacting.Contacts.class, client.getContacts());
        assertInstanceOf(Notifying.Notifications.class, client.getNotifications());
        assertInstanceOf(Escrowing.Escrows.class, client.getEscrows());
        assertInstanceOf(Coring.Oobis.class, client.getOobis());
        assertInstanceOf(Exchanging.Exchanges.class, client.getExchanges());
        assertInstanceOf(Grouping.Groups.class, client.getGroups());
    }

    @Test
    public void testJsonObject() throws Exception {
        final ObjectMapper obj = new ObjectMapper();
        final Map<String, Object> ICP_EVENT_OBJ = new LinkedHashMap<>() {{
            put("v", "KERI10JSON00012b_");
            put("t", "icp");
            put("d", "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose");
            put("i", "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose");
            put("s", "0");
            put("kt", "1");
            put("k", List.of("DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"));
            put("nt", "1");
            put("n", List.of("EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"));
            put("bt", "0");
            put("b", List.of());
            put("c", List.of());
            put("a", List.of());
        }};

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("icp", ICP_EVENT_OBJ);
        data.put("sig", "AACJwsJ0mvb4VgxD87H4jIsiT1QtlzznUy9zrX3lGdd48jjQRTv8FxlJ8ClDsGtkvK4Eekg5p-oPYiPvK_1eTXEG");
        data.put("stem", "signify:controller");
        data.put("pidx", 1);
        data.put("tier", Salter.Tier.low);

        String expectedData = "{\"icp\":{\"v\":\"KERI10JSON00012b_\",\"t\":\"icp\",\"d\":\"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose\",\"i\":\"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose\",\"s\":\"0\",\"kt\":\"1\",\"k\":[\"DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc\"],\"nt\":\"1\",\"n\":[\"EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL\"],\"bt\":\"0\",\"b\":[],\"c\":[],\"a\":[]},\"sig\":\"AACJwsJ0mvb4VgxD87H4jIsiT1QtlzznUy9zrX3lGdd48jjQRTv8FxlJ8ClDsGtkvK4Eekg5p-oPYiPvK_1eTXEG\",\"stem\":\"signify:controller\",\"pidx\":1,\"tier\":\"low\"}";
        assertEquals(obj.writeValueAsString(data), expectedData);
    }

}
