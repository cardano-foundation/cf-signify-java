package org.cardanofoundation.signify.cesr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TholderTest {

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

        assertThrows(IllegalArgumentException.class, () -> new Tholder(null, null, -1));

        // Test invalid nested thresholds
        List<String> invalidNested1 = Arrays.asList("1/2", "1/2", Arrays.asList("1/3", "1/3", "1/3").toString());
        assertThrows(IllegalArgumentException.class, () -> new Tholder(null, null, invalidNested1));

        List<List<String>> invalidNested2 = Arrays.asList(
            Arrays.asList("1/2", "1/2"),
            Arrays.asList("1/4", "1/4", "1/4")
        );
        assertThrows(IllegalArgumentException.class, () -> new Tholder(null, null, invalidNested2));

        //TODO test after mathjs replacement
//        // Test fractional weights
//        List<String> weights = Arrays.asList("1/2", "1/2", "1/4", "1/4", "1/4");
//        tholder = new Tholder(null, null, weights);
//        assertTrue(tholder.isWeighted());
//        assertEquals(5, tholder.getSize());
//        assertTrue(tholder.satisfy(Arrays.asList(0, 1)));
//        assertTrue(tholder.satisfy(Arrays.asList(0, 2, 4)));
//        assertTrue(tholder.satisfy(Arrays.asList(1, 3, 4)));
//        assertTrue(tholder.satisfy(Arrays.asList(0, 1, 2, 3, 4)));
//        assertTrue(tholder.satisfy(Arrays.asList(0, 2, 3)));
//        assertTrue(tholder.satisfy(Arrays.asList(0, 0, 1, 2, 1)));
//        assertFalse(tholder.satisfy(List.of(0)));
//        assertFalse(tholder.satisfy(Arrays.asList(0, 2)));
//        assertFalse(tholder.satisfy(Arrays.asList(2, 3, 4)));
//
//        // Test nested weighted thresholds
//        List<List<String>> nestedWeights = Arrays.asList(
//            Arrays.asList("1/2", "1/2", "1/2"),
//            Arrays.asList("1/3", "1/3", "1/3", "1/3")
//        );
//        tholder = new Tholder(null, null, nestedWeights);
//        assertTrue(tholder.isWeighted());
//        assertEquals(7, tholder.getSize());
//        assertTrue(tholder.satisfy(Arrays.asList(0, 2, 3, 5, 6)));
//        assertTrue(tholder.satisfy(Arrays.asList(1, 2, 3, 4, 5)));
//        assertFalse(tholder.satisfy(Arrays.asList(0, 1)));
//        assertFalse(tholder.satisfy(Arrays.asList(0, 2)));
//        assertFalse(tholder.satisfy(Arrays.asList(4, 5, 6)));
//        assertFalse(tholder.satisfy(Arrays.asList(1, 4, 5, 6)));
//
//        // Test JSON string input
//        String jsonWeights = "[[\"1/2\", \"1/2\", \"1/4\", \"1/4\", \"1/4\"], [\"1/1\", \"1\"]]";
//        tholder = new Tholder(null, null, jsonWeights);
//        assertTrue(tholder.isWeighted());
//        assertEquals(7, tholder.getSize());
//        assertTrue(tholder.satisfy(Arrays.asList(1, 2, 3, 5)));
//        assertTrue(tholder.satisfy(Arrays.asList(0, 1, 6)));
//        assertFalse(tholder.satisfy(Arrays.asList(0, 1)));
//        assertFalse(tholder.satisfy(Arrays.asList(5, 6)));
//        assertFalse(tholder.satisfy(Arrays.asList(2, 3, 4)));
//        assertFalse(tholder.satisfy(List.of()));
    }
} 