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
    Object getError;
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
}
