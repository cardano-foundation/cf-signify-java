package org.cardanofoundation.signify.cesr.deps;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public interface BaseDeps {
    ResponseEntity<String> fetch(
        String pathname,
        String method,
        Object body,
        HttpHeaders headers
    ) throws SodiumException;
}
