package org.cardanofoundation.signify.app.clienting;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Contacting {

    @Getter
    public static class Challenge {
        public List<String> words;
    }

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

        /**
         * Retrieve the key state for an identifier
         * @param strength Integer representing the strength of the challenge. Typically 128 or 256
         * @return A list of random words
         * @throws Exception if the fetch operation fails
         */
        public Challenge generate(Integer strength) throws Exception {
            String path = "/challenges?strength=" + strength.toString();
            String method = "GET";
            return Utils.fromJson(client.fetch(path, method, null, null).body(), Challenge.class);
        }

        public Challenge generate() throws Exception {
            return generate(128);
        }

        /**
         * Respond to a challenge by signing a message with the list of words
         * @param name Name or alias of the identifier
         * @param recipient Prefix of the recipient of the response
         * @param words List of words to embed in the signed response
         * @return The result of the response
         * @throws Exception if the fetch operation fails
         */
        public Object respond(String name, String recipient, List<String> words) throws Exception {
            States.HabState hab = this.client.getIdentifier().get(name);
            Exchanging.Exchanges exchanges = this.client.getExchanges();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("words", words);

            Map<String, List<Object>> embeds = new LinkedHashMap<>();

            return exchanges.send(
                name,
                "challenge",
                hab,
                "/challenge/response",
                payload,
                embeds,
                List.of(recipient)
            );
        }

        /**
         * Ask Agent to verify a given sender signed the provided words
         * @param source Prefix of the identifier that was challenged
         * @param words List of challenge words to check for
         * @return The long-running operation
         * * @throws Exception if the fetch operation fails
         */
        public Object verify(String source, List<String> words) throws Exception {
            String path = "/challenges_verify/" + source;
            String method = "POST";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("words", words);
            return Utils.fromJson(client.fetch(path, method, data, null).body(), Object.class);
        }

        /**
         * Mark challenge response as signed and accepted
         * @param source Prefix of the identifier that was challenged
         * @param said qb64 AID of exn message representing the signed response
         * @return The result
         * @throws Exception if the fetch operation fails
         */
        public Object responded(String source, String said) throws Exception {
            String path = "/challenges_verify/" + source;
            String method = "PUT";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("said", said);
            return Utils.fromJson(client.fetch(path, method, data, null).body(), Object.class);
        }
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