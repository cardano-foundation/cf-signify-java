package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;

import java.io.IOException;
import java.util.List;

/**
 * Deserializes the polymorphic {@code kt}/{@code nt} fields into a {@link KtValue}.
 * KERIA returns these as either a plain string (unweighted) or an array of weights (weighted).
 */
class KeyStateRecordKtDeserializer extends JsonDeserializer<KeyStateRecordKt> {

    @Override
    public KeyStateRecordKt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        switch (p.currentToken()) {
            case VALUE_STRING:
            case VALUE_NUMBER_INT: // tolerated; signify-ts types thresholds as string | number
                return KtValue.unweighted(p.getValueAsString());
            case START_ARRAY:
                return KtValue.weighted(ctxt.readValue(p, List.class));
            default:
                return ctxt.reportInputMismatch(KeyStateRecordKt.class,
                    "Cannot deserialize kt/nt threshold from token %s; expected a string or an array of weights",
                    p.currentToken());
        }
    }
}
