package org.cardanofoundation.signify.e2e.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

public class Retry {

    public static <T> T retry(Supplier<T> fn) {
        try {
            return retry(fn, RetryOptions.builder().build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T retry(Supplier<T> fn, RetryOptions options) throws InterruptedException {
        long start = System.currentTimeMillis();
        int retries = 0;
        int increaseFactor = 50;
        Exception cause = null;

        while (System.currentTimeMillis() - start < options.timeout && (options.maxRetries == null || retries < options.maxRetries)) {
            try {
                return fn.get();
            } catch (Exception e) {
                cause = e;
                retries++;
                int delay = Math.max(options.minSleep,
                        Math.min(options.maxSleep, (int) Math.pow(2, retries) * increaseFactor));
                Thread.sleep(delay);
            }
        }

        if (cause == null) {
            cause = new RuntimeException("Failed after " + retries + " attempts");
        }
        throw new RuntimeException(cause.getMessage() + " | Retries: " + retries + " | Max Attempts: " + options.maxRetries, cause);
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class RetryOptions {
        @Builder.Default
        private int maxSleep = 1000; // default max sleep in milliseconds
        @Builder.Default
        private int minSleep = 10;   // default min sleep in milliseconds

        private Integer maxRetries;// default infinite retries
        @Builder.Default
        private int timeout = 10000; // default timeout in milliseconds
    }
}
