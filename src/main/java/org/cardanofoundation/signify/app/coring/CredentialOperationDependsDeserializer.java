package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.CompletedCredentialOperationDepends;
import org.cardanofoundation.signify.generated.keria.model.CredentialOperationDepends;
import org.cardanofoundation.signify.generated.keria.model.PendingCredentialOperationDepends;

import java.io.IOException;

public class CredentialOperationDependsDeserializer extends JsonDeserializer<CredentialOperationDepends> {

    @Override
    public CredentialOperationDepends deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        boolean done = node.has("done") && node.get("done").asBoolean(false);
        Class<? extends CredentialOperationDepends> target = done
                ? CompletedCredentialOperationDepends.class
                : PendingCredentialOperationDepends.class;

        JsonParser nodeParser = node.traverse(p.getCodec());
        nodeParser.nextToken();
        return ctxt.readValue(nodeParser, target);
    }
}
