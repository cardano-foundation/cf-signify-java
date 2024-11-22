package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.deps.IdentifierDeps;
import org.cardanofoundation.signify.core.Httping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import static org.cardanofoundation.signify.core.Httping.parseRangeHeaders;

public class Aiding {
    /**
     * Identifier
     */
    @Getter
    public static class Identifier {
        public final IdentifierDeps client;

        /**
         * Identifier
         * @param client the client dependencies
         */
        public Identifier(IdentifierDeps client) {
            this.client = client;
        }

        /**
         * List managed identifiers
         *
         * @param start Start index of list of notifications, defaults to 0
         * @param end   End index of list of notifications, defaults to 24
         * @return A Mono containing the list response
         */
        public Mono<IdentifierListResponse> list(Integer start, Integer end) throws SodiumException {
            HttpHeaders extraHeaders = new HttpHeaders();
            extraHeaders.add("Range", String.format("aids=%d-%d", start, end));

            ResponseEntity<String> response = client.fetch(
                "/identifiers",
                "GET",
                null,
                extraHeaders
            );

            String contentRange = response.getHeaders().getFirst("content-range");
            Httping.RangeInfo range = parseRangeHeaders(contentRange, "aids");

            return Mono.just(new IdentifierListResponse(
                    range.start(),
                    range.end(),
                    range.total(),
                    response.getBody()
            ));
        }

        public Mono<IdentifierListResponse> list() throws SodiumException {
            return list(0, 24);
        }

        public record IdentifierListResponse(int start, int end, int total, Object aids) {
        }

        //TODO implement the rest of the function
    }

}
