package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.aiding.InteractionResponse;

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

        public EventResult approve(String name, Object data) throws SodiumException, DigestException, IOException, InterruptedException {
            InteractionResponse interactionResponse = client.getIdentifier().createInteract(name, data);
            HttpResponse<String> response = client.fetch(
                    "/identifiers/" + name + "/delegation",
                    "POST",
                    interactionResponse.jsondata(),
                    null
            );
            return new EventResult(interactionResponse.serder(), interactionResponse.sigs(), response);

        }
    }
}
