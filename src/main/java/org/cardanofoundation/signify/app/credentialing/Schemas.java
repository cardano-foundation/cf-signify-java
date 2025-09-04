package org.cardanofoundation.signify.app.credentialing;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Optional;

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
     * @return Optional containing the schema if found, or empty if not found
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws LibsodiumException   if a Sodium error occurs
     */
    public Optional<Object> get(String said) throws IOException, InterruptedException, LibsodiumException {
        String path = "/schema/" + said;
        var method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null);
        
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }
        
        return Optional.of(Utils.fromJson(response.body(), Object.class));
    }

    /**
     * List schemas
     *
     * @return an Object representing the list of schemas
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws LibsodiumException   if a Sodium error occurs
     */
    public Object list() throws IOException, InterruptedException, LibsodiumException {
        String path = "/schema";
        String method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null);
        return Utils.fromJson(response.body(), Object.class);
    }
}