package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;

public class Credentialing {
    @Getter
    public static class Ipex {
        public final SignifyClient client;

        /**
         * Schemas
         * @param client {SignifyClient}
         */
        public Ipex(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }


    @Getter
    public static class Schemas {
        public final SignifyClient client;

        /**
         * Schemas
         * @param client {SignifyClient}
         */
        public Schemas(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }
}
