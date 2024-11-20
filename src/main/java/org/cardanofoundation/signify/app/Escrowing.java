package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;

public class Escrowing {
    @Getter
    public static class Escrows {
        public final SignifyClient client;

        /**
         * Escrows
         * @param client {SignifyClient}
         */
        public Escrows(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }
}
