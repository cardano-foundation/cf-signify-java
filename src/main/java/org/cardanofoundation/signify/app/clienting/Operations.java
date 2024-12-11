package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.clienting.deps.OperationsDeps;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class Operations {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OperationsDeps client;

    public Operations(OperationsDeps client) {
        this.client = client;
    }

    public <T> Operation<T> get(String name) throws SodiumException, JsonProcessingException {
        String path = "/operations/" + name;
        String method = "GET";
        ResponseEntity<String> response = client.fetch(path, method, null, null);
        return Operation.fromObject(objectMapper.readValue(response.getBody(), new TypeReference<>() {})) ;
    }

    public List<Operation<?>> list(String type) throws SodiumException, JsonProcessingException {
        String path = "/operations" + (type != null ? "?type=" + type : "");
        String method = "GET";
        ResponseEntity<String> response = client.fetch(path, method, null, null);
        return objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
    }

    public void delete(String name) throws SodiumException {
        String path = "/operations/" + name;
        String method = "DELETE";
        client.fetch(path, method, null, null);
    }

    @SuppressWarnings("unchecked")
    public <T> Operation<T> wait(Operation<T> op, WaitOptions options) throws SodiumException, JsonProcessingException {
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
    @Builder
    public static class WaitOptions {
        private Integer minSleep;
        private Integer maxSleep;
        private Integer increaseFactor;
        private AbortSignal signal;

        // TODO mapping options.signal form signify-ts
    }
}