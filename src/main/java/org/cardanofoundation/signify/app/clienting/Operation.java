package org.cardanofoundation.signify.app.clienting;

import lombok.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operation<T> extends CompletableFuture<Operation<Object>> {

    String name;
    Metadata metadata;
    boolean done;
    Object error;
    T response;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata<V> {
        Operation<V> depends;
        Map<String, Object> properties;
    }

    public static <R> Operation<R> fromObject(Object obj) {
        Operation<R> result = new Operation<>();
        
        if (obj instanceof Operation<?> operation) {
            result.name = operation.name;
            result.metadata = operation.metadata;
            result.done = operation.done;
            result.error = operation.error;
            result.response = (R) operation.response;
        } 
        else if (obj instanceof Map<?,?> map) {
            result.name = (String) map.get("name");
            result.metadata = convertMetadata(map.get("metadata"));
            result.done = (Boolean) map.get("done");
            result.error = map.get("error");
            result.response = (R) map.get("response");
        }
        else {
            throw new IllegalArgumentException("Object is neither an Operation instance nor a Map");
        }
        
        return result;
    }

    private static <V> Metadata<V> convertMetadata(Object metadataObj) {
        if (metadataObj == null) {
            return null;
        }
        if (metadataObj instanceof Metadata) {
            return (Metadata<V>) metadataObj;
        }
        if (metadataObj instanceof Map<?,?> map) {
            Map<String, Object> properties = map.containsKey("properties")
                ? (Map<String, Object>) map.get("properties")
                : (Map<String, Object>) map;

            return Metadata.<V>builder()
                .depends(map.get("depends") != null ? fromObject(map.get("depends")) : null)
                .properties(properties)
                .build();
        }
        throw new IllegalArgumentException("Metadata object is neither a Metadata instance nor a Map");
    }
}
