package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;

public class Grouping {
    @Getter
    public static class Groups {
        public final SignifyClient client;

        /**
         * Groups
         * @param client {SignifyClient}
         */
        public Groups(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }
}
