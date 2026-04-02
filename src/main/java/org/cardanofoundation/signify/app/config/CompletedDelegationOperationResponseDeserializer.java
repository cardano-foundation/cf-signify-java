package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.cardanofoundation.signify.generated.keria.model.CompletedDelegationOperationResponse;
import org.cardanofoundation.signify.generated.keria.model.ICPV1Kt;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;

import java.io.IOException;

/**
 * Deserializes {@link CompletedDelegationOperationResponse} handling both object and string forms.
 * KERIA returns a full key-event object for delegation inception but a plain SAID string
 * for delegation approval (IXN) operations.
 */
class CompletedDelegationOperationResponseDeserializer extends JsonDeserializer<CompletedDelegationOperationResponse> {

    /**
     * Mapper without our CompletedDelegationOperationResponse deserializer (avoids recursion)
     * but with all other custom deserializers needed for nested fields (e.g. ICPV1Kt).
     */
    private static final ObjectMapper FALLBACK_MAPPER;

    static {
        FALLBACK_MAPPER = new ObjectMapper();
        FALLBACK_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule("FallbackModule");
        module.addDeserializer(ICPV1Kt.class, new ICPV1KtDeserializer());
        module.addDeserializer(KeyStateRecordKt.class, new KeyStateRecordKtDeserializer());
        FALLBACK_MAPPER.registerModule(module);
    }

    @Override
    public CompletedDelegationOperationResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            CompletedDelegationOperationResponse response = new CompletedDelegationOperationResponse();
            response.setD(p.getText());
            return response;
        }
        // Use a mapper with nested custom deserializers but without ours to avoid infinite recursion
        JsonNode tree = p.getCodec().readTree(p);
        return FALLBACK_MAPPER.treeToValue(tree, CompletedDelegationOperationResponse.class);
    }
}
