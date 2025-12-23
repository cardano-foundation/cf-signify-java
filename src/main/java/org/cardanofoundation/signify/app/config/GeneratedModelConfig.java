package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.cardanofoundation.signify.app.aiding.KeyStateRecordDeserializer;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.openapitools.jackson.nullable.JsonNullableModule;

/**
 * Centralizes Jackson configuration for OpenAPI-generated models.
 */
public final class GeneratedModelConfig {
    private GeneratedModelConfig() {
    }

    public static ObjectMapper mapper() {
        ObjectMapper mapper = baseMapper();
        mapper.registerModule(generatedModule());
        return mapper;
    }

    /**
     * Apply the generated-model settings to an existing mapper.
     */
    public static void configure(ObjectMapper mapper) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
        module.addDeserializer(KeyStateRecord.class, new KeyStateRecordDeserializer());
        return module;
    }
}
