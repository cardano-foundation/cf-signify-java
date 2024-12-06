package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.deps.IdentifierDeps;
import org.cardanofoundation.signify.core.Httping;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

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
         * @return A IdentifierListResponse containing the list response
         */
        public IdentifierListResponse list(Integer start, Integer end) throws IOException, InterruptedException, SodiumException {
            HttpHeaders extraHeaders = HttpHeaders.of(Map.of(
                "Range", List.of(String.format("aids=%d-%d", start, end))
            ), (name, value) -> true);

            HttpResponse<String> response = client.fetch(
                "/identifiers",
                "GET",
                null,
                extraHeaders
            );

            String contentRange = response.headers().firstValue("content-range").orElse("");
            Httping.RangeInfo range = parseRangeHeaders(contentRange, "aids");

            return new IdentifierListResponse(
                range.start(),
                range.end(),
                range.total(),
                response.body()
            );
        }

        public IdentifierListResponse list() throws IOException, InterruptedException, SodiumException {
            return list(0, 24);
        }

        public record IdentifierListResponse(int start, int end, int total, Object aids) {
        }

        //TODO implement the rest of the function
    }

}
