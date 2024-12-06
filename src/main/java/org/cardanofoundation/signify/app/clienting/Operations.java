package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.clienting.deps.OperationsDeps;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class Operations {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OperationsDeps client;

    public Operations(OperationsDeps client) {
        this.client = client;
    }

    public <T> Operation<T> get(String name) throws SodiumException, IOException, InterruptedException {
        String path = "/operations/" + name;
        String method = "GET";
        HttpResponse<String> response = client.fetch(path, method, null, null);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public List<Operation<?>> list(String type) throws SodiumException, IOException, InterruptedException {
        String path = "/operations" + (type != null ? "?type=" + type : "");
        String method = "GET";
        HttpResponse<String> response = client.fetch(path, method, null, null);
        return objectMapper.readValue(response.body(), new TypeReference<>() {});
    }

    public void delete(String name) throws SodiumException, IOException, InterruptedException {
        String path = "/operations/" + name;
        String method = "DELETE";
        client.fetch(path, method, null, null);
    }

    public <T> Operation<T> wait(Operation<T> op, WaitOptions options) throws SodiumException, IOException, InterruptedException {
        int minSleep = options == null || options.getMinSleep() == null ? 10 : options.getMinSleep();
        int maxSleep = options == null || options.getMaxSleep() == null ? 10000 : options.getMaxSleep();
        int increaseFactor = options == null || options.getIncreaseFactor() == null ? 50 : options.getIncreaseFactor();

        if (op.getMetadata() != null && op.getMetadata().getDepends() != null && !op.getMetadata().getDepends().isDone()) {
            return (Operation<T>) wait(op.getMetadata().getDepends(), options);
        }

        if (op.isDone()) {
            return op;
        }

        int retries = 0;

        while (true) {
            op = this.get(op.getName());

            int delay = Math.max(minSleep, Math.min(maxSleep, (int) Math.pow(2, retries) * increaseFactor));
            retries++;

            if (op.isDone()) {
                return op;
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // Handle the interruption, e.g., by re-throwing or logging
                // TODO: handle options.signal
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }

    @Getter
    @Setter
    public static class WaitOptions {
        private Integer minSleep;
        private Integer maxSleep;
        private Integer increaseFactor;

        // TODO mapping options.signal form signify-ts
    }
}