package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KtValueTest {

    private static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        GeneratedModelConfig.configure(mapper);
        return mapper;
    }

    @Test
    @DisplayName("kt/nt deserialize to KtValue carrying the raw threshold")
    void deserializeThresholds() throws Exception {
        KeyStateRecord state = mapper().readValue(
            "{\"kt\":\"1\",\"nt\":[\"1/2\",\"1/2\"]}", KeyStateRecord.class);

        assertEquals("1", KtValue.rawOf(state.getKt()));
        assertEquals(List.of("1/2", "1/2"), KtValue.rawOf(state.getNt()));
    }

    @Test
    @DisplayName("nested multi-clause weighted thresholds are preserved")
    void deserializeNestedWeightedThreshold() throws Exception {
        KeyStateRecord state = mapper().readValue(
            "{\"kt\":[[\"1/2\",\"1/2\"],[\"1\"]],\"nt\":\"2\"}", KeyStateRecord.class);

        assertEquals(List.of(List.of("1/2", "1/2"), List.of("1")), KtValue.rawOf(state.getKt()));
        assertEquals("2", KtValue.rawOf(state.getNt()));
    }

    @Test
    @DisplayName("distinct records keep distinct thresholds")
    void noCrossInstanceAliasing() throws Exception {
        ObjectMapper mapper = mapper();
        KeyStateRecord first = mapper.readValue("{\"kt\":\"1\",\"nt\":\"2\"}", KeyStateRecord.class);
        KeyStateRecord second = mapper.readValue("{\"kt\":\"3\",\"nt\":\"4\"}", KeyStateRecord.class);

        assertEquals("1", KtValue.rawOf(first.getKt()));
        assertEquals("2", KtValue.rawOf(first.getNt()));
        assertEquals("3", KtValue.rawOf(second.getKt()));
        assertEquals("4", KtValue.rawOf(second.getNt()));
    }

    @Test
    @DisplayName("serialization round-trips the original kt/nt JSON")
    void serializeRoundTrip() throws Exception {
        ObjectMapper mapper = mapper();
        KeyStateRecord state = mapper.readValue(
            "{\"kt\":\"1\",\"nt\":[\"1/2\",\"1/2\"]}", KeyStateRecord.class);

        JsonNode out = mapper.valueToTree(state);
        assertTrue(out.get("kt").isTextual());
        assertEquals("1", out.get("kt").asText());
        assertTrue(out.get("nt").isArray());
        assertEquals(List.of("1/2", "1/2"), mapper.convertValue(out.get("nt"), List.class));
    }

    @Test
    @DisplayName("weighted and unweighted thresholds discriminate via exhaustive switch")
    void discriminateViaSwitch() throws Exception {
        KeyStateRecord state = mapper().readValue(
            "{\"kt\":\"2\",\"nt\":[\"1/2\",\"1/2\"]}", KeyStateRecord.class);

        Object kt = switch (KtValue.of(state.getKt())) {
            case KtValue.Unweighted u -> u.threshold();
            case KtValue.Weighted w -> w.weights();
        };
        Object nt = switch (KtValue.of(state.getNt())) {
            case KtValue.Unweighted u -> u.threshold();
            case KtValue.Weighted w -> w.weights();
        };

        assertEquals("2", kt);
        assertEquals(List.of("1/2", "1/2"), nt);
    }

    @Test
    @DisplayName("rawOf is null-safe for absent or non-KtValue fields")
    void rawOfNullSafety() {
        assertNull(KtValue.rawOf(null));
        assertNull(KtValue.rawOf(new org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt()));
    }

    @Test
    @DisplayName("unsupported JSON shapes fail loudly instead of returning an empty value")
    void rejectsUnsupportedShape() {
        assertThrows(MismatchedInputException.class, () ->
            mapper().readValue("{\"kt\":{\"oops\":true},\"nt\":\"1\"}", KeyStateRecord.class));
    }
}
