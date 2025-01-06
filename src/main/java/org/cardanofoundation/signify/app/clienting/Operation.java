package org.cardanofoundation.signify.app.clienting;

import lombok.*;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operation<T> {

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
        OperationBuilder<R> resultBuilder = Operation.<R>builder();

        if (obj instanceof Operation<?> operation) {
           return (Operation<R>) operation;
        } 
        else if (obj instanceof Map<?,?> map) {
            resultBuilder
                .name(map.containsKey("name") ? (String) map.get("name") : null)
                .metadata(convertMetadata(map.get("metadata")))
                .done(map.containsKey("done") && (boolean) map.get("done"))
                .error(map.getOrDefault("error", null))
                .response(map.containsKey("response") ? (R) map.get("response") : null);
        } else if(obj instanceof String) { // json string
            Map<String, Object> mapObj = Utils.fromJson((String) obj, Map.class);
            return fromObject(mapObj);
        }
        else {
            throw new IllegalArgumentException("Object is neither an Operation instance nor a Map");
        }
        
        return resultBuilder.build();
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
