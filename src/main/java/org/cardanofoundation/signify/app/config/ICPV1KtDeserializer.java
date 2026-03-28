package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.ICPV1Kt;

import java.io.IOException;

/**
 * Deserializes the polymorphic {@code kt}/{@code nt} fields typed as {@link ICPV1Kt}.
 * KERIA returns these as either a plain string (unweighted) or an array of strings (weighted).
 * The value is consumed but not yet exposed; update this when callers need access to the threshold.
 */
class ICPV1KtDeserializer extends JsonDeserializer<ICPV1Kt> {

    @Override
    public ICPV1Kt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        // Consume the token (string or array) — value not yet exposed via ICPV1Kt
        // TODO: expose kt/nt value once callers need it (same pattern as KtValue for KeyStateRecordKt)
        return new ICPV1Kt();
    }
}
