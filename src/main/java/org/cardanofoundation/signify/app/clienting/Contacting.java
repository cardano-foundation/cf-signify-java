package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

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
    }

    @Getter
    public static class Contacts {
        private final ObjectMapper objectMapper = new ObjectMapper();
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
        public Object list(
            String group,
            String filterField,
            String filterValue
        ) throws SodiumException, IOException, InterruptedException {
            StringBuilder path = new StringBuilder("/contacts");
            boolean hasQuery = false;

            if (group != null) {
                path.append("?group=").append(group);
                hasQuery = true;
            }
            if (filterField != null && filterValue != null) {
                path.append(hasQuery ? "&" : "?")
                    .append("filter_field=").append(filterField)
                    .append("&filter_value=").append(filterValue);
            }

            HttpResponse<String> response = client.fetch(path.toString(), "GET", null, null);
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        }

        /**
         * Get a contact
         * @param pre Prefix of the contact
         * @return The contact
         */
        public Object get(String pre) throws SodiumException, IOException, InterruptedException {
            String path = "/contacts/" + pre;
            HttpResponse<String> response = client.fetch(path, "GET", null, null);
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
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
            return objectMapper.readValue(response.body(), new TypeReference<>() {
            });
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
            return objectMapper.readValue(response.body(), new TypeReference<>() {
            });
        }
    }
}
