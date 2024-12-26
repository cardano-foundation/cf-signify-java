package org.cardanofoundation.signify.e2e.utils;

import java.util.concurrent.Callable;

public class TestSteps {
    public <T> T step(String description, Callable<T> fn) throws Exception {
        long start = System.currentTimeMillis();

        try {
            T response = fn.call();
            System.out.println(
                    "Step - " + description + " - finished (" + (System.currentTimeMillis() - start) + "ms)");
            return response;
        } catch (Exception e) {
            throw new Exception("Step - " + description + " - failed", e);
        }
    }
}
