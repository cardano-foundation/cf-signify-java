package org.cardanofoundation.signify.cesr.util;

import static org.junit.jupiter.api.Assertions.*;

import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoreUtilTest {

    @Test
    @DisplayName("should encode and decode stuff")
    void testIntToB64AndBack() {
        String cs = CoreUtil.intToB64(0, 1);
        assertEquals("A", cs);
        int i = CoreUtil.b64ToInt(cs);
        assertEquals(0, i);

        cs = CoreUtil.intToB64(0, 0);
        assertEquals("", cs);
        assertThrows(InvalidValueException.class, () -> CoreUtil.b64ToInt(""));

        byte[] csb = CoreUtil.intToB64b(0, 1);
        assertArrayEquals("A".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(0, i);

        csb = CoreUtil.intToB64b(27, 1);
        assertArrayEquals("b".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(27, i);

        csb = CoreUtil.intToB64b(27, 1);
        assertArrayEquals("b".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(27, i);

        cs = CoreUtil.intToB64(27, 2);
        assertEquals("Ab", cs);
        i = CoreUtil.b64ToInt(cs);
        assertEquals(27, i);

        csb = CoreUtil.intToB64b(27, 2);
        assertArrayEquals("Ab".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(27, i);

        cs = CoreUtil.intToB64(80, 1);
        assertEquals("BQ", cs);
        i = CoreUtil.b64ToInt(cs);
        assertEquals(80, i);

        csb = CoreUtil.intToB64b(80, 1);
        assertArrayEquals("BQ".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(80, i);

        cs = CoreUtil.intToB64(4095, 1);
        assertEquals("__", cs);
        i = CoreUtil.b64ToInt(cs);
        assertEquals(4095, i);

        csb = CoreUtil.intToB64b(4095, 1);
        assertArrayEquals("__".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(4095, i);

        cs = CoreUtil.intToB64(4096, 1);
        assertEquals("BAA", cs);
        i = CoreUtil.b64ToInt(cs);
        assertEquals(4096, i);

        csb = CoreUtil.intToB64b(4096, 1);
        assertArrayEquals("BAA".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(4096, i);

        cs = CoreUtil.intToB64(6011, 1);
        assertEquals("Bd7", cs);
        i = CoreUtil.b64ToInt(cs);
        assertEquals(6011, i);

        csb = CoreUtil.intToB64b(6011, 1);
        assertArrayEquals("Bd7".getBytes(), csb);
        i = CoreUtil.b64ToInt(new String(csb));
        assertEquals(6011, i);
    }

    @Test
    @DisplayName("should encode base64url")
    void testEncodeBase64Url() {
        assertEquals("Zg", CoreUtil.encodeBase64Url("f".getBytes()));
        assertEquals("Zmk", CoreUtil.encodeBase64Url("fi".getBytes()));
        assertEquals("Zmlz", CoreUtil.encodeBase64Url("fis".getBytes()));
        assertEquals("ZmlzaA", CoreUtil.encodeBase64Url("fish".getBytes()));
        assertEquals("-A", CoreUtil.encodeBase64Url(new byte[]{(byte) 248}));
        assertEquals("_A", CoreUtil.encodeBase64Url(new byte[]{(byte) 252}));
    }

    @Test
    @DisplayName("should decode base64url")
    void testDecodeBase64Url() {
        assertArrayEquals("f".getBytes(), CoreUtil.decodeBase64Url("Zg"));
        assertArrayEquals("fi".getBytes(), CoreUtil.decodeBase64Url("Zmk"));
        assertArrayEquals("fis".getBytes(), CoreUtil.decodeBase64Url("Zmlz"));
        assertArrayEquals("fish".getBytes(), CoreUtil.decodeBase64Url("ZmlzaA"));
        assertArrayEquals(new byte[]{(byte) 248}, CoreUtil.decodeBase64Url("-A"));
        assertArrayEquals(new byte[]{(byte) 252}, CoreUtil.decodeBase64Url("_A"));
    }

    @Test
    @DisplayName("Test encode / decode compare with built in node Buffer")
    void testEncodeDecodeCompareWithNodeBuffer() {
        String text = "🏳️🏳️";
        String b64url = "8J-Ps--4j_Cfj7PvuI8";

        assertEquals(CoreUtil.encodeBase64Url(text.getBytes()), b64url);
        assertArrayEquals(CoreUtil.decodeBase64Url(b64url), text.getBytes());
    }

}