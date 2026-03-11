package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;
import org.cardanofoundation.signify.generated.keria.model.KtValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes the polymorphic {@code kt}/{@code nt} fields into a {@link KtValue}.
 * KERIA returns these as either a plain string (unweighted) or an array of strings (weighted).
 */
class KeyStateRecordKtDeserializer extends JsonDeserializer<KeyStateRecordKt> {

    @Override
    public KeyStateRecordKt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isTextual()) {
            return KtValue.unweighted(node.asText());
        }

        if (node.isArray()) {
            List<String> weights = new ArrayList<>();
            for (JsonNode item : node) {
                weights.add(item.asText());
            }
            return KtValue.weighted(weights);
        }

        return KtValue.unweighted(node.asText());
    }
}
