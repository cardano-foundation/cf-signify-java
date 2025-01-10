package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Codex.NumCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.exceptions.material.RawMaterialException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @DisplayName("Test CesrNumber subclass of Matter")
    void testNumberInstance() {
        assertThrows(EmptyMaterialException.class, () ->
            new CesrNumber(RawArgs.builder().raw("".getBytes()).build()));
        assertThrows(RawMaterialException.class, 
            () -> new CesrNumber(RawArgs.builder()
                .raw("".getBytes())
                .code(MatterCodex.Ed25519.getValue())
                .build(), null, null));

        // when code provided does not dynamically size code
        assertThrows(InvalidCodeException.class,
            () -> new CesrNumber(RawArgs.builder()
                .code(MatterCodex.Ed25519.getValue())
                .build(), BigInteger.valueOf(256 * 256), null));

        // test defaults, num is None forces to zero, code dynamic
        CesrNumber number = new CesrNumber(RawArgs.builder().build());
        assertEquals(BigInteger.ZERO, number.getNum());
        assertEquals("0", number.getNumh());
        assertEquals(NumCodex.Short.getValue(), number.getCode());
        assertEquals("MAAA", number.getQb64());
        assertArrayEquals("MAAA".getBytes(), number.getQb64b());
        assertFalse(number.isPositive());


        // test num as empty string defaults to 0
        number = new CesrNumber("");
        assertEquals(BigInteger.ZERO, number.getNum());
        assertEquals("0", number.getNumh());

        // test negative number error
        assertThrows(InvalidValueException.class, 
            () -> new CesrNumber(BigInteger.valueOf(-5)));
        
        // force bigger code for smaller number like for lexicographic namespace
        // which must be fixed length no matter the numeric value such as sequence
        // numbers in namespaces for lmdb
        number = new CesrNumber(RawArgs.builder().code(NumCodex.Huge.getValue()).build(), BigInteger.ONE, null);
        assertArrayEquals("0AAAAAAAAAAAAAAAAAAAAAAB".getBytes(), number.getQb64b());
        assertEquals(16, number.getRaw().length);
        assertEquals(NumCodex.Huge.getValue(), number.getCode());
        assertEquals("0AAAAAAAAAAAAAAAAAAAAAAB", number.getQb64());
        assertEquals(BigInteger.ONE, number.getNum());
        assertEquals("1", number.getNumh());

        BigInteger bigNum = BigInteger.valueOf(256).pow(18).subtract(BigInteger.ONE);  // too big to represent
        assertEquals(bigNum, new BigInteger("22300745198530623141535718272648361505980415"));
        String bigNumh = bigNum.toString(16);
        assertEquals(bigNumh, "ffffffffffffffffffffffffffffffffffff");
        assertEquals(18 * 2, bigNumh.length());

        assertThrows(InvalidValueException.class,
            () -> new CesrNumber(bigNum));

        assertThrows(InvalidValueException.class, 
            () -> new CesrNumber(bigNumh));

        BigInteger num = BigInteger.valueOf(256).pow(2).subtract(BigInteger.ONE);
        assertEquals(num, new BigInteger("65535"));
        String numh = num.toString(16);
        assertEquals(numh, "ffff");
        assertEquals(2 * 2, numh.length());
        String code = NumCodex.Short.getValue();
        byte[] raw = new byte[] { (byte) 0xff, (byte) 0xff };
        String nqb64 = "MP__";

        number = new CesrNumber(num);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(numh);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(RawArgs.builder().raw(raw).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        num = BigInteger.valueOf(256).pow(4).subtract(BigInteger.ONE);
        assertEquals(num, new BigInteger("4294967295"));
        numh = num.toString(16);
        assertEquals(numh, "ffffffff");
        raw = new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        code = NumCodex.Long.getValue();
        nqb64 = "0HD_____";

        number = new CesrNumber(num);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(numh);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(RawArgs.builder().raw(raw).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());


        num = BigInteger.valueOf(256).pow(8).subtract(BigInteger.ONE);
        assertEquals(num, new BigInteger("18446744073709551615"));
        numh = num.toString(16);
        assertEquals(numh, "ffffffffffffffff");
        raw = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        code = NumCodex.Big.getValue();
        nqb64 = "NP__________";

        number = new CesrNumber(num);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(numh);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(RawArgs.builder().raw(raw).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        num = BigInteger.valueOf(256).pow(16).subtract(BigInteger.ONE);
        assertEquals(num, new BigInteger("340282366920938463463374607431768211455"));
        numh = num.toString(16);
        assertEquals(numh, "ffffffffffffffffffffffffffffffff");
        raw = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        code = NumCodex.Huge.getValue();
        nqb64 = "0AD_____________________";

        number = new CesrNumber(num);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(numh);
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        number = new CesrNumber(RawArgs.builder().raw(raw).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertArrayEquals(nqb64.getBytes(), number.getQb64b());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        
        // test with wrong size raw for code short
        num = BigInteger.valueOf(256 * 256 - 1);
        numh = num.toString(16);
        assertEquals(numh, "ffff");
        raw = new byte[] { (byte) 0xff, (byte) 0xff };
        code = NumCodex.Short.getValue();
        nqb64 = "MP__";

        // raw to large for code, then truncates
        byte[] raw2bad = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
       
        number = new CesrNumber(RawArgs.builder().raw(raw2bad).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        // raw to small for code raises error
        assertThrows(RawMaterialException.class, () -> {
            byte[] rbad = new byte[] { (byte) 0xff };
            String c = NumCodex.Short.getValue();
            new CesrNumber(RawArgs.builder().raw(rbad).code(c).build());
        });

        // test with wrong size raw for code long
        num = BigInteger.valueOf(256).pow(4).subtract(BigInteger.ONE);
        numh = num.toString(16);
        assertEquals(numh, "ffffffff");
        raw = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        code = NumCodex.Long.getValue();
        nqb64 = "0HD_____";

        // raw to large for code, then truncates
        raw2bad = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        number = new CesrNumber(RawArgs.builder().raw(raw2bad).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        // raw too small for code raises error
        assertThrows(RawMaterialException.class, () -> {
            byte[] rbad = new byte[] { (byte) 0xff };
            String c = NumCodex.Long.getValue();
            new CesrNumber(RawArgs.builder().raw(rbad).code(c).build());
        });

        // tests with wrong size raw for code big
        num = BigInteger.valueOf(256).pow(8).subtract(BigInteger.ONE);
        numh = num.toString(16);
        assertEquals(numh, "ffffffffffffffff");
        raw = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        code = NumCodex.Big.getValue();
        nqb64 = "NP__________";

        // raw to large for code, then truncates
        raw2bad = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        number = new CesrNumber(RawArgs.builder().raw(raw2bad).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        // raw too small for code raises error
        assertThrows(RawMaterialException.class, () -> {
            byte[] rbad = new byte[] { (byte) 0xff };
            String c = NumCodex.Big.getValue();
            new CesrNumber(RawArgs.builder().raw(rbad).code(c).build());
        });

        // test with wrong size raw for code huge
        num = BigInteger.valueOf(256).pow(16).subtract(BigInteger.ONE);
        numh = num.toString(16);
        assertEquals(numh, "ffffffffffffffffffffffffffffffff");
        raw = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        code = NumCodex.Huge.getValue();
        nqb64 = "0AD_____________________";

        // raw to large for code, then truncates
        raw2bad = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        number = new CesrNumber(RawArgs.builder().raw(raw2bad).code(code).build());
        assertEquals(code, number.getCode());
        assertArrayEquals(raw, number.getRaw());
        assertEquals(nqb64, number.getQb64());
        assertEquals(num, number.getNum());
        assertEquals(numh, number.getNumh());
        assertTrue(number.isPositive());

        // raw too small for code raises error
        assertThrows(RawMaterialException.class, () -> {
            byte[] rbad = new byte[] { (byte) 0xff };
            String c = NumCodex.Huge.getValue();
            new CesrNumber(RawArgs.builder().raw(rbad).code(c).build());
        });
    }
} 