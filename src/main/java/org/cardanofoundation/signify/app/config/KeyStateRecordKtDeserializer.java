package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

public class KeyStateRecordKtDeserializer extends JsonDeserializer<KeyStateRecordKt> {
    // WeakHashMap to avoid memory leaks (keys are GC'd when no longer referenced)
    private static final Map<KeyStateRecordKt, Object> rawValueMap = new WeakHashMap<>();

    public static Object getRawValue(KeyStateRecordKt instance) {
        return rawValueMap.get(instance);
    }

    @Override
    public KeyStateRecordKt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Object value;
        switch (p.currentToken()) {
            case VALUE_STRING:
                value = p.getValueAsString();
                break;
            case START_ARRAY:
                value = ctxt.readValue(p, Object.class); // parse array
                break;
            case START_OBJECT:
                value = ctxt.readValue(p, Object.class); // parse object
                break;
            default:
                value = null;
                break;
        }
        KeyStateRecordKt instance = new KeyStateRecordKt();
        rawValueMap.put(instance, value);
        return instance;
    }
}
