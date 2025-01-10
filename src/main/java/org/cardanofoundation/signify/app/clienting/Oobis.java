package org.cardanofoundation.signify.app.clienting;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;

public class Oobis {
    private final SignifyClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Oobis(SignifyClient client) {
        this.client = client;
    }

    /**
     * Get the OOBI(s) for a managed identifier for a given role
     *
     * @param name Name or alias of the identifier
     * @param role Authorized role
     * @return A promise to the OOBI(s)
     * @throws JsonProcessingException if there is an error processing the JSON
     * @throws SodiumException
     */
    public Object get(String name, String role) throws IOException, SodiumException, InterruptedException {
        if (role == null) {
            role = "agent";
        }
        String path = "/identifiers/" + name + "/oobis?role=" + role;
        String method = "GET";
        HttpResponse<String> response = client.fetch(path, method, null, null);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    /**
     * Resolve an OOBI
     *
     * @param oobi  The OOBI to be resolved
     * @param alias Optional name or alias to link the OOBI resolution to a contact
     * @return A promise to the long-running operation
     * @throws JsonProcessingException if there is an error processing the JSON
     * @throws SodiumException
     */
    public Object resolve(String oobi, String alias) throws IOException, SodiumException, InterruptedException {
        String path = "/oobis";
        String method = "POST";

        Map<String, Object> data = new HashMap<>();
        data.put("url", oobi);
        if (alias != null) {
            data.put("oobialias", alias);
        }
        HttpResponse<String> response = client.fetch(path, method, data, null);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }
}