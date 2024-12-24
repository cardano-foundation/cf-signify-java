package org.cardanofoundation.signify.e2e.utils;


import java.util.Arrays;
import java.util.List;

public class ResolveEnv {
    public enum TestEnvironmentPreset {
        LOCAL, DOCKER
    }
    public record EnvironmentConfig(TestEnvironmentPreset preset,
                                    String url,
                                    String bootUrl,
                                    String vleiServerUrl,
                                    List<String> witnessUrls,
                                    List<String> witnessIds) {

    }

    private static final String WAN = "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha";
    private static final String WIL = "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM";
    private static final String WES = "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX";

    public static EnvironmentConfig resolveEnvironment(String input) {
        TestEnvironmentPreset preset;
        if (input != null) {
            preset = TestEnvironmentPreset.valueOf(input.toUpperCase());
        } else {
            String envPreset = System.getenv("TEST_ENVIRONMENT");
            preset = envPreset != null ? TestEnvironmentPreset.valueOf(envPreset.toUpperCase()) : TestEnvironmentPreset.DOCKER;
        }

        String url = "http://127.0.0.1:3901";
        String bootUrl = "http://127.0.0.1:3903";

        return switch (preset) {
            case DOCKER -> new EnvironmentConfig(
                    preset,
                    url,
                    bootUrl,
                    "http://vlei-server:7723",
                    Arrays.asList(
                            "http://witness-demo:5642",
                            "http://witness-demo:5643",
                            "http://witness-demo:5644"
                    ),
                    Arrays.asList(WAN, WIL, WES)
            );
            case LOCAL -> new EnvironmentConfig(
                    preset,
                    url,
                    bootUrl,
                    "http://localhost:7723",
                    Arrays.asList(
                            "http://localhost:5642",
                            "http://localhost:5643",
                            "http://localhost:5644"
                    ),
                    Arrays.asList(WAN, WIL, WES)
            );
            default -> throw new IllegalArgumentException("Unknown test environment preset: " + preset);
        };
    }
}
