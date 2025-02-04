package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChallengesTest extends BaseMockServerTest {
    @Test
    @DisplayName("Test Challenges")
    void testChallenges() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Contacting.Challenges challenges = client.challenges();

        challenges.generate(128);
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(url + "/challenges?strength=128", request.getRequestUrl().toString());

        List<String> words = List.of(
            "shell", "gloom", "mimic", "cereal", "stool", "furnace",
            "nominee", "nation", "sauce", "sausage", "rather", "venue"
        );

        challenges.respond("aid1", "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p", words);
        request = getRecordedRequests().getLast();
        assertEquals(url + "/identifiers/aid1/exchanges", request.getRequestUrl().toString());
        assertEquals("POST", request.getMethod());

        Map<String, Object> lastBody = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
        assertEquals("challenge", lastBody.get("tpc"));

        Map<String, Object> exn = Utils.toMap(lastBody.get("exn"));
        assertEquals("/challenge/response", exn.get("r"));
        assertEquals("ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK", exn.get("i"));

        Map<String, Object> a = Utils.toMap(exn.get("a"));
        assertEquals(words, a.get("words"));

        List<String> sigs = Utils.toList(lastBody.get("sigs"));
        assertEquals(88, sigs.getFirst().length());

        challenges.verify("EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p", words);
        request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals(
            url + "/challenges_verify/EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p",
            request.getRequestUrl().toString()
        );
        lastBody = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
        assertEquals(words, lastBody.get("words"));

        challenges.responded(
            "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p",
            "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao"
        );
        request = mockWebServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals(
            url + "/challenges_verify/EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p",
            request.getRequestUrl().toString()
        );
        lastBody = objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
        assertEquals("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", lastBody.get("said"));
    }
}
