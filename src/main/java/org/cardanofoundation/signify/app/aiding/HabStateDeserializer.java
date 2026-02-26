package org.cardanofoundation.signify.app.aiding;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cardanofoundation.signify.generated.keria.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Deserializer for HabState sealed interface.
 * Determines which implementation (HabStateOneOf, HabStateOneOf1, HabStateOneOf2, HabStateOneOf3) based on discriminator field.
 */
public class HabStateDeserializer extends StdDeserializer<HabState> {

    public HabStateDeserializer() {
        super(HabState.class);
    }

    @Override
    public HabState deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        if (node == null || node.isNull()) {
            return null;
        }
        
        // Create a simple ObjectMapper for sub-objects - avoid recursion
        ObjectMapper simpleMapper = new ObjectMapper();
        simpleMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Determine which implementation to use based on discriminator field
        if (node.has("salty")) {
            return simpleMapper.treeToValue(node, HabStateOneOf.class);
        } else if (node.has("randy")) {
            return simpleMapper.treeToValue(node, HabStateOneOf1.class);
        } else if (node.has("group")) {
            return simpleMapper.treeToValue(node, HabStateOneOf2.class);
        } else if (node.has("extern")) {
            return simpleMapper.treeToValue(node, HabStateOneOf3.class);
        }
        
        // If no discriminator found, default to HabStateOneOf since it's the most common
        return simpleMapper.treeToValue(node, HabStateOneOf.class);
    }
}
