package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.CompletedDelegatorOperationDepends;
import org.cardanofoundation.signify.generated.keria.model.DelegatorOperationDepends;
import org.cardanofoundation.signify.generated.keria.model.PendingDelegatorOperationDepends;

import java.io.IOException;

public class DelegatorOperationDependsDeserializer extends JsonDeserializer<DelegatorOperationDepends> {

    @Override
    public DelegatorOperationDepends deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        boolean done = node.has("done") && node.get("done").asBoolean(false);
        Class<? extends DelegatorOperationDepends> target = done
                ? CompletedDelegatorOperationDepends.class
                : PendingDelegatorOperationDepends.class;

        JsonParser nodeParser = node.traverse(p.getCodec());
        nodeParser.nextToken();
        return ctxt.readValue(nodeParser, target);
    }
}
