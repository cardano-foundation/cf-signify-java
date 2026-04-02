package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.*;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.Operation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Operations {
    private final OperationsDeps client;

    public Operations(OperationsDeps client) {
        this.client = client;
    }

    /**
     * Get operation by name, deserialized into a specific type.
     *
     * @param name Name or ID of the operation to retrieve
     * @param type The target class to deserialize into (e.g., CredentialOperation.class)
     * @return Optional containing the typed operation if found, or empty if not found
     */
    public <T> Optional<T> get(String name, Class<T> type) throws IOException, InterruptedException, LibsodiumException {
        String path = "/operations/" + name;
        HttpResponse<String> response = this.client.fetch(path, "GET", null);

        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }

        return Optional.of(Utils.fromJson(response.body(), type));
    }

    /**
     * Get operation by name, deserialized into the general Operation union type.
     *
     * @param name Name or ID of the operation to retrieve
     * @return Optional containing the Operation if found, or empty if not found
     */
    public Optional<Operation> get(String name) throws IOException, InterruptedException, LibsodiumException {
        return get(name, Operation.class);
    }

    /**
     * List operations, deserialized into the general Operation union type.
     */
    public List<Operation> list(String type) throws IOException, InterruptedException, LibsodiumException {
        String path = "/operations" + (type != null ? "?type=" + type : "");
        HttpResponse<String> response = this.client.fetch(path, "GET", null);
        return Utils.fromJson(response.body(), new TypeReference<>() {});
    }

    /**
     * List all operations.
     */
    public List<Operation> list() throws IOException, InterruptedException, LibsodiumException {
        return list(null);
    }

    public void delete(String name) throws IOException, InterruptedException, LibsodiumException {
        String path = "/operations/" + name;
        this.client.fetch(path, "DELETE", null);
    }

    /**
     * Wait for an operation to complete, returning the result as the general Operation union type.
     *
     * @param operationName The name of the operation to wait for
     */
    public Operation wait(String operationName) throws IOException, InterruptedException, LibsodiumException {
        return wait(operationName, Operation.class, WaitOptions.builder().build(), System.currentTimeMillis());
    }

    /**
     * Wait for an operation to complete, returning the result deserialized into the given type.
     * Handles dependent operations automatically.
     *
     * @param operationName The name of the operation to wait for
     * @param resultType    The target class to deserialize the final result into (e.g., CredentialOperation.class)
     */
    public <T> T wait(String operationName, Class<T> resultType) throws IOException, InterruptedException, LibsodiumException {
        return wait(operationName, resultType, WaitOptions.builder().build(), System.currentTimeMillis());
    }

    public <T> T wait(String operationName, Class<T> resultType, WaitOptions options) throws IOException, InterruptedException, LibsodiumException {
        return wait(operationName, resultType, options, System.currentTimeMillis());
    }

    @SuppressWarnings("unchecked")
    private <T> T wait(String operationName, Class<T> resultType, WaitOptions options, long startingTime) throws IOException, InterruptedException, LibsodiumException {
        int minSleep = options.getMinSleep();
        int maxSleep = options.getMaxSleep();
        int increaseFactor = options.getIncreaseFactor();

        // First fetch to check depends and initial state
        Map<String, Object> json = get(operationName, Map.class)
                .orElseThrow(() -> new IOException("Operation not found: " + operationName));

        // Wait on dependencies first
        waitOnDepends(json, options, startingTime);

        if (Boolean.TRUE.equals(json.get("done"))) {
            if (resultType == Map.class) {
                return (T) json;
            }
            return get(operationName, resultType)
                    .orElseThrow(() -> new IOException("Operation not found: " + operationName));
        }

        int retries = 0;

        while (true) {
            json = get(operationName, Map.class)
                    .orElseThrow(() -> new IOException("Operation not found: " + operationName));

            int delay = Math.max(minSleep, Math.min(maxSleep, (int) Math.pow(2, retries) * increaseFactor));
            retries++;

            if (Boolean.TRUE.equals(json.get("done"))) {
                if (resultType == Map.class) {
                    return (T) json;
                }
                return get(operationName, resultType)
                        .orElseThrow(() -> new IOException("Operation not found: " + operationName));
            }
            Thread.sleep(delay);

            if (options.getAbortSignal().getTimeout() != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startingTime > options.getAbortSignal().getTimeout()) {
                    options.getAbortSignal().abort("Timeout");
                }
            }

            options.getAbortSignal().throwIfAborted();
        }
    }

    @SuppressWarnings("unchecked")
    private void waitOnDepends(Map<String, Object> operationJson, WaitOptions options, long startingTime) throws IOException, InterruptedException, LibsodiumException {
        Object metadata = operationJson.get("metadata");
        if (metadata instanceof Map<?, ?> metaMap) {
            Object depends = metaMap.get("depends");
            if (depends instanceof Map<?, ?> depMap) {
                String depName = (String) depMap.get("name");
                Boolean depDone = depMap.get("done") instanceof Boolean b ? b : null;
                if (depName != null && !Boolean.TRUE.equals(depDone)) {
                    wait(depName, Map.class, options, startingTime);
                }
            }
        }
    }

    @Builder
    @Getter
    @Setter
    public static class WaitOptions {

        @Builder.Default
        private Integer minSleep = 10;
        @Builder.Default
        private Integer maxSleep = 10000;
        @Builder.Default
        private Integer increaseFactor = 50;

        @Builder.Default
        private AbortSignal abortSignal = new AbortSignal();
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AbortSignal {
        private final AtomicBoolean aborted = new AtomicBoolean(false);
        private Object reason;
        private Long timeout;

        public boolean isAborted() {
            return aborted.get();
        }

        public void abort(Object reason) {
            if (!isAborted()) {
                this.reason = reason;
                aborted.set(true);
            }
        }

        public void throwIfAborted() throws InterruptedException {
            if (isAborted()) {
                throw new InterruptedException("Operation aborted: " + reason.toString());
            }
        }
    }
}
