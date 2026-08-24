package id.veridian.signify.app;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import id.veridian.signify.app.clienting.SignifyClient;
import id.veridian.signify.app.credentialing.credentials.CredentialData;
import id.veridian.signify.app.credentialing.credentials.CredentialFilter;
import id.veridian.signify.app.credentialing.credentials.Credentials;
import id.veridian.signify.cesr.Salter;
import id.veridian.signify.cesr.Signer;
import id.veridian.signify.cesr.util.Utils;
import id.veridian.signify.core.Authenticater;
import id.veridian.signify.core.Httping;
import id.veridian.signify.generated.keria.model.Credential;
import id.veridian.signify.generated.keria.model.Tier;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CredentialingTest extends BaseMockServerTest {

    private static final String CESR_SAID = "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo";
    private static final String MISSING_SAID = "EAbsentAbsentAbsentAbsentAbsentAbsentAbsentA";

    private static final String ISSUER_ICP = """
        {"v":"KERI10JSON0000fd_","t":"icp","d":"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",\
        "i":"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1","s":"0","kt":"1",\
        "k":["DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"],"nt":"1",\
        "n":["EIf-ENw7PrM52w4H-S7NGU2qVIfraXVIlV9hEAaMHg7W"],"bt":"0","b":[],"c":[],"a":[]}""";

    private static final String ISSUER_ICP_ATTACHMENT =
        "-VAn-AABAAB6P97kZ3al3V3z3VusdI-QsHDvOnJRXBH0aBLcgMYtIzJlDtR1KrqfgnBmDlBpXNCwlv6L6RtOxbxDXoBSTZUL";

    private static final String ACDC = """
        {"v":"ACDC10JSON000197_","d":"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo",\
        "i":"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1",\
        "ri":"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df",\
        "s":"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao",\
        "a":{"d":"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P",\
        "i":"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby",\
        "dt":"2023-08-23T15:16:07.553000+00:00","LEI":"5493001KJTIIGC8Y1R17"}}""";

    private static final String ACDC_ATTACHMENT =
        "-IABEMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo0AAAAAAAAAAAAAAAAAAAAAAAENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW";

    private static final String CESR_STREAM =
        ISSUER_ICP + ISSUER_ICP_ATTACHMENT + ACDC + ACDC_ATTACHMENT;

    private Credentials connect() throws IOException, InterruptedException {
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();
        return client.credentials();
    }


    @Override
    public MockResponse mockAllRequests(RecordedRequest req) {
        if (req.getRequestUrl().toString().startsWith(url + "/credentials/" + MISSING_SAID)) {
            return new MockResponse().setResponseCode(404).setBody("");
        }

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
                        body = "[" + MOCK_CREDENTIAL + "]";
                } else if (reqUrl.startsWith(url + "/credentials/")) {
                        body = "application/json+cesr".equals(req.getHeader("Accept"))
                                ? CESR_STREAM
                                : MOCK_CREDENTIAL;
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


        credentials.getCESR("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao");
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
    @DisplayName("getCESR returns the whole stream, not just its first object")
    void getCESRReturnsWholeStream() throws IOException, InterruptedException {
        Credentials credentials = connect();

        Optional<String> cesr = credentials.getCESR(CESR_SAID);

        assertTrue(cesr.isPresent());
        assertEquals(CESR_STREAM, cesr.get());
        assertTrue(cesr.get().endsWith(ACDC_ATTACHMENT));

        RecordedRequest lastCall = mockWebServer.takeRequest();
        assertEquals("GET", lastCall.getMethod());
        assertEquals(url + "/credentials/" + CESR_SAID, lastCall.getRequestUrl().toString());
        assertEquals("application/json+cesr", lastCall.getHeader("Accept"));
    }

    @Test
    @DisplayName("get asks for JSON and returns the typed credential")
    void getSendsJsonAcceptHeader() throws IOException, InterruptedException {
        Credentials credentials = connect();

        Optional<Credential> cred = credentials.get(CESR_SAID);

        assertTrue(cred.isPresent());
        assertEquals(CESR_SAID, cred.get().getSad().getD());

        RecordedRequest lastCall = mockWebServer.takeRequest();
        assertEquals("application/json", lastCall.getHeader("Accept"));
    }

    @Test
    @DisplayName("The deprecated overload still serves the JSON branch on false")
    @SuppressWarnings("deprecation")
    void deprecatedOverloadServesJsonOnFalse() throws IOException, InterruptedException {
        Credentials credentials = connect();

        Optional<Credential> cred = credentials.get(CESR_SAID, false);

        assertTrue(cred.isPresent());
        assertEquals(CESR_SAID, cred.get().getSad().getD());
        assertNotNull(cred.get().getStatus());

        RecordedRequest lastCall = mockWebServer.takeRequest();
        assertEquals("application/json", lastCall.getHeader("Accept"));
    }

    // true asks for a different body, not a different encoding: the CESR stream carries the
    // issuer/subject KELs and the registry/credential TELs that the JSON branch omits. Answering
    // with the JSON branch would silently return less than was asked for.
    @Test
    @DisplayName("The deprecated overload refuses true instead of quietly serving JSON")
    @SuppressWarnings("deprecation")
    void deprecatedOverloadRejectsIncludeCesr() throws IOException, InterruptedException {
        Credentials credentials = connect();
        int before = mockWebServer.getRequestCount();

        UnsupportedOperationException thrown = assertThrows(
            UnsupportedOperationException.class,
            () -> credentials.get(CESR_SAID, true)
        );

        assertTrue(thrown.getMessage().contains("getCESR"));
        assertEquals(before, mockWebServer.getRequestCount());
    }

    @Test
    @DisplayName("getCESR maps a 404 to an empty Optional")
    void getCESRReturnsEmptyOn404() throws IOException, InterruptedException {
        Credentials credentials = connect();

        assertTrue(credentials.getCESR(MISSING_SAID).isEmpty());
    }

    // Pins the defect #118 describes: parsing the stream as a Credential neither throws nor
    // yields anything usable, so the CESR path must stay a separate, string-returning method.
    @Test
    @DisplayName("Parsing a CESR stream as a Credential silently keeps only the first object")
    void jsonParseOfCesrStreamKeepsOnlyFirstObject() {
        Credential parsed = Utils.fromJson(CESR_STREAM, Credential.class);

        assertNotNull(parsed);
        assertAll(
            () -> assertNull(parsed.getSad()),
            () -> assertNull(parsed.getIss()),
            () -> assertNull(parsed.getAnc()),
            () -> assertNull(parsed.getStatus()),
            () -> assertNull(parsed.getPre()),
            () -> assertNull(parsed.getSchema()),
            () -> assertNull(parsed.getAtc())
        );

        Map<String, Object> firstObject = Utils.fromJson(
            CESR_STREAM.substring(0, CESR_STREAM.indexOf(ISSUER_ICP_ATTACHMENT)),
            new TypeReference<Map<String, Object>>() {}
        );
        assertEquals("icp", firstObject.get("t"));
    }
}
