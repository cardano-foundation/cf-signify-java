package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.cardanofoundation.signify.generated.keria.model.Credential;
import org.cardanofoundation.signify.generated.keria.model.CredentialState;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;
import org.openapitools.jackson.nullable.JsonNullableModule;

/**
 * Centralizes Jackson configuration for OpenAPI-generated models.
 */
public final class GeneratedModelConfig {
    private GeneratedModelConfig() {
    }

    public static ObjectMapper mapper() {
        ObjectMapper mapper = baseMapper();
        mapper.addMixIn(KeyStateRecord.class, KeyStateRecordMixin.class);
        mapper.addMixIn(CredentialState.class, CredentialStateMixin.class);
        mapper.addMixIn(Credential.class, CredentialMixin.class);
        mapper.registerModule(generatedModule());
        return mapper;
    }

    /**
     * Apply the generated-model settings to an existing mapper.
     */
    public static void configure(ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.addMixIn(KeyStateRecord.class, KeyStateRecordMixin.class);
        mapper.addMixIn(CredentialState.class, CredentialStateMixin.class);
        mapper.addMixIn(Credential.class, CredentialMixin.class);
        mapper.registerModule(new JsonNullableModule());
        mapper.registerModule(generatedModule());
    }

    /**
     * Base mapper with nullable module but without the custom generated module.
     * Useful for internal delegates to avoid recursive deserializer lookups.
     */
    public static ObjectMapper baseMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JsonNullableModule());
        return mapper;
    }

    private static Module generatedModule() {
        SimpleModule module = new SimpleModule("GeneratedModelModule");
        module.addDeserializer(KeyStateRecordKt.class, new KeyStateRecordKtDeserializer());
        // TODO: register deserializer for ICPV1Kt.class (same pattern) when any schema that uses
        //  it (ICPV1/V2, ROTV1/V2, DIPV1/V2, DRTV1/V2, CredentialAnc, ControllerEe) needs kt/nt
        //  accessed in non-generated code.
        return module;
    }
}
