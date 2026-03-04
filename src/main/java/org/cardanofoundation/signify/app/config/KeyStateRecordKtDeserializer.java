package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for KeyStateRecordKt that handles polymorphic JSON.
 * KeyStateRecordKt can be either a String or a List of Strings in the JSON.
 */
public class KeyStateRecordKtDeserializer extends JsonDeserializer<KeyStateRecordKt> {
    
    @Override
    public KeyStateRecordKt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        if (node.isTextual()) {
            // Return a wrapper that holds the string value
            return new KeyStateRecordKtString(node.asText());
        } else if (node.isArray()) {
            // Return a wrapper that holds the list value
            List<String> values = new ArrayList<>();
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    values.add(item.asText());
                }
            }
            return new KeyStateRecordKtList(values);
        } else {
            throw new IOException("KeyStateRecordKt must be either a String or an array of Strings");
        }
    }
    
    /**
     * Wrapper for string values
     */
    public static class KeyStateRecordKtString extends KeyStateRecordKt {
        private final String value;
        
        public KeyStateRecordKtString(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof KeyStateRecordKtString)) return false;
            KeyStateRecordKtString that = (KeyStateRecordKtString) o;
            return value.equals(that.value);
        }
        
        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
    
    /**
     * Wrapper for list values
     */
    public static class KeyStateRecordKtList extends KeyStateRecordKt {
        private final List<String> values;
        
        public KeyStateRecordKtList(List<String> values) {
            this.values = values;
        }
        
        public List<String> getValues() {
            return values;
        }
        
        @Override
        public String toString() {
            return values.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof KeyStateRecordKtList)) return false;
            KeyStateRecordKtList that = (KeyStateRecordKtList) o;
            return values.equals(that.values);
        }
        
        @Override
        public int hashCode() {
            return values.hashCode();
        }
    }
}
