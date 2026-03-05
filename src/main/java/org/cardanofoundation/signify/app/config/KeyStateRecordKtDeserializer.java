package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for KeyStateRecordKt (polymorphic kt/nt fields).
 * These fields can be either String or List<String> in JSON.
 * Returns the actual value (String or List) directly for use with Jackson mixin.
 */
public class KeyStateRecordKtDeserializer extends JsonDeserializer<Object> {
    
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    values.add(item.asText());
                }
            }
            return values;
        } else {
            return node.asText();
        }
    }
}
