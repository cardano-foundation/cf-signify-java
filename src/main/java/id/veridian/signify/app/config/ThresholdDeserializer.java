package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.List;

/**
 * Deserializes the polymorphic {@code kt}/{@code nt} fields into a {@link Threshold}.
 * KERIA returns these as either a plain string (unweighted) or an array of weights (weighted).
 */
class ThresholdDeserializer extends JsonDeserializer<Threshold> {

    @Override
    public Threshold deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        switch (p.currentToken()) {
            case VALUE_STRING:
                return Threshold.unweighted(p.getText());
            case VALUE_NUMBER_INT:
                // KERIA always emits strings, but KERI v1 events made with keripy's
                // intive=True carry integer thresholds; kept as an integer because the
                // string form is hex (stringifying 10 would be re-read as sixteen)
                return Threshold.unweighted(p.getIntValue());
            case START_ARRAY:
                return Threshold.weighted(ctxt.readValue(p, List.class));
            default:
                return ctxt.reportInputMismatch(Threshold.class,
                    "Cannot deserialize kt/nt threshold from token %s; expected a string or an array of weights",
                    p.currentToken());
        }
    }
}
