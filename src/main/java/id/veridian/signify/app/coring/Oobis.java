package org.cardanofoundation.signify.app.coring;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.EndRole;
import org.cardanofoundation.signify.generated.keria.model.OOBI;
import org.cardanofoundation.signify.generated.keria.model.OOBIOperation;

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
     */
    public Optional<OOBI> get(String name, String role) {
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
     */
    public OOBIOperation resolve(String oobi, String alias) {
        String path = "/oobis";
        String method = "POST";

        Map<String, Object> data = new HashMap<>();
        data.put("url", oobi);
        if (alias != null) {
            data.put("oobialias", alias);
        }
        HttpResponse<String> response = this.client.fetch(path, method, data);
        return Utils.fromJson(response.body(), OOBIOperation.class);
    }

    /**
     * Get end roles for an AID by prefix
     *
     * @param aid  AID prefix
     * @param role Optional role to filter by
     * @return List of end roles
     */
    public List<EndRole> endroles(String aid, String role) {
        String path = (role != null)
                ? "/endroles/" + aid + "/" + role
                : "/endroles/" + aid;
        HttpResponse<String> response = this.client.fetch(path, "GET", null);
        return Utils.fromJson(response.body(), new TypeReference<List<EndRole>>() {});
    }
}