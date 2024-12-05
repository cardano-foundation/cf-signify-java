package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;

public class Delegating {
    @Getter
    public static class Delegations {
        public final SignifyClient client;

        /**
         * Delegations
         * @param client {SignifyClient}
         */
        public Delegations(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }
}
