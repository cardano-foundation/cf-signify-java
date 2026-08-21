package id.veridian.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import id.veridian.signify.app.clienting.SignifyClient;
import id.veridian.signify.app.credentialing.credentials.CredentialData;
import id.veridian.signify.exception.SignifySerializationException;
import id.veridian.signify.app.credentialing.credentials.CredentialRecord;
import id.veridian.signify.app.credentialing.credentials.CredentialFilter;
import id.veridian.signify.app.credentialing.credentials.Credentials;
import id.veridian.signify.cesr.Salter;
import id.veridian.signify.cesr.Signer;
import id.veridian.signify.cesr.util.Utils;
import id.veridian.signify.core.Authenticater;
import id.veridian.signify.core.Httping;
import id.veridian.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CredentialingTest extends BaseMockServerTest {

    private String credentialBody = MOCK_CREDENTIAL;

    private static final String WIRE_ORDER_CREDENTIAL = """
        {
            "sad": {
                "v": "ACDC10JSON000197_",
                "d": "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",
                "s": "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao",
                "i": "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",
                "ri": "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df",
                "a": {
                    "d": "EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P",
                    "i": "EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby",
                    "dt": "2023-08-23T15:16:07.553000+00:00",
                    "LEI": "5493001KJTIIGC8Y1R17"
                },
                "xtra": "kept"
            },
            "pre": "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",
            "atc": "-ATC-acdc",
            "issatc": "-ATC-iss",
            "ancatc": ["-ATC-anc-first", "-ATC-anc-second"],
            "iss": {
                "v": "KERI10JSON0000ed_",
                "t": "iss",
                "d": "ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW",
                "i": "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",
                "s": "0",
                "ri": "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df",
                "dt": "2023-08-23T15:16:07.553000+00:00"
            },
            "anc": {
                "v": "KERI10JSON000160_",
                "t": "rot",
                "d": "EAncSaidRotationEventForWireOrderTestXXXXXXXX",
                "i": "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",
                "s": "1",
                "p": "EPrevDigestXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                "kt": "1",
                "k": ["DKey1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"],
                "nt": "1",
                "n": ["ENext1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"],
                "bt": "0",
                "br": [],
                "ba": [],
                "a": [{
                    "i": "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",
                    "s": "0",
                    "d": "ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW"
                }]
            },
            "chains": [],
            "rev": {
                "v": "KERI10JSON000120_",
                "t": "rev",
                "d": "ERevSaidXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                "i": "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",
                "s": "1",
                "ri": "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df",
                "p": "ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW",
                "dt": "2023-08-24T15:16:07.553000+00:00"
            },
            "revatc": "-ATC-rev",
            "status": {
                "v": "KERI10JSON000135_",
                "i": "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",
                "s": "0",
                "d": "ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW",
                "ri": "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df",
                "ra": {},
                "dt": "2023-08-23T15:16:07.553000+00:00",
                "et": "iss"
            }
        }""";

    @Override
    public MockResponse mockAllRequests(RecordedRequest req) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("signify-resource", "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei");
        headers.put(Httping.HEADER_SIG_TIME, Utils.currentDateTimeString());
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
                if (reqUrl.startsWith(url + "/credentials/query")) {
                        body = "[" + credentialBody + "]";
                } else if (reqUrl.startsWith(url + "/credentials/")) {
                        body = credentialBody;
                } else if (reqUrl.contains("/identifiers/aid1/credentials")) {
                        body = "DELETE".equals(req.getMethod())
                                ? "{\"name\": \"witness.EJ5EZpC_NjBKAPz8jzVUgRMQtyxpqsCKVefAFPSAVdSp\", \"done\": false, \"metadata\": {\"sn\": 2}}"
                                : "{\"name\": \"credential.EI6gHFuoUyqyB1MOJxBhab2EVUEt_3IYg2DqFI4Q/Ya5\", \"done\": false, \"metadata\": {\"ced\": {}}}";
                } else {
                        body = MOCK_GET_AID;
                }

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(202)
                .setBody(body);

        signedHeaderMap.forEach(mockResponse::addHeader);
        return mockResponse;
    }

    @Test
    @DisplayName("Test Credentialing")
    void testCredentialing() throws InterruptedException {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Credentials credentials = client.credentials();

        // Create the CredentialFilter object
        CredentialFilter kargs = CredentialFilter.builder()
                .filter(new HashMap<>() {{
                    put("-i", Collections.singletonMap("$eq", "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX"));
                }})
                .sort(Collections.singletonList(new HashMap<>() {{
                    put("-s", 1);
                }}))
                .limit(25)
                .skip(5)
                .build();

        credentials.list(kargs);
        RecordedRequest lastCall = mockWebServer.takeRequest();
        assertEquals("POST", lastCall.getMethod());
        assertEquals("/credentials/query", lastCall.getPath());
        assertEquals(Utils.jsonStringify(kargs), lastCall.getBody().readUtf8());


        credentials.get("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", true);
        lastCall = mockWebServer.takeRequest();
        assertEquals("GET", lastCall.getMethod());
        assertEquals(url + "/credentials/EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", lastCall.getRequestUrl().toString());

        String registry = "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX";
        String schema = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
        String isuee = "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p";

        CredentialData.CredentialSubject subject = CredentialData.CredentialSubject.builder()
                .i(isuee)
                .additionalProperties(new LinkedHashMap<>() {{
                    put("LEI", "1234");
                }})
                .build();

        CredentialData credentialData = CredentialData.builder()
                .ri(registry)
                .s(schema)
                .a(subject)
                .build();


        // test issue
        credentials.issue("aid1", credentialData);
        lastCall = getRecordedRequests().getLast();

        Map<String, Object> lastBody = Utils.fromJson(lastCall.getBody().readUtf8(), Map.class);
        Map<String, Object> acdc = (Map<String, Object>) lastBody.get("acdc");
        Map<String, Object> iss = (Map<String, Object>) lastBody.get("iss");
        Map<String, Object> ixn = (Map<String, Object>) lastBody.get("ixn");
        List<String> sigs = (List<String>) lastBody.get("sigs");

        assertEquals("POST", lastCall.getMethod());
        assertEquals("/identifiers/aid1/credentials", lastCall.getPath());
        assertEquals(acdc.get("ri"), registry);
        assertEquals(acdc.get("s"), schema);
        assertEquals(((Map<?, ?>) acdc.get("a")).get("i"), isuee);
        assertEquals(((Map<?, ?>) acdc.get("a")).get("LEI"), "1234");

        assertEquals(iss.get("s"), "0");
        assertEquals(iss.get("t"), "iss");
        assertEquals(iss.get("ri"), registry);
        assertEquals(iss.get("i"), acdc.get("d"));

        assertEquals(ixn.get("t"), "ixn");
        assertEquals(ixn.get("i"), acdc.get("i"));
        assertEquals(ixn.get("p"), acdc.get("i"));

        assertEquals(sigs.size(), 1);
        assertEquals(sigs.get(0).substring(0, 2), "AA");
        assertEquals(sigs.get(0).length(), 88);

        // test revoke
        String credential = acdc.get("i").toString();
        credentials.revoke("aid1", credential, null);

        lastCall = getRecordedRequests().getLast();
        assertEquals("DELETE", lastCall.getMethod());
        assertEquals(url + "/identifiers/aid1/credentials/" + credential, lastCall.getRequestUrl().toString());

        lastBody = Utils.fromJson(lastCall.getBody().readUtf8(), Map.class);

        Map<String, Object> rev = (Map<String, Object>) lastBody.get("rev");
        ixn = (Map<String, Object>) lastBody.get("ixn");
        sigs = (List<String>) lastBody.get("sigs");

        assertEquals(rev.get("t"), "rev");
        assertEquals(rev.get("s"), "1");
        assertEquals(rev.get("ri"), "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df");
        assertEquals(rev.get("i"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");

        assertEquals(ixn.get("t"), "ixn");
        assertEquals(ixn.get("i"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");
        assertEquals(ixn.get("p"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");

        assertEquals(sigs.size(), 1);
        assertEquals(sigs.get(0).substring(0, 2), "AA");
        assertEquals(sigs.get(0).length(), 88);

        // test state
        credentials.state("EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df", "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo").get();
        lastCall = getRecordedRequests().getLast();
        assertEquals("GET", lastCall.getMethod());
        assertEquals(url + "/registries/EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df/EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo", lastCall.getRequestUrl().toString());
        assertEquals(lastCall.getBody().readUtf8(), "");
    }

    @Test
    @DisplayName("get() keeps the credential's wire sad")
    void credentialGetKeepsWireSad() throws InterruptedException {
        CredentialRecord cred = fetchCredential(WIRE_ORDER_CREDENTIAL);

        assertEquals("EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo", cred.value().getSad().getD());

        List<String> wire = List.of("v", "d", "s", "i", "ri", "a", "xtra");
        Map<String, Object> acdcSad = (Map<String, Object>) cred.body().get("sad");
        assertEquals(wire, List.copyOf(acdcSad.keySet()));
        assertEquals("kept", acdcSad.get("xtra"));
        assertEquals(wire, topLevelKeyOrder(cred.acdc().getRaw()));

        assertEquals(
                List.of("v", "d", "i", "ri", "s", "a"),
                List.copyOf(Utils.toMap(cred.value().getSad()).keySet()));
        assertEquals("iss", cred.iss().getKed().get("t"));

        assertThrows(UnsupportedOperationException.class, () -> cred.body().put("pre", "x"));
    }

    @Test
    @DisplayName("a rotation-anchored anc keeps 'a' last, where the typed model would move it")
    void rotationAnchorKeepsWireOrder() throws InterruptedException {
        CredentialRecord cred = fetchCredential(WIRE_ORDER_CREDENTIAL);

        List<String> wire = List.of("v", "t", "d", "i", "s", "p", "kt", "k", "nt", "n", "bt", "br", "ba", "a");
        assertEquals(wire, topLevelKeyOrder(cred.anc().getRaw()));

        List<String> roundTripped = List.copyOf(Utils.toMap(cred.value().getAnc()).keySet());
        assertEquals(6, roundTripped.indexOf("a"));
        assertEquals(13, wire.indexOf("a"));
    }

    @Test
    @DisplayName("body keeps blocks the generated model has no field for")
    void bodyKeepsUnmodelledBlocks() throws InterruptedException {
        CredentialRecord cred = fetchCredential(WIRE_ORDER_CREDENTIAL);

        assertEquals("rev", ((Map<String, Object>) cred.body().get("rev")).get("t"));
        assertTrue(cred.body().containsKey("revatc"));
        assertFalse(Utils.toMap(cred.value()).containsKey("rev"));
    }

    @Test
    @DisplayName("attachments are reachable without going through the typed view")
    void credentialExposesAttachments() throws InterruptedException {
        CredentialRecord cred = fetchCredential(WIRE_ORDER_CREDENTIAL);

        assertEquals("-ATC-acdc", cred.acdcAttachment());
        assertEquals("-ATC-iss", cred.issAttachment());
        assertEquals("-ATC-anc-first", cred.ancAttachment());
    }

    @Test
    @DisplayName("a missing embed block names the field it could not find")
    void credentialMissingBlockFails() throws InterruptedException {
        CredentialRecord cred = fetchCredential(MOCK_CREDENTIAL);

        SignifySerializationException e = assertThrows(SignifySerializationException.class, cred::anc);
        assertTrue(e.getMessage().contains("'anc'"));

        assertNull(cred.acdcAttachment());
        assertNull(cred.issAttachment());
        assertNull(cred.ancAttachment());
    }

    private CredentialRecord fetchCredential(String body) throws InterruptedException {
        credentialBody = body;
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();
        return client.credentials()
                .get("EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo")
                .orElseThrow();
    }

    private static List<String> topLevelKeyOrder(String json) {
        return List.copyOf(Utils.fromJson(json, new TypeReference<Map<String, Object>>() {}).keySet());
    }
}
