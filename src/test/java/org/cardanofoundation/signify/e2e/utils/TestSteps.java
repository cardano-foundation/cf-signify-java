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

    public <T> void steps(String description, Callable<T> fn) {
        long start = System.currentTimeMillis();

        try {
            System.out.println("Step - " + description + " - started.");
            fn.call();
            System.out.println("Step - " + description + " - finished (" + (System.currentTimeMillis() - start) + "ms)");
        } catch (Exception e) {
            System.err.println("Step - " + description + " - failed");
            throw new RuntimeException("Step - " + description + " - failed", e);
        }
    }

    public void step(String description, Runnable action) {
        long start = System.currentTimeMillis();

        try {
            System.out.println("Step - " + description + " - started.");
            action.run();
            System.out.println("Step - " + description + " - finished (" + (System.currentTimeMillis() - start) + "ms)");
        } catch (Exception e) {
            System.err.println("Step - " + description + " - failed");
            throw new RuntimeException("Step - " + description + " - failed", e);
        }
    }
}
