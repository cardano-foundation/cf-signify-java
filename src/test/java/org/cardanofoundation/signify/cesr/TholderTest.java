package org.cardanofoundation.signify.cesr;

import org.apache.commons.math3.fraction.Fraction;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TholderTest {

    private Fraction getFraction(String s) {
        String[] parts = s.split("/");
        int numerator = Integer.parseInt(parts[0]);
        int denominator = Integer.parseInt(parts[1]);
        return new Fraction(numerator, denominator);
    }


    @Test
    @DisplayName("should hold thresholds")
    void shouldHoldThresholds() {
        Tholder tholder = new Tholder(null, null, "b");
        assertEquals(11, tholder.getThold());
        assertEquals(11, tholder.getSize());
        assertArrayEquals(new byte[]{77, 65, 65, 76}, tholder.getLimen()); // b(MAAL)
        assertEquals("b", tholder.getSith());
        assertEquals("\"b\"", tholder.getJson());
        assertEquals(11, tholder.getNum());
        assertFalse(tholder.satisfy(Arrays.asList(1, 2, 3)));
        assertTrue(tholder.satisfy(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)));

        tholder = new Tholder(null, null, 11);
        assertEquals(11, tholder.getThold());
        assertEquals(11, tholder.getSize());
        assertArrayEquals(new byte[]{77, 65, 65, 76}, tholder.getLimen());
        assertEquals("b", tholder.getSith());
        assertEquals("\"b\"", tholder.getJson());
        assertEquals(11, tholder.getNum());
        assertFalse(tholder.satisfy(Arrays.asList(1, 2, 3)));
        assertTrue(tholder.satisfy(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)));

        tholder = new Tholder(null, null, 2);
        assertEquals(2, tholder.getThold());
        assertEquals(2, tholder.getSize());
        assertArrayEquals(new byte[]{77, 65, 65, 67}, tholder.getLimen()); // b(MAAC)
        assertEquals("2", tholder.getSith());
        assertEquals("\"2\"", tholder.getJson());
        assertEquals(2, tholder.getNum());
        assertFalse(tholder.satisfy(List.of(1)));
        assertTrue(tholder.satisfy(Arrays.asList(1, 2)));
        assertTrue(tholder.satisfy(Arrays.asList(1, 2, 3, 4)));

        assertThrows(InvalidValueException.class, () -> new Tholder(null, null, -1));

        // Test invalid nested thresholds
        List<String> invalidNested1 = Arrays.asList("1/2", "1/2", Arrays.asList("1/3", "1/3", "1/3").toString());
        assertThrows(Exception.class, () -> new Tholder(null, null, invalidNested1));

        List<List<String>> invalidNested2 = Arrays.asList(
                Arrays.asList("1/2", "1/2"),
                Arrays.asList("1/4", "1/4", "1/4")
        );
        assertThrows(Exception.class, () -> new Tholder(null, null, invalidNested2));

        // Test fractional weights
        List<String> weights = Arrays.asList("1/2", "1/2", "1/4", "1/4", "1/4");
        List<List<Fraction>> expectedThold = List.of(weights.stream()
                .map(this::getFraction)
                .toList());

        tholder = new Tholder(null, null, weights);
        assertTrue(tholder.isWeighted());
        assertEquals(5, tholder.getSize());

        List<List<Fraction>> actualThold = (List<List<Fraction>>) tholder.getThold();
        assertEquals(expectedThold, actualThold);

        assertTrue(tholder.satisfy(Arrays.asList(0, 1)));
        assertTrue(tholder.satisfy(Arrays.asList(0, 2, 4)));
        assertTrue(tholder.satisfy(Arrays.asList(1, 3, 4)));
        assertTrue(tholder.satisfy(Arrays.asList(0, 1, 2, 3, 4)));
        assertTrue(tholder.satisfy(Arrays.asList(0, 2, 3)));
        assertTrue(tholder.satisfy(Arrays.asList(0, 0, 1, 2, 1)));
        assertFalse(tholder.satisfy(List.of(0)));
        assertFalse(tholder.satisfy(Arrays.asList(0, 2)));
        assertFalse(tholder.satisfy(Arrays.asList(2, 3, 4)));

        // Test nested weighted thresholds
        List<List<String>> nestedWeights = Arrays.asList(
                Arrays.asList("1/2", "1/2", "1/2"),
                Arrays.asList("1/3", "1/3", "1/3", "1/3")
        );
        tholder = new Tholder(null, null, nestedWeights);
        assertTrue(tholder.isWeighted());
        assertEquals(7, tholder.getSize());

        expectedThold = nestedWeights.stream()
                .map(clause -> clause.stream()
                        .map(this::getFraction)
                        .toList())
                .toList();
        actualThold = (List<List<Fraction>>) tholder.getThold();
        assertEquals(expectedThold, actualThold);
        assertEquals(nestedWeights, tholder.getSith());

        assertTrue(tholder.satisfy(Arrays.asList(0, 2, 3, 5, 6)));
        assertTrue(tholder.satisfy(Arrays.asList(1, 2, 3, 4, 5)));
        assertFalse(tholder.satisfy(Arrays.asList(0, 1)));
        assertFalse(tholder.satisfy(Arrays.asList(0, 2)));
        assertFalse(tholder.satisfy(Arrays.asList(4, 5, 6)));
        assertFalse(tholder.satisfy(Arrays.asList(1, 4, 5, 6)));

        tholder = new Tholder(null, null, "[[\"1/2\", \"1/2\", \"1/4\", \"1/4\", \"1/4\"], [\"1/1\", \"1\"]]");
        assertTrue(tholder.isWeighted());
        assertEquals(7, tholder.getSize());

        expectedThold = List.of(
                List.of(new Fraction(1, 2), new Fraction(1, 2), new Fraction(1, 4), new Fraction(1, 4), new Fraction(1, 4)),
                List.of(new Fraction(1, 1), new Fraction(1, 1))
        );
        actualThold = (List<List<Fraction>>) tholder.getThold();
        assertEquals(expectedThold, actualThold);

        List<List<String>> expectedSith = List.of(
                List.of("1/2", "1/2", "1/4", "1/4", "1/4"),
                List.of("1", "1")
        );
        assertEquals(expectedSith, tholder.getSith());

        String expectedJson = "[[\"1/2\",\"1/2\",\"1/4\",\"1/4\",\"1/4\"],[\"1\",\"1\"]]";
        assertEquals(expectedJson, tholder.getJson());
        assertTrue(tholder.satisfy(Arrays.asList(1, 2, 3, 5)));
        assertTrue(tholder.satisfy(Arrays.asList(0, 1, 6)));
        assertFalse(tholder.satisfy(Arrays.asList(0, 1)));
        assertFalse(tholder.satisfy(Arrays.asList(5, 6)));
        assertFalse(tholder.satisfy(Arrays.asList(2, 3, 4)));
        assertFalse(tholder.satisfy(List.of()));
    }
} 
