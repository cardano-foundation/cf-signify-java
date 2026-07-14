package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.cardanofoundation.signify.generated.keria.model.Icp;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.Rot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThresholdTest {

    private static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        GeneratedModelConfig.configure(mapper);
        return mapper;
    }

    @Test
    @DisplayName("kt/nt deserialize to Threshold carrying the raw threshold")
    void deserializeThresholds() throws JsonProcessingException {
        KeyStateRecord state = mapper().readValue(
            "{\"kt\":\"1\",\"nt\":[\"1/2\",\"1/2\"]}", KeyStateRecord.class);

        assertEquals("1", Threshold.rawOf(state.getKt()));
        assertEquals(List.of("1/2", "1/2"), Threshold.rawOf(state.getNt()));
    }

    @Test
    @DisplayName("integer thresholds (keripy intive events) keep integer form; hex strings stay hex")
    void deserializeIntegerThreshold() throws JsonProcessingException {
        ObjectMapper mapper = mapper();
        // unweighted string thresholds are hex, so JSON 10 (ten) and "10" (sixteen) differ
        Icp icp = mapper.readValue("{\"kt\":10,\"nt\":\"10\"}", Icp.class);

        assertEquals(10, Threshold.rawOf(icp.getKt()));
        assertEquals("a", ((Threshold.Unweighted) icp.getKt()).threshold());
        assertEquals("10", Threshold.rawOf(icp.getNt()));
        assertEquals("10", ((Threshold.Unweighted) icp.getNt()).threshold());

        JsonNode out = mapper.valueToTree(icp);
        assertTrue(out.get("kt").isInt());
        assertEquals(10, out.get("kt").asInt());
        assertTrue(out.get("nt").isTextual());
        assertEquals("10", out.get("nt").asText());
    }

    @Test
    @DisplayName("KEL event models share the same threshold representation")
    void deserializeEventThresholds() throws JsonProcessingException {
        Icp icp = mapper().readValue("{\"kt\":\"1\",\"nt\":[\"1/2\",\"1/2\"]}", Icp.class);
        Rot rot = mapper().readValue("{\"kt\":[[\"1/2\",\"1/2\"],[\"1\"]],\"nt\":\"2\"}", Rot.class);

        assertEquals("1", Threshold.rawOf(icp.getKt()));
        assertEquals(List.of("1/2", "1/2"), Threshold.rawOf(icp.getNt()));
        assertEquals(List.of(List.of("1/2", "1/2"), List.of("1")), Threshold.rawOf(rot.getKt()));
        assertEquals("2", Threshold.rawOf(rot.getNt()));
    }

    @Test
    @DisplayName("nested multi-clause weighted thresholds are preserved")
    void deserializeNestedWeightedThreshold() throws JsonProcessingException {
        KeyStateRecord state = mapper().readValue(
            "{\"kt\":[[\"1/2\",\"1/2\"],[\"1\"]],\"nt\":\"2\"}", KeyStateRecord.class);

        assertEquals(List.of(List.of("1/2", "1/2"), List.of("1")), Threshold.rawOf(state.getKt()));
        assertEquals("2", Threshold.rawOf(state.getNt()));
    }

    @Test
    @DisplayName("distinct records keep distinct thresholds")
    void noCrossInstanceAliasing() throws JsonProcessingException {
        ObjectMapper mapper = mapper();
        KeyStateRecord first = mapper.readValue("{\"kt\":\"1\",\"nt\":\"2\"}", KeyStateRecord.class);
        KeyStateRecord second = mapper.readValue("{\"kt\":\"3\",\"nt\":\"4\"}", KeyStateRecord.class);

        assertEquals("1", Threshold.rawOf(first.getKt()));
        assertEquals("2", Threshold.rawOf(first.getNt()));
        assertEquals("3", Threshold.rawOf(second.getKt()));
        assertEquals("4", Threshold.rawOf(second.getNt()));
    }

    @Test
    @DisplayName("serialization round-trips the original kt/nt JSON")
    void serializeRoundTrip() throws JsonProcessingException {
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
    void discriminateViaSwitch() throws JsonProcessingException {
        KeyStateRecord state = mapper().readValue(
            "{\"kt\":\"2\",\"nt\":[\"1/2\",\"1/2\"]}", KeyStateRecord.class);

        Object kt = switch (state.getKt()) {
            case Threshold.Unweighted u -> u.threshold();
            case Threshold.Weighted w -> w.weights();
        };
        Object nt = switch (state.getNt()) {
            case Threshold.Unweighted u -> u.threshold();
            case Threshold.Weighted w -> w.weights();
        };

        assertEquals("2", kt);
        assertEquals(List.of("1/2", "1/2"), nt);
    }

    @Test
    @DisplayName("rawOf is null-safe for absent fields")
    void rawOfNullSafety() {
        assertNull(Threshold.rawOf(null));
    }

    @Test
    @DisplayName("unsupported JSON shapes fail loudly instead of returning an empty value")
    void rejectsUnsupportedShape() {
        assertThrows(MismatchedInputException.class, () ->
            mapper().readValue("{\"kt\":{\"oops\":true},\"nt\":\"1\"}", KeyStateRecord.class));
    }
}
