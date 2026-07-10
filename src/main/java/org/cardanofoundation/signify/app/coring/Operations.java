package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.*;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
import org.cardanofoundation.signify.exception.OperationAbortedException;
import org.cardanofoundation.signify.exception.OperationFailedException;
import org.cardanofoundation.signify.exception.OperationNotFoundException;
import org.cardanofoundation.signify.exception.OperationTimeoutException;
import org.cardanofoundation.signify.exception.SignifyInterruptedException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.*;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.List;
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
    public <T extends Operation> Optional<T> get(String name, Class<T> type) {
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
    public Optional<Operation> get(String name) {
        return get(name, Operation.class);
    }

    /**
     * List operations, deserialized into the general Operation union type.
     */
    public List<Operation> list(String type) {
        String path = "/operations" + (type != null ? "?type=" + type : "");
        HttpResponse<String> response = this.client.fetch(path, "GET", null);
        return Utils.fromJson(response.body(), new TypeReference<>() {});
    }

    /**
     * List all operations.
     */
    public List<Operation> list() {
        return list(null);
    }

    public void delete(String name) {
        String path = "/operations/" + name;
        this.client.fetch(path, "DELETE", null);
    }

    /**
     * Wait for an operation to complete, returning the result as the general Operation union type.
     *
     * @param op The operation instance to wait for
     */
    public Operation wait(Operation op) {
        return wait(op, Operation.class, WaitOptions.builder().build(), System.currentTimeMillis());
    }

    /**
     * Wait for an operation to complete, returning the result as the given type.
     * Handles dependent operations automatically; fails if a dependent operation fails.
     *
     * @param op The operation instance to wait for
     * @param resultType The expected type of the completed operation (e.g., CredentialOperation.class)
     * @throws IllegalArgumentException if the completed operation is not of the expected type
     */
    public <T extends Operation> T wait(Operation op, Class<T> resultType) {
        return wait(op, resultType, WaitOptions.builder().build(), System.currentTimeMillis());
    }

    /**
     * Wait for an operation to complete, returning the result as the general Operation union type.
     *
     * @param op The operation instance to wait for
     * @param options Polling and timeout options
     */
    public Operation wait(Operation op, WaitOptions options) {
        return wait(op, Operation.class, options, System.currentTimeMillis());
    }

    public <T extends Operation> T wait(Operation op, Class<T> resultType, WaitOptions options) {
        return wait(op, resultType, options, System.currentTimeMillis());
    }

    private <T extends Operation> T wait(Operation op, Class<T> resultType, WaitOptions options, long startingTime) {
        int minSleep = options.getMinSleep();
        int maxSleep = options.getMaxSleep();
        int increaseFactor = options.getIncreaseFactor();

        String operationName = op.getName();

        waitOnDepends(op, options, startingTime);

        if (isDone(op)) {
            return castResult(op, resultType);
        }

        int retries = 0;

        while (true) {
            Operation current = get(operationName, Operation.class)
                    .orElseThrow(() -> new OperationNotFoundException(operationName));

            if (isDone(current)) {
                return castResult(current, resultType);
            }

            long delay = Math.max(minSleep, Math.min(maxSleep, (long) Math.pow(2, retries) * increaseFactor));
            retries++;

            Long timeout = options.getAbortSignal().getTimeout();
            if (timeout != null) {
                long remaining = timeout - (System.currentTimeMillis() - startingTime);
                if (remaining <= 0) {
                    throw new OperationTimeoutException(operationName, timeout);
                }
                // clamp so the timeout is honored to within one poll, then give
                // the operation a final fetch before timing out on the next pass
                delay = Math.min(delay, remaining);
            }
            options.getAbortSignal().throwIfAborted();

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SignifyInterruptedException(e);
            }
        }
    }

    private static <T extends Operation> T castResult(Operation op, Class<T> resultType) {
        if (!resultType.isInstance(op)) {
            throw new IllegalArgumentException("Operation " + op.getName() + " is a "
                    + op.getClass().getSimpleName() + ", not the requested " + resultType.getSimpleName());
        }
        return resultType.cast(op);
    }

    private static boolean isDone(Operation op) {
        return !(op instanceof PendingOperation);
    }

    /**
     * Returns the dependent operation embedded in the given operation's metadata,
     * or null if the operation has none.
     */
    public static KelOperation dependsOf(Operation operation) {
        return switch (operation) {
            case DelegatorOperation op when op.getMetadata() != null -> op.getMetadata().getDepends();
            case RegistryOperation op when op.getMetadata() != null -> op.getMetadata().getDepends();
            case CredentialOperation op when op.getMetadata() != null -> op.getMetadata().getDepends();
            default -> null;
        };
    }

    private void waitOnDepends(Operation operation, WaitOptions options, long startingTime) {
        KelOperation depOp = dependsOf(operation);

        if (depOp != null) {
            KelOperation depResult = isDone(depOp) ? depOp : wait(depOp, KelOperation.class, options, startingTime);
            if (depResult instanceof FailedOperation failedDep) {
                throw new OperationFailedException(failedDep);
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

        public void throwIfAborted() {
            if (isAborted()) {
                throw new OperationAbortedException(reason);
            }
        }
    }
}
