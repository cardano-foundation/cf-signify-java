package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.*;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class Operations {
    private final OperationsDeps client;

    public Operations(OperationsDeps client) {
        this.client = client;
    }

    /**
     * Get operation by name
     *
     * @param name Name or ID of the operation to retrieve
     * @return Optional containing the operation if found, or empty if not found
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws LibsodiumException if a Sodium error occurs
     */
    public <T> Optional<Operation<T>> get(String name) throws IOException, InterruptedException, LibsodiumException {
        String path = "/operations/" + name;
        String method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null);
        
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }
        
        return Optional.of(Utils.fromJson(response.body(), new TypeReference<>() {}));
    }

    public List<Operation<?>> list(String type) throws IOException, InterruptedException, LibsodiumException {
        String path = "/operations" + (type != null ? "?type=" + type : "");
        String method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null);
        return Utils.fromJson(response.body(), new TypeReference<>() {});
    }

    public void delete(String name) throws IOException, InterruptedException, LibsodiumException {
        String path = "/operations/" + name;
        String method = "DELETE";
        this.client.fetch(path, method, null);
    }
}
