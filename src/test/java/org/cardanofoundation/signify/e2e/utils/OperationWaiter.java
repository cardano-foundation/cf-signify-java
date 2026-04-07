package org.cardanofoundation.signify.e2e.utils;

import lombok.*;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.app.coring.Operations;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test utility for waiting on KERI async operations to complete.
 * <p>
 * Implements exponential backoff polling using {@link Operations#get(String)}
 * to check operation status. This logic is intentionally placed in test scope
 * so that library consumers can implement their own polling strategy.
 */
public class OperationWaiter {

    private OperationWaiter() {
    }

    public static <T> Operation<T> wait(Operations operations, Operation<T> op)
            throws IOException, InterruptedException, LibsodiumException {
        return wait(operations, op, WaitOptions.builder().build(), System.currentTimeMillis());
    }

    public static <T> Operation<T> wait(Operations operations, Operation<T> op, WaitOptions options)
            throws IOException, InterruptedException, LibsodiumException {
        return wait(operations, op, options, System.currentTimeMillis());
    }

    public static <T> Operation<T> wait(Operations operations, Operation<T> op, WaitOptions options, long startingTime)
            throws IOException, InterruptedException, LibsodiumException {
        int minSleep = options.getMinSleep();
        int maxSleep = options.getMaxSleep();
        int increaseFactor = options.getIncreaseFactor();

        if (op.getMetadata() != null && op.getMetadata().getDepends() != null && !op.getMetadata().getDepends().isDone()) {
            wait(operations, op.getMetadata().getDepends(), options, startingTime);
        }

        if (op.isDone()) {
            return op;
        }

        int retries = 0;

        while (true) {
            String opName = op.getName();
            op = operations.<T>get(opName).orElseThrow(() -> new IllegalArgumentException("Operation not found: " + opName));

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
