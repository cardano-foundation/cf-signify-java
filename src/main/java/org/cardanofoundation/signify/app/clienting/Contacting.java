package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
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
    public static class Contact {
        private String alias;
        private String oobi;
        private String id;
        private Map<String, Object> additionalProperties = new HashMap<>();
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
        public Contact[] list(
            String group,
            String filterField,
            String filterValue
        ) throws SodiumException, JsonProcessingException {
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

            ResponseEntity<String> response = client.fetch(path.toString(), "GET", null, null);
            return objectMapper.readValue(response.getBody(), Contact[].class);
        }

        /**
         * Get a contact
         * @param pre Prefix of the contact
         * @return The contact
         */
        public Contact get(String pre) throws SodiumException, JsonProcessingException {
            String path = "/contacts/" + pre;
            ResponseEntity<String> response = client.fetch(path, "GET", null, null);
            return objectMapper.readValue(response.getBody(), Contact.class);
        }

        /**
         * Add a contact
         * @param pre Prefix of the contact
         * @param info Information about the contact
         * @return Result of the addition
         */
        public Contact add(String pre, Map<String, Object> info) throws SodiumException, JsonProcessingException {
            String path = "/contacts/" + pre;
            ResponseEntity<String> response = client.fetch(path, "POST", info, null);
            return objectMapper.readValue(response.getBody(), Contact.class);
        }

        /**
         * Delete a contact
         * @param pre Prefix of the contact
         */
        public void delete(String pre) throws SodiumException {
            String path = "/contacts/" + pre;
            client.fetch(path, "DELETE", null, null);
        }

        /**
         * Update a contact
         * @param pre Prefix of the contact
         * @param info Updated information about the contact
         * @return Result of the update
         */
        public Contact update(String pre, Map<String, Object> info) throws SodiumException, JsonProcessingException {
            String path = "/contacts/" + pre;
            ResponseEntity<String> response = client.fetch(path, "PUT", info, null);
            return objectMapper.readValue(response.getBody(), Contact.class);
        }
    }
}
