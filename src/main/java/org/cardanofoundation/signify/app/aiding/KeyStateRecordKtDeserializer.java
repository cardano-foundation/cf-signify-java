package org.cardanofoundation.signify.app.aiding;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKtListString;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKtString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializer for KeyStateRecordKt sealed interface.
 * Accepts string, number, or array values and maps to the correct variant.
 * This is necessary because Jackson doesn't automatically deduce sealed types without explicit type info.
 */
public class KeyStateRecordKtDeserializer extends StdDeserializer<KeyStateRecordKt> {

    public KeyStateRecordKtDeserializer() {
        super(KeyStateRecordKt.class);
    }

    @Override
    public KeyStateRecordKt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        // Determine the correct sealed variant based on the JSON type
        if (node.isTextual()) {
            // String variant
            return new KeyStateRecordKtString(node.asText());
        }

        if (node.isNumber()) {
            // Number should be converted to string variant
            return new KeyStateRecordKtString(node.asText());
        }

        if (node.isArray()) {
            // Array variant
            List<String> values = new ArrayList<>();
            node.forEach(item -> values.add(item.asText()));
            return new KeyStateRecordKtListString(values);
        }

        throw new IllegalArgumentException("Unexpected type for KeyStateRecordKt: " + node.getNodeType());
    }
}
