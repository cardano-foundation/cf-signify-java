package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.CompletedRegistryOperationDepends;
import org.cardanofoundation.signify.generated.keria.model.PendingRegistryOperationDepends;
import org.cardanofoundation.signify.generated.keria.model.RegistryOperationDepends;

import java.io.IOException;

public class RegistryOperationDependsDeserializer extends JsonDeserializer<RegistryOperationDepends> {

    @Override
    public RegistryOperationDepends deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        boolean done = node.has("done") && node.get("done").asBoolean(false);
        Class<? extends RegistryOperationDepends> target = done
                ? CompletedRegistryOperationDepends.class
                : PendingRegistryOperationDepends.class;

        JsonParser nodeParser = node.traverse(p.getCodec());
        nodeParser.nextToken();
        return ctxt.readValue(nodeParser, target);
    }
}
