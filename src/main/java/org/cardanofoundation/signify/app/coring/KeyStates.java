package org.cardanofoundation.signify.app.coring;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;

import java.io.IOException;
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
     * @throws Exception if the fetch operation fails
     */
    public Optional<KeyStateRecord> get(String pre) throws LibsodiumException, IOException, InterruptedException {
        String path = "/states?pre=" + pre;
        String method = "GET";
        HttpResponse<String> res = this.client.fetch(path, method, null);
        
        if (res.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }

        String body = res.body();
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }

        // Note: KERIA can return either a single object or an array
        // Check if response is an array or single object
        String trimmed = body.trim();
        if (trimmed.startsWith("[")) {
            KeyStateRecord[] records = Utils.fromJson(body, KeyStateRecord[].class);
            if (records == null || records.length == 0) {
                return Optional.empty();
            }
            return Optional.of(records[0]);
        } else {
            return Optional.of(Utils.fromJson(body, KeyStateRecord.class));
        }
    }

    /**
     * Retrieve the key state for a list of identifiers
     */
    public List<KeyStateRecord> list(List<String> pres) throws LibsodiumException, IOException, InterruptedException {
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
     * @throws Exception if the fetch operation fails
     */
    public Object query(String pre, String sn, Object anchor) throws LibsodiumException, IOException, InterruptedException {
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
        return Utils.fromJson(res.body(), Object.class);
    }

    public Object query(String pre, String sn) throws Exception {
        return query(pre, sn, null);
    }
}
