package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.InteractionResponse;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.DigestException;

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
        public EventResult approve(String name, Object data) throws LibsodiumException, DigestException, IOException, InterruptedException {
            InteractionResponse interactionResponse = null;
            HttpResponse<String> res = null;
            try {
                interactionResponse = this.client.identifiers().createInteract(name, data);
                res = this.client.fetch(
                    "/identifiers/" + name + "/delegation",
                    "POST",
                    interactionResponse.jsondata()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new EventResult(interactionResponse.serder(), interactionResponse.sigs(), res);
        }

        public EventResult approve(String name) throws LibsodiumException, DigestException, IOException, InterruptedException {
            return this.approve(name, null);
        }
    }
}
