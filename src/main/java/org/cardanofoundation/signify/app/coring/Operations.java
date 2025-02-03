package org.cardanofoundation.signify.app.coring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.*;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Operations {
    private final OperationsDeps client;

    public Operations(OperationsDeps client) {
        this.client = client;
    }

    public <T> Operation<T> get(String name) throws SodiumException, IOException, InterruptedException {
        String path = "/operations/" + name;
        String method = "GET";
        HttpResponse<String> response = client.fetch(path, method, null, null);
        return Utils.fromJson(response.body(), new TypeReference<>() {
        });
    }

    public List<Operation<?>> list(String type) throws SodiumException, IOException, InterruptedException {
        String path = "/operations" + (type != null ? "?type=" + type : "");
        String method = "GET";
        HttpResponse<String> response = client.fetch(path, method, null, null);
        return Utils.fromJson(response.body(), new TypeReference<>() {
        });
    }

    public void delete(String name) throws SodiumException, IOException, InterruptedException {
        String path = "/operations/" + name;
        String method = "DELETE";
        client.fetch(path, method, null, null);
    }

    public <T> Operation<T> wait(Operation<T> op) throws SodiumException, IOException, InterruptedException {
        return wait(op, WaitOptions.builder().build(), System.currentTimeMillis());
    }

    public <T> Operation<T> wait(Operation<T> op, WaitOptions options) throws SodiumException, IOException, InterruptedException {
        return wait(op, options, System.currentTimeMillis());
    }

    public <T> Operation<T> wait(Operation<T> op, WaitOptions options, long startingTime) throws SodiumException, IOException, InterruptedException {
        int minSleep = options.getMinSleep();
        int maxSleep = options.getMaxSleep();
        int increaseFactor = options.getIncreaseFactor();

        if (op.getMetadata() != null && op.getMetadata().getDepends() != null && !op.getMetadata().getDepends().isDone()) {
            wait(op.getMetadata().getDepends(), options, startingTime);
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
