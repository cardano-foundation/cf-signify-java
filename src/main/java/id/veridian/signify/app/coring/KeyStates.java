package org.cardanofoundation.signify.app.coring;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.QueryOperation;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.*;

public class KeyStates {
    public final SignifyClient client;

    /**
     * KeyStates
     *
     * @param client {SignifyClient}
     */
    public KeyStates(SignifyClient client) {
        this.client = client;
    }


    /**
     * Retrieve the key state for an identifier
     *
     * @param pre Identifier prefix
     * @return Optional containing a map representing the key states, or empty if not found
     */
    public Optional<KeyStateRecord> get(String pre) {
        String path = "/states?pre=" + pre;
        String method = "GET";
        HttpResponse<String> res = this.client.fetch(path, method, null);
        
        if (res.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }

        // Note: KERIA always returns an array (at least empty array, or array with length 1 for single identifier)
        KeyStateRecord[] records = Utils.fromJson(res.body(), KeyStateRecord[].class);
        if (records.length == 0) {
            return Optional.empty();
        }
        return Optional.of(records[0]);
    }

    /**
     * Retrieve the key state for a list of identifiers
     */
    public List<KeyStateRecord> list(List<String> pres) {
        String path = "/states?" + String.join("&", pres.stream().map(pre -> "pre=" + pre).toArray(String[]::new));
        String method = "GET";
        HttpResponse<String> res = this.client.fetch(path, method, null);

        return Arrays.asList(Utils.fromJson(res.body(), KeyStateRecord[].class));
    }

    /**
     * Query the key state of an identifier for a given sequence number or anchor
     *
     * @param pre    Identifier prefix
     * @param sn     Optional sequence number
     * @param anchor Optional anchor
     * @return A map representing the long-running operation
     */
    public QueryOperation query(String pre, String sn, Object anchor) {
        String path = "/queries";
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pre", pre);
        if (sn != null) {
            data.put("sn", sn);
        }
        if (anchor != null) {
            data.put("anchor", anchor);
        }
        String method = "POST";
        HttpResponse<String> res = this.client.fetch(path, method, data);
        return Utils.fromJson(res.body(), QueryOperation.class);
    }

    public QueryOperation query(String pre, String sn) {
        return query(pre, sn, null);
    }
}
