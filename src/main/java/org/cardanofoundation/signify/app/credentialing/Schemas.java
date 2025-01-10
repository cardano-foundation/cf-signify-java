package org.cardanofoundation.signify.app.credentialing;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.io.IOException;
import java.net.http.HttpResponse;

public class Schemas {
    private final SignifyClient client;

    /**
     * Schemas
     *
     * @param client SignifyClient instance
     */
    public Schemas(SignifyClient client) {
        this.client = client;
    }

    /**
     * Get a schema
     *
     * @param said SAID of the schema
     * @return an Object representing the schema
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws SodiumException      if a Sodium error occurs
     */
    public Object get(String said) throws IOException, InterruptedException, SodiumException {
        String path = "/schema/" + said;
        var method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null, null);
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * List schemas
     *
     * @return an Object representing the list of schemas
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws SodiumException      if a Sodium error occurs
     */
    public Object list() throws IOException, InterruptedException, SodiumException {
        String path = "/schema";
        String method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null, null);
        return Utils.fromJson(response.body(), Object.class);
    }
}