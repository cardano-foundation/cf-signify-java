package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.aiding.InteractionResponse;

import java.net.http.HttpResponse;

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

        /**
         * Approve the delegation via interaction event
         * @param name Name or alias of the identifier
         * @param data The anchoring interaction event
         * @return The delegated approval result
         * @throws Exception if the fetch operation fails
         */
        public EventResult approve(String name, Object data) throws Exception {
            InteractionResponse interactionResponse = this.client.getIdentifier().createInteract(name, data);

            HttpResponse<String> res = this.client.fetch(
                "/identifiers/" + name + "/delegation",
                "POST",
                interactionResponse.jsondata(),
                null
            );
            return new EventResult(interactionResponse.serder(), interactionResponse.sigs(), res);
        }

        public EventResult approve(String name) throws Exception {
            return this.approve(name, null);
        }
    }
}
