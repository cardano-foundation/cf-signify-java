package org.cardanofoundation.signify.cesr.deps;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface BaseDeps {
    Mono<ResponseEntity<String>> fetch(
        String pathname,
        String method,
        Object body,
        HttpHeaders headers
    );
}
