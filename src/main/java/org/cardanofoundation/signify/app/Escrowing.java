package org.cardanofoundation.signify.app;

import lombok.Getter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

        /**
         * List replay messages
         * @param route Optional route in the replay message
         * @return THe list of replay messages
         * @throws Exception if the fetch operation fails
         */
        public Object listReply(String route) throws Exception {
            StringBuilder path = new StringBuilder("/escrows/rpy");
        
            if (route != null && !route.isEmpty()) {
                String encodedRoute = URLEncoder.encode(route, StandardCharsets.UTF_8);
                path.append("?route=").append(encodedRoute);
            }

            return client.fetch(path.toString(), "GET", null, null);
        }
    
        public Object listReply() throws Exception {
            return listReply(null);
        }
    }
}
