package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.CredentialStateBisOrBrv;
import org.cardanofoundation.signify.generated.keria.model.CredentialState;
import org.cardanofoundation.signify.generated.keria.model.CredentialStateIssOrRev;
import org.cardanofoundation.signify.generated.keria.model.RaFields;
import org.cardanofoundation.signify.generated.keria.model.Seal;

import java.io.IOException;

/**
 * Tolerant deserializer for generated CredentialState.
 *
 * OpenAPI `oneOf` for CredentialState can carry et values from two branches:
 * - iss/rev
 * - bis/brv
 *
 * Generated model currently only accepts bis/brv in CredentialState.EtEnum.
 * This deserializer keeps deserialization resilient for both payload shapes.
 */
public class CredentialStateDeserializer extends JsonDeserializer<CredentialState> {

    @Override
    public CredentialState deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final ObjectCodec codec = p.getCodec();
        final JsonNode node = codec.readTree(p);

        CredentialState out = new CredentialState();

        JsonNode vnNode = node.get("vn");
        if (vnNode != null && !vnNode.isNull()) {
            out.setVn(codec.treeToValue(vnNode, Object.class));
        }

        setText(node, "i", out::setI);
        setText(node, "s", out::setS);
        setText(node, "d", out::setD);
        setText(node, "ri", out::setRi);
        setText(node, "dt", out::setDt);

        JsonNode aNode = node.get("a");
        if (aNode != null && !aNode.isNull()) {
            out.setA(codec.treeToValue(aNode, Seal.class));
        }

        JsonNode etNode = node.get("et");
        if (etNode != null && !etNode.isNull()) {
            String et = etNode.asText();
            switch (et) {
                case "bis", "brv" -> {
                    CredentialStateBisOrBrv.EtEnum.fromValue(et);
                    out.setEt(CredentialState.EtEnum.fromValue(et));
                }
                case "iss", "rev" -> {
                    CredentialStateIssOrRev.EtEnum.fromValue(et);
                    out.setEt(CredentialState.EtEnum.fromValue(et));
                }
                default -> {
                    // leave unset for unknown future values
                }
            }
        }

        JsonNode raNode = node.get("ra");
        if (raNode != null && raNode.isObject()) {
            // for iss/rev branch ra is often {}, so only map when fields are present
            boolean hasI = raNode.has("i") && !raNode.get("i").isNull();
            boolean hasS = raNode.has("s") && !raNode.get("s").isNull();
            boolean hasD = raNode.has("d") && !raNode.get("d").isNull();
            if (hasI && hasS && hasD) {
                out.setRa(codec.treeToValue(raNode, RaFields.class));
            }
        }

        return out;
    }

    private static void setText(JsonNode node, String field, ThrowingConsumer<String> setter) throws IOException {
        JsonNode value = node.get(field);
        if (value != null && !value.isNull()) {
            setter.accept(value.asText());
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T value) throws IOException;
    }
}
