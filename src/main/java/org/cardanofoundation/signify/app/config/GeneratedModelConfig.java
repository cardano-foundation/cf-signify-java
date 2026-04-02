package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.cardanofoundation.signify.generated.keria.model.CredentialState;
import org.cardanofoundation.signify.generated.keria.model.CompletedDelegationOperationResponse;
import org.cardanofoundation.signify.generated.keria.model.ICPV1Kt;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;
import org.openapitools.jackson.nullable.JsonNullableModule;

/**
 * Centralizes Jackson configuration for OpenAPI-generated models.
 */
public final class GeneratedModelConfig {
    private GeneratedModelConfig() {
    }

    public static void configure(ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.addMixIn(CredentialState.class, CredentialStateMixin.class);
        mapper.registerModule(new JsonNullableModule());
        mapper.registerModule(new InsertionOrderAdditionalPropertiesModule());

        SimpleModule module = new SimpleModule("GeneratedModelModule");
        module.addDeserializer(ICPV1Kt.class, new ICPV1KtDeserializer());
        module.addDeserializer(KeyStateRecordKt.class, new KeyStateRecordKtDeserializer());
        module.addDeserializer(CompletedDelegationOperationResponse.class, new CompletedDelegationOperationResponseDeserializer());
        mapper.registerModule(module);
    }
}
