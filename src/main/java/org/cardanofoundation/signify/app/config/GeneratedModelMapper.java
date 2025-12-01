package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.generated.keria.model.Identifier;

/**
 * Shared accessor for the generated-model ObjectMapper.
 */
public final class GeneratedModelMapper {
    private static final ObjectMapper MAPPER = GeneratedModelConfig.mapper();

    private GeneratedModelMapper() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static <T> T read(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse generated model JSON", e);
        }
    }

    public static <T> T read(String json, TypeReference<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse generated model JSON", e);
        }
    }

    public static Identifier readIdentifier(String json) {
        return read(json, Identifier.class);
    }

    public static Identifier[] readIdentifierArray(String json) {
        return read(json, Identifier[].class);
    }

    public static java.util.List<Identifier> readIdentifierList(String json) {
        return read(json, new TypeReference<>() {});
    }
}
