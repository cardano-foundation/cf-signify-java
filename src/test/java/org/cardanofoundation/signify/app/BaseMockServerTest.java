package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.core.Authenticater;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.core.Httping;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BaseMockServerTest {
    public MockWebServer mockWebServer;
    public String url = "http://127.0.0.1:3901";
    public String bootUrl = "http://127.0.0.1:3903";
    public final String bran = "0123456789abcdefghijk";
    public final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        bootUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
        url = mockWebServer.url("/").toString().replaceAll("/$", "");

        setUpNormalDispatcher();
    }

    void setUpNormalDispatcher() {
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
                // Handle bad request headers with invalid signature
                else if (requestUrl.startsWith(url + "/invalid-signature")) {
                    return mockBadRequestHeader(false);
                }
                // Handle bad request headers with different remote agent
                else if (requestUrl.startsWith(url + "/different-remote-agent")) {
                    return mockBadRequestHeader(true);
                } else {
                    try {
                        return mockAllRequests(request);
                    } catch (LibsodiumException e) {
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

    public static final String MOCK_CONNECT = """
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

    public static final String MOCK_GET_AID = """
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

    public static final String MOCK_KEY_STATE = """
        {
            "vn": [1, 0],
            "i": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            "s": "0",
            "p": "",
            "d": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
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
                "d": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
                "br": [],
                "ba": []
            },
            "di": ""
        }""";

    public static final String MOCK_KEY_STATES_ARRAY = """
        [
            {
                "vn": [1, 0],
                "i": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
                "s": "0",
                "p": "",
                "d": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
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
                    "d": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
                    "br": [],
                    "ba": []
                },
                "di": ""
            },
            {
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
            }
        ]""";

    public static final String MOCK_KEY_EVENT = """
        {
            "v": "KERI10JSON00012b_",
            "t": "icp",
            "d": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            "i": "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX",
            "s": "0",
            "kt": "1",
            "k": ["DPmhSfdhCPxr3EqjxzEtF8TVy0YX7ATo0Uc8oo2cnmY9"],
            "nt": "1",
            "n": ["EAORnRtObOgNiOlMolji-KijC_isa3lRDpHCsol79cOc"],
            "bt": "0",
            "b": [],
            "c": [],
            "a": []
        }""";

    public static final String MOCK_CREDENTIAL = """
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
                Tier.LOW,
                false
        );

        Authenticater authn = new Authenticater(signer, signer.getVerfer());
        Map<String, String> signedHeaderMap = authn.sign(
                headers,
                req.getMethod(),
                req.getPath().split("\\?")[0],
                null
        );

        String body;
        if (reqUrl.startsWith(url + "/identifiers/aid1/credentials")) {
            body = MOCK_CREDENTIAL;
        } else if (reqUrl.startsWith(url + "/events")) {
            body = MOCK_KEY_EVENT;
        } else if (reqUrl.startsWith(url + "/states")) {
            // Return array if multiple prefixes in query params, single object otherwise
            String query = req.getRequestUrl().query();
            long preCount = query != null ? query.split("pre=").length - 1 : 0;
            body = preCount > 1 ? MOCK_KEY_STATES_ARRAY : MOCK_KEY_STATE;
        } else {
            body = MOCK_GET_AID;
        }

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(202)
                .setBody(body);

        signedHeaderMap.forEach(mockResponse::addHeader);
        return mockResponse;
    }

    MockResponse mockBadRequestHeader(boolean differentRemoteAgent) {
        Map<String, String> badAgentHeaders = new LinkedHashMap<>();
        if (differentRemoteAgent) {
            badAgentHeaders.put("signify-resource", "bad_resource");
        } else {
            badAgentHeaders.put("signify-resource", "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei");
        }
        badAgentHeaders.put(Httping.HEADER_SIG_TIME, "2023-08-20T15:34:31.534673+00:00");
        badAgentHeaders.put(Httping.HEADER_SIG_INPUT,
                "signify=(\"signify-resource\" \"@method\" \"@path\" \"signify-timestamp\");created=1692545671;keyid=\"EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei\";alg=\"ed25519\"");
        badAgentHeaders.put("signature",
                "indexed=\"?0\";signify=\"0BDiSoxCv42h2BtGMHy_tpWAqyCgEoFwRa8bQy20mBB2D5Vik4gRp3XwkEHtqy6iy6SUYAytMUDtRbewotAfkCgN\"");
        badAgentHeaders.put("content-type", "application/json");

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(202)
                .setBody("");

        badAgentHeaders.forEach(mockResponse::addHeader);
        return mockResponse;
    }


    MockResponse mockConnect() {
        return new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", "application/json")
            .setBody(MOCK_CONNECT);
    }

    MockResponse mockGetAID() {
        return new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", "application/json")
            .setBody(MOCK_GET_AID);
    }

    MockResponse mockCredential() {
        return new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", "application/json")
            .setBody(MOCK_CREDENTIAL);
    }

    void cleanUpRequest() throws InterruptedException {
        while (true) {
            if (mockWebServer.takeRequest(200, TimeUnit.MILLISECONDS) == null) {
                break;
            }
        }
    }

    List<RecordedRequest> getRecordedRequests() throws InterruptedException {
        List<RecordedRequest> recordedRequests = new LinkedList<>();
        while (true) {
            RecordedRequest request = mockWebServer.takeRequest(200, TimeUnit.MILLISECONDS);
            if (request == null) {
                break;
            } else {
                recordedRequests.add(request);
            }
        }
        return recordedRequests;
    }
}