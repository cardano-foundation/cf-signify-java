package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jackson module that preserves JSON insertion order for generated models with
 * {@code additionalProperties: true}.
 *
 * <p>The generator emits {@code @JsonAnySetter}/{@code @JsonAnyGetter} for extra fields, but
 * backs them with a {@code HashMap}, losing order. It also serialises typed fields first
 * (per {@code @JsonPropertyOrder}), so the round-trip no longer reflects the original order.
 *
 * <p>This module intercepts any bean that has an {@code additionalProperties} field written by
 * {@code @JsonAnySetter}, and:
 * <ul>
 *   <li><b>Deserialization</b>: stores <em>all</em> JSON fields (typed + extra) in a
 *       {@code LinkedHashMap} as {@code additionalProperties}, preserving insertion order.
 *       Typed setters are still called so typed getters continue to work.</li>
 *   <li><b>Serialization</b>: if {@code additionalProperties} is the full ordered map, writes
 *       from it directly, bypassing {@code @JsonPropertyOrder}, so the output order matches
 *       the original JSON and SAID computation is correct.</li>
 * </ul>
 */
public class InsertionOrderAdditionalPropertiesModule extends SimpleModule {

    public InsertionOrderAdditionalPropertiesModule() {
        super("InsertionOrderAdditionalPropertiesModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addBeanDeserializerModifier(new DeserializerModifier());
        context.addBeanSerializerModifier(new SerializerModifier());
    }

    /**
     * Returns the additionalProperties field if the class has @JsonAnySetter,
     * null otherwise.
     */
    private static Field findApField(Class<?> clazz) {
        try {
            Field f = clazz.getDeclaredField("additionalProperties");
            if (!Map.class.isAssignableFrom(f.getType())) return null;
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Deserialization
    // -------------------------------------------------------------------------

    private static class DeserializerModifier extends BeanDeserializerModifier {
        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                      BeanDescription beanDesc,
                                                      JsonDeserializer<?> deserializer) {
            Field apField = findApField(beanDesc.getBeanClass());
            if (apField == null) return deserializer;
            return new OrderPreservingDeserializer<>(deserializer, apField);
        }
    }

    @SuppressWarnings("unchecked")
    private static class OrderPreservingDeserializer<T> extends StdDeserializer<T>
            implements ResolvableDeserializer {

        private final JsonDeserializer<T> delegate;
        private final Field apField;

        OrderPreservingDeserializer(JsonDeserializer<?> delegate, Field apField) {
            super(delegate.handledType());
            this.delegate = (JsonDeserializer<T>) delegate;
            this.apField = apField;
        }

        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            if (delegate instanceof ResolvableDeserializer rd) rd.resolve(ctxt);
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            ObjectNode tree = (ObjectNode) ctxt.readTree(p);

            // Capture all fields in wire order
            LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
            tree.fields().forEachRemaining(e ->
                    ordered.put(e.getKey(), mapper.convertValue(e.getValue(), Object.class)));

            // Delegate to standard bean deserializer for typed field access
            JsonParser treeParser = tree.traverse(mapper);
            treeParser.nextToken();
            T bean = delegate.deserialize(treeParser, ctxt);

            // Replace with ordered map containing all fields
            try {
                apField.set(bean, ordered);
            } catch (IllegalAccessException e) {
                throw new IOException("Cannot set additionalProperties on "
                        + bean.getClass().getSimpleName(), e);
            }

            return bean;
        }
    }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    private static class SerializerModifier extends BeanSerializerModifier {
        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                  BeanDescription beanDesc,
                                                  JsonSerializer<?> serializer) {
            Field apField = findApField(beanDesc.getBeanClass());
            if (apField == null) return serializer;
            return new OrderPreservingSerializer<>(serializer, apField);
        }
    }

    @SuppressWarnings("unchecked")
    private static class OrderPreservingSerializer<T> extends JsonSerializer<T> {

        private final JsonSerializer<T> delegate;
        private final Field apField;

        OrderPreservingSerializer(JsonSerializer<?> delegate, Field apField) {
            this.delegate = (JsonSerializer<T>) delegate;
            this.apField = apField;
        }

        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            Map<String, Object> ordered;
            try {
                ordered = (Map<String, Object>) apField.get(value);
            } catch (IllegalAccessException e) {
                delegate.serialize(value, gen, provider);
                return;
            }

            if (ordered == null || ordered.isEmpty()) {
                delegate.serialize(value, gen, provider);
                return;
            }

            gen.writeStartObject();
            for (Map.Entry<String, Object> entry : ordered.entrySet()) {
                gen.writeFieldName(entry.getKey());
                provider.defaultSerializeValue(entry.getValue(), gen);
            }
            gen.writeEndObject();
        }
    }
}
