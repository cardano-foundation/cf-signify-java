package org.cardanofoundation.signify.cesr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatterTest {

    @Test
    @DisplayName("Should hold size values in 4 properties")
    void shouldHoldSizeValuesIn4Properties() {
        Matter.Sizage sizage = new Matter.Sizage(1, 2, 3, 4);
        assertEquals(sizage.hs, 1);
        assertEquals(sizage.ss, 2);
        assertEquals(sizage.fs, 3);
        assertEquals(sizage.ls, 4);
    }
}