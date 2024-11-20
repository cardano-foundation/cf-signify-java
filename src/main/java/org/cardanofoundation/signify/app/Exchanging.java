package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;

public class Exchanging {
    @Getter
    public static class Exchanges {
        public final SignifyClient client;

        /**
         * Exchanges
         * @param client {SignifyClient}
         */
        public Exchanges(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }
}
