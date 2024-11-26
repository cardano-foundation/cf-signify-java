package org.cardanofoundation.signify.core;

import lombok.*;

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
    Object getError;
    T response;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Metadata<V> {
        Operation<V> depends;
        Map<String, Object> properties;
    }
}
