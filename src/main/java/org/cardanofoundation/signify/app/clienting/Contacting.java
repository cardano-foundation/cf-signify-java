package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.Agent;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.util.Collections;
import java.util.HashMap;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Contacting {
    @Getter
    public static class Challenges {
        public final SignifyClient client;

        /**
         * Challenges
         * @param client {SignifyClient}
         */
        public Challenges(SignifyClient client) {
            this.client = client;
        }

        // others functions

        /**
         * Generate a random challenge word list based on BIP39
         * @async
         * @param {number} strength Integer representing the strength of the challenge. Typically 128 or 256
         * @returns list of random words
         */
        public Object generate(String strength) throws SodiumException, IOException, InterruptedException {
            String path = "/challenges?strength=" + strength;
            String method = "GET";
            HttpResponse<String> response = client.fetch(path, method, null, null);
            return Utils.fromJson(response.body(), Object.class);
        }

        public Object respond(String name, String recipient, Object words) throws SodiumException, IOException, InterruptedException, DigestException, ExecutionException {
            States.HabState hab = client.getIdentifier().get(name);
            Exchanging.Exchanges exchanges = client.getExchanges();
            Challenge challenge = new Challenge();
            challenge.setWords((Map<String, Object>) words);

            Object resp = exchanges.send(name,
                    "challenge",
                    hab,
                    "/challenge/response",
                    challenge.getWords(),
                    null,
                    Collections.singletonList(recipient)
            );
            return resp;
        }

        public Object verify(String source, Object words) throws SodiumException, IOException, InterruptedException, ExecutionException {
            String path = "/challenges_verify/" + source;
            String method = "POST";
            Challenge challenge = new Challenge();
            challenge.setWords((Map<String, Object>) words);
            Map<String, Object> data = challenge.getWords();
            HttpResponse<String> response = client.fetch(path, method, data, null);
            return Utils.fromJson(response.body(), Object.class);
        }

        public Object responded(String source, String said) throws SodiumException, IOException, InterruptedException {
            String path = "/challenges_verify/" + source;
            String method = "PUT";
            String data = said;
            HttpResponse<String> response = client.fetch(path, method, data, null);
            return Utils.fromJson(response.body(), Object.class);
        }

    }

    @Getter
    @Setter
    public static class Challenge {
        private Map<String, Object> words = new HashMap<>();
    }

    @Getter
    public static class Contact {
        private String alias;
        private String oobi;
        private String id;
        private Map<String, Object> additionalProperties = new HashMap<>();
    }

    @Getter
    public static class Contacts {
        private final SignifyClient client;

        /**
         * Contacts
         * @param client {SignifyClient}
         */
        public Contacts(SignifyClient client) {
            this.client = client;
        }

        /**
         * List contacts
         * @param group Optional group name to filter contacts
         * @param filterField Optional field name to filter contacts
         * @param filterValue Optional field value to filter contacts
         * @return List of contacts
         */
        public Contact[] list(
                String group,
                String filterField,
                String filterValue
        ) throws SodiumException, InterruptedException, IOException {
            StringBuilder path = new StringBuilder("/contacts");
            boolean hasQuery = false;

            if (group != null) {
                path.append("?group=").append(group);
                hasQuery = true;
            }
            if (filterField != null && filterValue != null) {
                path.append(hasQuery ? "&" : "?")
                        .append("filter_field=").append(filterField)
                        .append("&filter_value=").append(URLEncoder.encode(filterValue, StandardCharsets.UTF_8));
            }

            HttpResponse<String> response = client.fetch(path.toString(), "GET", null, null);
            return Utils.fromJson(response.body(), Contact[].class);
        }

        /**
         * Get a contact
         * @param pre Prefix of the contact
         * @return The contact
         */
        public Object get(String pre) throws SodiumException, InterruptedException, IOException {
            String path = "/contacts/" + pre;
            HttpResponse<String> response = client.fetch(path, "GET", null, null);
            return Utils.fromJson(response.body(), Object.class);
        }

        /**
         * Add a contact
         * @param pre Prefix of the contact
         * @param info Information about the contact
         * @return Result of the addition
         */
        public Object add(String pre, Map<String, Object> info) throws SodiumException, IOException, InterruptedException {
            String path = "/contacts/" + pre;
            HttpResponse<String> response = client.fetch(path, "POST", info, null);
            return Utils.fromJson(response.body(), Object.class);
        }

        /**
         * Delete a contact
         * @param pre Prefix of the contact
         */
        public void delete(String pre) throws SodiumException, IOException, InterruptedException {
            String path = "/contacts/" + pre;
            client.fetch(path, "DELETE", null, null);
        }

        /**
         * Update a contact
         * @param pre Prefix of the contact
         * @param info Updated information about the contact
         * @return Result of the update
         */
        public Object update(String pre, Object info) throws SodiumException, IOException, InterruptedException {
            String path = "/contacts/" + pre;
            HttpResponse<String> response = client.fetch(path, "PUT", info, null);
            return Utils.fromJson(response.body(), Object.class);
        }
    }
}