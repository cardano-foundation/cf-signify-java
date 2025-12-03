package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.util.HashMap;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.concurrent.ExecutionException;
import org.cardanofoundation.signify.generated.keria.model.Identifier;

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
         * @param strength Integer representing the strength of the challenge. Typically, 128 or 256
         * @return A list of random words
         * @throws Exception if the fetch operation fails
         */
        public Challenge generate(Integer strength) throws LibsodiumException, IOException, InterruptedException {
            String path = "/challenges?strength=" + strength.toString();
            String method = "GET";

            HttpResponse<String> response = this.client.fetch(path, method, null);
            return Utils.fromJson(response.body(), Challenge.class);
        }

        public Challenge generate() throws LibsodiumException, IOException, InterruptedException {
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
        public Object respond(String name, String recipient, List<String> words) throws IOException, InterruptedException, DigestException, ExecutionException, LibsodiumException {
            Identifier hab = this.client.identifiers().get(name)
                    .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + name));
            Exchanging.Exchanges exchanges = this.client.exchanges();

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
         * @throws Exception if the fetch operation fails
         */
        public Object verify(String source, List<String> words) throws LibsodiumException, IOException, InterruptedException {
            String path = "/challenges_verify/" + source;
            String method = "POST";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("words", words);

            HttpResponse<String> response = this.client.fetch(path, method, data);
            return Utils.fromJson(response.body(), Object.class);
        }

        /**
         * Mark challenge response as signed and accepted
         * @param source Prefix of the identifier that was challenged
         * @param said qb64 AID of exn message representing the signed response
         * @return The result
         * @throws Exception if the fetch operation fails
         */
        public Object responded(String source, String said) throws LibsodiumException, IOException, InterruptedException {
            String path = "/challenges_verify/" + source;
            String method = "PUT";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("said", said);

            return this.client.fetch(path, method, data);
        }
    }

    @Getter
    public static class Contact {
        private String alias;
        private String oobi;
        private String id;
        private Map<String, Object> additionalProperties = new HashMap<>();

        @JsonAnySetter
        public void setAdditionalProperty(String key, Object value) {
            additionalProperties.put(key, value);
        }

        public <T> T get(String key) {
            return switch (key) {
                case "alias" -> (T) alias;
                case "oobi" -> (T) oobi;
                case "id" -> (T) id;
                default -> (T) additionalProperties.get(key);
            };
        }
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
         * @return An array list of contacts
         */
        public Contact[] list(
            String group,
            String filterField,
            String filterValue
        ) throws InterruptedException, IOException, LibsodiumException {
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
            String method = "GET";
            HttpResponse<String> response = this.client.fetch(path.toString(), method, null);
            return Utils.fromJson(response.body(), Contact[].class);
        }

        public Contact[] list() throws IOException, InterruptedException, LibsodiumException {
            return list(null, null, null);
        }

        /**
         * Get a contact
         * @param pre Prefix of the contact
         * @return Optional containing the contact if found, or empty if not found
         */
        public Optional<Object> get(String pre) throws InterruptedException, IOException, LibsodiumException {
            String path = "/contacts/" + pre;
            String method = "GET";
            HttpResponse<String> response = this.client.fetch(path, method, null);
            
            if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Optional.empty();
            }
            
            return Optional.of(Utils.fromJson(response.body(), Object.class));
        }

        /**
         * Add a contact
         * @param pre Prefix of the contact
         * @param info Information about the contact
         * @return Result of the addition
         */
        public Object add(String pre, Map<String, Object> info) throws IOException, InterruptedException, LibsodiumException {
            String path = "/contacts/" + pre;
            String method = "POST";
            HttpResponse<String> response = this.client.fetch(path, method, info);
            return Utils.fromJson(response.body(), Object.class);
        }

        /**
         * Delete a contact
         * @param pre Prefix of the contact
         */
        public void delete(String pre) throws IOException, InterruptedException, LibsodiumException {
            String path = "/contacts/" + pre;
            String method = "DELETE";
            this.client.fetch(path, method, null);
        }

        /**
         * Update a contact
         * @param pre Prefix of the contact
         * @param info Updated information about the contact
         * @return Result of the update
         */
        public Object update(String pre, Object info) throws IOException, InterruptedException, LibsodiumException {
            String path = "/contacts/" + pre;
            String method = "PUT";
            HttpResponse<String> response = this.client.fetch(path, method, info);
            return Utils.fromJson(response.body(), Object.class);
        }
    }
}