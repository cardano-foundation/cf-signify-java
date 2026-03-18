package org.cardanofoundation.signify.app.coring;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.OOBI;

public class Oobis {
    private final SignifyClient client;

    public Oobis(SignifyClient client) {
        this.client = client;
    }

    /**
     * Get the OOBI(s) for a managed identifier for a given role
     *
     * @param name Name or alias of the identifier
     * @param role Authorized role
     * @return Optional containing the OOBI(s) if found, or empty if not found
     * @throws JsonProcessingException if there is an error processing the JSON
     * @throws LibsodiumException if there is an error in the cryptographic operations
     */
    public Optional<OOBI> get(String name, String role) throws IOException, InterruptedException, LibsodiumException {
        if (role == null) {
            role = "agent";
        }
        String path = "/identifiers/" + name + "/oobis?role=" + role;
        String method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null);
        
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }
        
        return Optional.of(Utils.fromJson(response.body(), OOBI.class));
    }

    /**
     * Resolve an OOBI
     *
     * @param oobi  The OOBI to be resolved
     * @param alias Optional name or alias to link the OOBI resolution to a contact
     * @return A promise to the long-running operation
     * @throws JsonProcessingException if there is an error processing the JSON
     * @throws LibsodiumException if there is an error in the cryptographic operations
     */
    public Object resolve(String oobi, String alias) throws IOException, InterruptedException, LibsodiumException {
        String path = "/oobis";
        String method = "POST";

        Map<String, Object> data = new HashMap<>();
        data.put("url", oobi);
        if (alias != null) {
            data.put("oobialias", alias);
        }
        HttpResponse<String> response = this.client.fetch(path, method, data);
        return Utils.fromJson(response.body(), Object.class);
    }
}