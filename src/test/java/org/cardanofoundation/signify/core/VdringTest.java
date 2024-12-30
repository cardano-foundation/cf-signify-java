package org.cardanofoundation.signify.core;

import org.cardanofoundation.signify.cesr.Serder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.DigestException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VdringTest {

    @Test
    @DisplayName("should create registry inception events")
    void shouldCreateRegistryInceptionEvents() throws DigestException {
        Vdring.VDRInceptArgs args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .toad(0)
                .build();
        Serder actual = Vdring.incept(args);
        assertEquals(44, actual.getPre().length());

        args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .toad(0)
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .build();
        actual = Vdring.incept(args);
        assertEquals("EDAsrwU75uoh8sii7w-KN-Txy2d0dhHiUP34TQVBJiPS", actual.getPre());
        assertEquals("E", actual.getCode());
        assertEquals(271, actual.getSize());
    }

    @Test
    @DisplayName("should fail on NB config with backers")
    void shouldFailOnNBConfigWithBackers() {
        Vdring.VDRInceptArgs args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .toad(0)
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .cnfg(Collections.singletonList("NB"))
                .baks(Collections.singletonList("a backer"))
                .build();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Vdring.incept(args));
        assertEquals("1 backers specified for NB vcp, 0 allowed", exception.getMessage());
    }

    @Test
    @DisplayName("should fail with duplicate backers")
    void shouldFailWithDuplicateBackers() {
        Vdring.VDRInceptArgs args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .toad(0)
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .baks(List.of("a backer", "a backer"))
                .build();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Vdring.incept(args));
        assertEquals("Invalid baks [a backer, a backer] has duplicates", exception.getMessage());
    }

    @Test
    @DisplayName("should fail with invalid toad config for backers")
    void shouldFailWithInvalidToadConfigForBackers() {
        Vdring.VDRInceptArgs args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .toad(0)
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .baks(List.of("a backer"))
                .build();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Vdring.incept(args));
        assertEquals("Invalid toad 0 for baks in [a backer]", exception.getMessage());
    }

    @Test
    @DisplayName("should fail with invalid toad for no backers")
    void shouldFailWithInvalidToadForNoBackers() {
        Vdring.VDRInceptArgs args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .toad(1)
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .build();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Vdring.incept(args));
        assertEquals("Invalid toad 1 for no baks", exception.getMessage());
    }

    @Test
    @DisplayName("should allow optional toad and no backers")
    void shouldAllowOptionalToadAndNoBackers() throws DigestException {
        Vdring.VDRInceptArgs args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .build();
        Serder actual = Vdring.incept(args);
        assertEquals("EDAsrwU75uoh8sii7w-KN-Txy2d0dhHiUP34TQVBJiPS", actual.getPre());
        assertEquals("E", actual.getCode());
        assertEquals(271, actual.getSize());
    }

    @Test
    @DisplayName("should allow optional toad and backers")
    void shouldAllowOptionalToadAndBackers() throws DigestException {
        Vdring.VDRInceptArgs args = Vdring.VDRInceptArgs.builder()
                .pre("ECJIoBpEcCWMzvquk861dXP8JJZ-vbmJczlDR-NYcE3g")
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .baks(List.of("a backer"))
                .toad(1)
                .build();
        Serder actual = Vdring.incept(args);
        String expectedPrefix = "ENlghG6_krj9YMzy5-E3j5sEjsd6FR1nskBtbtSQGOFL";
        assertEquals(expectedPrefix, actual.getPre());
        assertEquals("E", actual.getCode());
        assertEquals(281, actual.getSize());
    }
}