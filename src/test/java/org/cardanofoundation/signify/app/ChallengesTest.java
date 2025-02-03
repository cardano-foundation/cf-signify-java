package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

public class ChallengesTest extends BaseMockServerTest {
    @Test
    @DisplayName("Test Challenges")
    void testChallenges() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Contacting.Challenges challenges = client.getChallenges();

        challenges.generate(128);
        Mockito.verify(client).fetch(
            eq("/challenges?strength=128"),
            eq("GET"),
            isNull(),
            any()
        );

        List<String> words = List.of(
            "shell", "gloom", "mimic", "cereal", "stool", "furnace",
            "nominee", "nation", "sauce", "sausage", "rather", "venue"
        );

        challenges.respond("aid1", "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p", words);
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(client).fetch(
            eq("/identifiers/aid1/exchanges"),
            eq("POST"),
            bodyCaptor.capture(),
            any()
        );

        Map<String, Object> lastBody = Utils.fromJson(Utils.jsonStringify(bodyCaptor.getValue()), new TypeReference<>() {});
        assertEquals("challenge", lastBody.get("tpc"));

        Map<String, Object> exn = Utils.toMap(lastBody.get("exn"));
        assertEquals("/challenge/response", exn.get("r"));
        assertEquals("ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK", exn.get("i"));

        Map<String, Object> a = Utils.toMap(exn.get("a"));
        assertEquals(words, a.get("words"));

        List<String> sigs = Utils.toList(lastBody.get("sigs"));
        assertEquals(88, sigs.getFirst().length());

        challenges.verify("EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p", words);
        bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(client).fetch(
            eq("/challenges_verify/EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p"),
            eq("POST"),
            bodyCaptor.capture(),
            any()
        );
        lastBody = Utils.fromJson(Utils.jsonStringify(bodyCaptor.getValue()), new TypeReference<>() {});
        assertEquals(words, lastBody.get("words"));

        challenges.responded(
            "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p",
            "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao"
        );
        bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(client).fetch(
            eq("/challenges_verify/EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p"),
            eq("PUT"),
            bodyCaptor.capture(),
            any()
        );
        lastBody = Utils.fromJson(Utils.jsonStringify(bodyCaptor.getValue()), new TypeReference<>() {});
        assertEquals("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", lastBody.get("said"));
    }
}
