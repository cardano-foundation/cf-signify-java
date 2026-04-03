package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.cardanofoundation.signify.generated.keria.model.*;

import java.io.IOException;
import java.util.Map;

public class OperationDeserializer extends JsonDeserializer<Operation> implements ContextualDeserializer {

    private enum OperationType {
        CHALLENGE(PendingChallengeOperation.class, CompletedChallengeOperation.class, FailedChallengeOperation.class),
        CREDENTIAL(PendingCredentialOperation.class, CompletedCredentialOperation.class, FailedCredentialOperation.class),
        DELEGATION(PendingDelegationOperation.class, CompletedDelegationOperation.class, FailedDelegationOperation.class),
        DELEGATOR(PendingDelegatorOperation.class, CompletedDelegatorOperation.class, FailedDelegatorOperation.class),
        DONE(PendingDoneOperation.class, CompletedDoneOperation.class, FailedDoneOperation.class),
        ENDROLE(PendingEndRoleOperation.class, CompletedEndRoleOperation.class, FailedEndRoleOperation.class),
        EXCHANGE(PendingExchangeOperation.class, CompletedExchangeOperation.class, FailedExchangeOperation.class),
        GROUP(PendingGroupOperation.class, CompletedGroupOperation.class, FailedGroupOperation.class),
        LOCSCHEME(PendingLocSchemeOperation.class, CompletedLocSchemeOperation.class, FailedLocSchemeOperation.class),
        OOBI(PendingOOBIOperation.class, CompletedOOBIOperation.class, FailedOOBIOperation.class),
        QUERY(PendingQueryOperation.class, CompletedQueryOperation.class, FailedQueryOperation.class),
        REGISTRY(PendingRegistryOperation.class, CompletedRegistryOperation.class, FailedRegistryOperation.class),
        SUBMIT(PendingSubmitOperation.class, CompletedSubmitOperation.class, FailedSubmitOperation.class),
        WITNESS(PendingWitnessOperation.class, CompletedWitnessOperation.class, FailedWitnessOperation.class);

        final Class<? extends Operation> pending;
        final Class<? extends Operation> completed;
        final Class<? extends Operation> failed;

        OperationType(Class<? extends Operation> pending, Class<? extends Operation> completed, Class<? extends Operation> failed) {
            this.pending = pending;
            this.completed = completed;
            this.failed = failed;
        }
    }

    private static final Map<String, OperationType> PREFIX_MAP = Map.ofEntries(
            Map.entry("challenge", OperationType.CHALLENGE),
            Map.entry("credential", OperationType.CREDENTIAL),
            Map.entry("delegation", OperationType.DELEGATION),
            Map.entry("delegator", OperationType.DELEGATOR),
            Map.entry("done", OperationType.DONE),
            Map.entry("endrole", OperationType.ENDROLE),
            Map.entry("exchange", OperationType.EXCHANGE),
            Map.entry("group", OperationType.GROUP),
            Map.entry("locscheme", OperationType.LOCSCHEME),
            Map.entry("oobi", OperationType.OOBI),
            Map.entry("query", OperationType.QUERY),
            Map.entry("registry", OperationType.REGISTRY),
            Map.entry("submit", OperationType.SUBMIT),
            Map.entry("witness", OperationType.WITNESS)
    );

    private static final Map<Class<?>, OperationType> INTERFACE_MAP = Map.ofEntries(
            Map.entry(ChallengeOperation.class, OperationType.CHALLENGE),
            Map.entry(CredentialOperation.class, OperationType.CREDENTIAL),
            Map.entry(DelegationOperation.class, OperationType.DELEGATION),
            Map.entry(DelegatorOperation.class, OperationType.DELEGATOR),
            Map.entry(DoneOperation.class, OperationType.DONE),
            Map.entry(EndRoleOperation.class, OperationType.ENDROLE),
            Map.entry(ExchangeOperation.class, OperationType.EXCHANGE),
            Map.entry(GroupOperation.class, OperationType.GROUP),
            Map.entry(LocSchemeOperation.class, OperationType.LOCSCHEME),
            Map.entry(OOBIOperation.class, OperationType.OOBI),
            Map.entry(QueryOperation.class, OperationType.QUERY),
            Map.entry(RegistryOperation.class, OperationType.REGISTRY),
            Map.entry(SubmitOperation.class, OperationType.SUBMIT),
            Map.entry(WitnessOperation.class, OperationType.WITNESS)
    );

    private final OperationType fixedType;

    public OperationDeserializer() {
        this.fixedType = null;
    }

    private OperationDeserializer(OperationType fixedType) {
        this.fixedType = fixedType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType type = ctxt.getContextualType();
        if (type != null) {
            OperationType opType = INTERFACE_MAP.get(type.getRawClass());
            if (opType != null) {
                return new OperationDeserializer(opType);
            }
        }
        return this;
    }

    @Override
    public Operation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();

        OperationType opType = fixedType;
        if (opType == null) {
            String name = node.has("name") ? node.get("name").asText() : null;
            opType = resolveFromPrefix(name);
            if (opType == null) {
                throw new IOException("Cannot determine operation type from name: " + name
                        + ". Expected format: <type>.<identifier> where type is one of: " + PREFIX_MAP.keySet());
            }
        }

        // Determine state: response → completed, error → failed, otherwise → pending
        Class<? extends Operation> concreteType;
        if (node.has("response") && !node.get("response").isNull()) {
            if (opType == OperationType.DELEGATION && node.get("response").isTextual()) {
                concreteType = OperationType.DELEGATOR.completed;
            } else {
                concreteType = opType.completed;
            }
        } else if (node.has("error") && !node.get("error").isNull()) {
            concreteType = opType.failed;
        } else {
            concreteType = opType.pending;
        }

        JsonParser nodeParser = node.traverse(p.getCodec());
        nodeParser.nextToken();
        return ctxt.readValue(nodeParser, concreteType);
    }

    private static OperationType resolveFromPrefix(String name) {
        if (name == null || !name.contains(".")) {
            return null;
        }
        String prefix = name.substring(0, name.indexOf('.')).toLowerCase();
        return PREFIX_MAP.get(prefix);
    }
}
