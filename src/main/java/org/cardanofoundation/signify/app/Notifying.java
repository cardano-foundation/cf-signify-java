package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;

public class Notifying {
    @Getter
    public static class Notifications {
        public final SignifyClient client;

        /**
         * Notifications
         * @param client {SignifyClient}
         */
        public Notifications(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }
}
