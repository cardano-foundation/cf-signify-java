package org.cardanofoundation.signify.core;

import org.cardanofoundation.signify.cesr.CesrNumber;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CesrNumberTest {

    @Test
    @DisplayName("should hold thresholds")
    void shouldHoldThresholds() {
        CesrNumber n = new CesrNumber(RawArgs.builder().build(), null, "0");
        assertEquals(BigInteger.ZERO, n.getNum());
        assertEquals("0", n.getNumh());

        n = new CesrNumber(RawArgs.builder().build(), BigInteger.ZERO, null);
        assertEquals(BigInteger.ZERO, n.getNum());
        assertEquals("0", n.getNumh());

        n = new CesrNumber(RawArgs.builder().build(), BigInteger.ONE, null);
        assertEquals(BigInteger.ONE, n.getNum());
        assertEquals("1", n.getNumh());

        n = new CesrNumber(RawArgs.builder().build(), BigInteger.valueOf(15), null);
        assertEquals(BigInteger.valueOf(15), n.getNum());
        assertEquals("f", n.getNumh());

        n = new CesrNumber(RawArgs.builder().build(), null, "1");
        assertEquals(BigInteger.ONE, n.getNum());
        assertEquals("1", n.getNumh());

        n = new CesrNumber(RawArgs.builder().build(), null, "f");  
        assertEquals(BigInteger.valueOf(15), n.getNum());
        assertEquals("f", n.getNumh());

        n = new CesrNumber(RawArgs.builder().build(), null, "15");
        assertEquals(BigInteger.valueOf(21), n.getNum());
        assertEquals("15", n.getNumh());
    }
} 