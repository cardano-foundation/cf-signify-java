package org.cardanofoundation.signify.app.aiding;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;

import java.io.IOException;

/**
 * Deserializer for KeyStateRecord that delegates to the default deserializer.
 * This is needed to ensure the CustomModule gets applied to KeyStateRecordKt fields.
 */
public class KeyStateRecordDeserializer extends StdDeserializer<KeyStateRecord> {

    public KeyStateRecordDeserializer() {
        super(KeyStateRecord.class);
    }

    @Override
    public KeyStateRecord deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // Use the default bean deserializer from the context
        return ctxt.readValue(p, KeyStateRecord.class);
    }
}
