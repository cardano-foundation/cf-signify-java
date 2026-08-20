package id.veridian.signify.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.RecordedRequest;
import id.veridian.signify.app.aiding.EventResult;
import id.veridian.signify.app.aiding.LocSchemeArgs;
import id.veridian.signify.app.clienting.SignifyClient;
import id.veridian.signify.cesr.util.Utils;
import id.veridian.signify.generated.keria.model.LocSchemeOperation;
import id.veridian.signify.generated.keria.model.PendingLocSchemeOperation;
import id.veridian.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class IdentifiersTest extends BaseMockServerTest {

    private static final String AID1_PREFIX = "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK";

    @Test
    @DisplayName("Test addLocScheme")
    void testAddLocScheme() throws InterruptedException, JsonProcessingException {
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        String eid = "EHgwVwQT15OJvilVvW57HE4w0-GPs_Stj2OFoAHZSysY";
        EventResult<LocSchemeOperation> result = client.identifiers().addLocScheme("aid1",
                LocSchemeArgs.builder()
                        .url("https://test.com")
                        .scheme("https")
                        .eid(eid)
                        .stamp("2021-06-27T21:26:21.233257+00:00")
                        .build());

        assertInstanceOf(PendingLocSchemeOperation.class, result.op());

        RecordedRequest request = getRecordedRequests().getLast();
        assertEquals("POST", request.getMethod());
        assertEquals(url + "/identifiers/aid1/locschemes", request.getRequestUrl().toString());

        Map<String, Object> body = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
        Map<String, Object> rpy = Utils.toMap(body.get("rpy"));
        assertEquals("rpy", rpy.get("t"));
        assertEquals("/loc/scheme", rpy.get("r"));
        assertEquals("2021-06-27T21:26:21.233257+00:00", rpy.get("dt"));
        assertEquals(
                Map.of("eid", eid, "url", "https://test.com", "scheme", "https"),
                Utils.toMap(rpy.get("a")));

        List<String> sigs = Utils.toList(body.get("sigs"));
        assertEquals(88, sigs.getFirst().length());
    }

    @Test
    @DisplayName("Test addLocScheme defaults to own prefix over http")
    void testAddLocScheme_defaults() throws InterruptedException, JsonProcessingException {
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        client.identifiers().addLocScheme("aid1", LocSchemeArgs.builder().url("http://test.com").build());

        RecordedRequest request = getRecordedRequests().getLast();
        assertEquals("POST", request.getMethod());
        assertEquals(url + "/identifiers/aid1/locschemes", request.getRequestUrl().toString());

        Map<String, Object> body = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
        Map<String, Object> rpy = Utils.toMap(body.get("rpy"));
        assertEquals(
                Map.of("eid", AID1_PREFIX, "url", "http://test.com", "scheme", "http"),
                Utils.toMap(rpy.get("a")));
    }
}
