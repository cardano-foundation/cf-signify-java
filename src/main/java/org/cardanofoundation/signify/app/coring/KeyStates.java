package org.cardanofoundation.signify.app.coring;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * @return A map representing the key states
     * @throws Exception if the fetch operation fails
     */
    public Object get(String pre) throws Exception {
        String path = "/states?pre=" + pre;
        String method = "GET";
        String data = null;
        return Utils.fromJson(client.fetch(path, method, data, null).body(), Object.class);
    }

    /**
     * Retrieve the key state for a list of identifiers
     *
     * @param pres List of identifier prefixes
     * @return A map representing the key states
     * @throws Exception if the fetch operation fails
     */
    public Object list(List<String> pres) throws Exception {
        String path = "/states?" + String.join("&", pres.stream().map(pre -> "pre=" + pre).toArray(String[]::new));
        String method = "GET";
        String data = null;
        return Utils.fromJson(client.fetch(path, method, data, null).body(), Object.class);
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
    public Object query(String pre, Integer sn, Object anchor) throws Exception {
        String path = "/queries";
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pre", pre);
        if (sn != null) {
            data.put("sn", sn.toString());
        }
        if (anchor != null) {
            data.put("anchor", anchor);
        }
        String method = "POST";
        return Utils.fromJson(client.fetch(path, method, data, null).body(), Object.class);
    }
}
