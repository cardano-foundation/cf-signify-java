package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tolerant deserializer for generated Credential.
 *
 * Handles cases where ancatc can be either a String or an Array in the JSON response.
 */
public class CredentialDeserializer extends JsonDeserializer<Credential> {

    @Override
    public Credential deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final ObjectCodec codec = p.getCodec();
        final JsonNode node = codec.readTree(p);

        Credential out = new Credential();

        // Handle sad field
        JsonNode sadNode = node.get("sad");
        if (sadNode != null && !sadNode.isNull()) {
            out.setSad(codec.treeToValue(sadNode, CredentialSad.class));
        }

        // Handle atc field
        JsonNode atcNode = node.get("atc");
        if (atcNode != null && !atcNode.isNull()) {
            out.setAtc(atcNode.asText());
        }

        // Handle iss field
        JsonNode issNode = node.get("iss");
        if (issNode != null && !issNode.isNull()) {
            out.setIss(codec.treeToValue(issNode, IssEvent.class));
        }

        // Handle issatc field
        JsonNode issatcNode = node.get("issatc");
        if (issatcNode != null && !issatcNode.isNull()) {
            out.setIssatc(issatcNode.asText());
        }

        // Handle pre field
        JsonNode preNode = node.get("pre");
        if (preNode != null && !preNode.isNull()) {
            out.setPre(preNode.asText());
        }

        // Handle schema field
        JsonNode schemaNode = node.get("schema");
        if (schemaNode != null && !schemaNode.isNull()) {
            out.setSchema(codec.treeToValue(schemaNode, Schema.class));
        }

        // Handle chains field
        JsonNode chainsNode = node.get("chains");
        if (chainsNode != null && !chainsNode.isNull() && chainsNode.isArray()) {
            List<Map<String, Object>> chains = new ArrayList<>();
            for (JsonNode chainNode : chainsNode) {
                chains.add(codec.treeToValue(chainNode, Map.class));
            }
            out.setChains(chains);
        }

        // Handle status field
        JsonNode statusNode = node.get("status");
        if (statusNode != null && !statusNode.isNull()) {
            out.setStatus(codec.treeToValue(statusNode, CredentialState.class));
        }

        // Handle anchor field
        JsonNode anchorNode = node.get("anchor");
        if (anchorNode != null && !anchorNode.isNull()) {
            out.setAnchor(codec.treeToValue(anchorNode, Anchor.class));
        }

        // Handle anc field
        JsonNode ancNode = node.get("anc");
        if (ancNode != null && !ancNode.isNull()) {
            out.setAnc(codec.treeToValue(ancNode, CredentialAnc.class));
        }

        // Handle ancatc field - can be String or Array
        JsonNode ancatcNode = node.get("ancatc");
        if (ancatcNode != null && !ancatcNode.isNull()) {
            if (ancatcNode.isArray()) {
                // If it's an array, serialize it as a JSON string
                out.setAncatc(ancatcNode.toString());
            } else {
                // If it's a string, use it directly
                out.setAncatc(ancatcNode.asText());
            }
        }

        return out;
    }
}
