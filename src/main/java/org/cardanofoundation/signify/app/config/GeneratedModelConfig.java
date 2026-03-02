package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.cardanofoundation.signify.app.aiding.KeyStateRecordKtDeserializer;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKtString;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKtListString;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf1;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf2;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf3;
import org.openapitools.jackson.nullable.JsonNullableModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralizes Jackson configuration for OpenAPI-generated models.
 */
public final class GeneratedModelConfig {
    private GeneratedModelConfig() {
    }

    public static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        configure(mapper);
        return mapper;
    }

    /**
     * Apply the generated-model settings to an existing mapper.
     */
    public static void configure(ObjectMapper mapper) {
        // Disable strict validation that would block sealed interface deserialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true);
        // Disable default type information processing to prevent validation of empty property names
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Register custom modules BEFORE JsonNullableModule to ensure they take precedence
        mapper.registerModule(generatedModule());
        mapper.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanDeserializerModifier(new SealedInterfaceModifier());
            }
        });
        mapper.registerModule(new JsonNullableModule());
    }

    private static Module generatedModule() {
        SimpleModule module = new SimpleModule("GeneratedModelModule");
        // Register deserializers for sealed interfaces and complex types
        module.addDeserializer(KeyStateRecordKt.class, new KeyStateRecordKtDeserializer());
        // Also register concrete implementations to ensure they can be deserialized
        module.addDeserializer(KeyStateRecordKtString.class, new StdDeserializer<KeyStateRecordKtString>(KeyStateRecordKtString.class) {
            @Override
            public KeyStateRecordKtString deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
                JsonNode node = p.getCodec().readTree(p);
                return new KeyStateRecordKtString(node.asText());
            }
        });
        module.addDeserializer(KeyStateRecordKtListString.class, new StdDeserializer<KeyStateRecordKtListString>(KeyStateRecordKtListString.class) {
            @Override
            public KeyStateRecordKtListString deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
                JsonNode node = p.getCodec().readTree(p);
                List<String> values = new ArrayList<>();
                node.forEach(item -> values.add(item.asText()));
                return new KeyStateRecordKtListString(values);
            }
        });
        
        return module;
    }
}