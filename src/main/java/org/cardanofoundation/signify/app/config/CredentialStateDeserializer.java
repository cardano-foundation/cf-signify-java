package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.CredentialState;
import org.cardanofoundation.signify.generated.keria.model.RaFields;
import org.cardanofoundation.signify.generated.keria.model.Seal;

import java.io.IOException;

/**
 * Custom deserializer for {@link CredentialState} that works around OpenAPI Generator limitations.
 * <p>
 * The generated {@code CredentialState} has {@code @JsonSubTypes} mapping to
 * {@code CredentialStateIssOrRev} and {@code CredentialStateBisOrBrv}, but those classes
 * don't extend {@code CredentialState}, so Jackson's polymorphic dispatch fails.
 * This deserializer bypasses that by deserializing directly into {@code CredentialState},
 * accepting all four {@code et} values (iss, rev, bis, brv) and both {@code ra} shapes.
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
            out.setEt(CredentialState.EtEnum.fromValue(etNode.asText()));
        }

        JsonNode raNode = node.get("ra");
        if (raNode != null && raNode.isObject()) {
            boolean hasFields = raNode.has("i") && raNode.has("s") && raNode.has("d");
            if (hasFields) {
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
