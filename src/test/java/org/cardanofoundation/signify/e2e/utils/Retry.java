package org.cardanofoundation.signify.e2e.utils;

import java.util.function.Supplier;

public class Retry {
    public static class RetryOptions {
        private int maxSleep = 1000; // default max sleep in milliseconds
        private int minSleep = 10;   // default min sleep in milliseconds
        private int maxRetries = Integer.MAX_VALUE; // default infinite retries
        private long timeout = 10000; // default timeout in milliseconds

        public RetryOptions setMaxSleep(int maxSleep) {
            this.maxSleep = maxSleep;
            return this;
        }

        public RetryOptions setMinSleep(int minSleep) {
            this.minSleep = minSleep;
            return this;
        }

        public RetryOptions setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RetryOptions setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }
    }

    public static <T> T retry(Supplier<T> fn, RetryOptions options) throws Exception {
        long start = System.currentTimeMillis();
        int retries = 0;
        int increaseFactor = 50;
        Exception cause = null;

        while (System.currentTimeMillis() - start < options.timeout && retries < options.maxRetries) {
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
            cause = new Exception("Failed after " + retries + " attempts");
        }
        throw new Exception(cause.getMessage() + " | Retries: " + retries + " | Max Attempts: " + options.maxRetries, cause);
    }
}
