package org.cardanofoundation.signify.app.aiding;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.app.config.GeneratedModelConfig;

import java.io.IOException;

/**
 * Allows kt/nt fields to arrive as arrays/objects and coerces them to strings for the generated model.
 */
public class KeyStateRecordDeserializer extends StdDeserializer<KeyStateRecord> {

    private static final ObjectMapper delegate = GeneratedModelConfig.baseMapper();

    public KeyStateRecordDeserializer() {
        super(KeyStateRecord.class);
    }

    @Override
    public KeyStateRecord deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode node = mapper.readTree(p);

        coerceToString(node, "kt");
        coerceToString(node, "nt");

        return delegate.treeToValue(node, KeyStateRecord.class);
    }

    private void coerceToString(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null) {
            return;
        }
        if (value.isTextual()) {
            return;
        }
        node.put(field, value.toString());
    }
}
