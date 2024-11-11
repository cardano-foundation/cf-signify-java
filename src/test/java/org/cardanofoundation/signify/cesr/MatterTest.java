package org.cardanofoundation.signify.cesr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import org.cardanofoundation.signify.cesr.Codex.LargeVarRawSizeCodex;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Codex.SmallVarRawSizeCodex;
import org.cardanofoundation.signify.cesr.Matter.Sizage;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ConversionException;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ShortageException;
import org.cardanofoundation.signify.cesr.exceptions.material.RawMaterialException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;


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

    @Test
    void testSizages() {
        Map<String, Sizage> sizageMap = Matter.sizes;

        for (Map.Entry<String, Sizage> entry : sizageMap.entrySet()) {
            String code = entry.getKey();
            Sizage sizage = entry.getValue();

            Integer hs = sizage.hs;
            Integer ss = sizage.ss;
            Integer fs = sizage.fs;
            Integer ls = sizage.ls;
            int cs = hs + ss;

            assertTrue(hs > 0 && ss >= 0 && ls >= 0 && ls < 3);

            if (fs == null) { // variable sized
                assertTrue(ss > 0 && cs % 4 != 1);

                String firstChar = code.substring(0, 1);

                // assumes that Matter methods also ensure (ls + rs) % 3 == 0 i.e.
                // variable raw with lead is 24 bit aligned, where rs is raw size.
                assertTrue(SmallVarRawSizeCodex.has(firstChar) || LargeVarRawSizeCodex.has(firstChar));

                if (SmallVarRawSizeCodex.has(firstChar)) { // small variable sized code
                    assertTrue(hs == 2 && ss == 2);
                    assertEquals(firstChar, SmallVarRawSizeCodex.fromLsIndex(ls).getValue());

                    switch (firstChar) {
                        case "4" -> assertEquals(0, ls);
                        case "5" -> assertEquals(1, ls);
                        case "6" -> assertEquals(2, ls);
                        default -> {
                            assert false;
                        }
                    }
                } else if (LargeVarRawSizeCodex.has(firstChar)) { // large variable sized code
                    assertTrue(hs == 4 && ss == 4);
                    assertEquals(firstChar, LargeVarRawSizeCodex.fromLsIndex(ls).getValue());

                    switch (firstChar) {
                        case "7" -> assertEquals(0, ls);
                        case "8" -> assertEquals(1, ls);
                        case "9" -> assertEquals(2, ls);
                        default -> {
                            assert false;
                        }
                    }
                } else {
                    assert false;
                }
            } else { // fixed size
                String firstChar = code.substring(0, 1);
                assertFalse(SmallVarRawSizeCodex.has(firstChar) && LargeVarRawSizeCodex.has(firstChar));

                assertTrue(fs > 0 && fs % 4 != 1);
                assertTrue(fs >= cs);
                assertTrue(cs % 4 != 3); // prevent ambiguous conversion

                if (ss > 0 && fs == cs) { // special soft value with raw empty
                    assertEquals(0, ls); // no lead
                    assertEquals(Matter.getRawSize(code), 0);
                }

                int rs = ((fs - cs) * 3 / 4) - ls; // raw size bytes sans lead
                assertEquals((int) Math.ceil((rs + ls) * 4.0 / 3) + cs, fs); // sextets add up

                int ps = (3 - ((rs + ls) % 3)) % 3; // net pad size given raw with lead
                assertEquals(ps, cs % 4); // ensure correct midpad zero bits for cs

                if (firstChar.matches("[ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz]")) {
                    assertEquals(1, code.length());
                } else if (firstChar.equals("0")) {
                    assertEquals(2, code.length());
                } else if (firstChar.equals("1")) {
                    assertEquals(4, code.length());
                    assertEquals(0, ls);
                } else if (firstChar.equals("2")) {
                    assertEquals(4, code.length());
                    assertEquals(1, ls);
                } else if (firstChar.equals("3")) {
                    assertEquals(4, code.length());
                    assertEquals(2, ls);
                } else {
                    assertFalse(firstChar.matches("[456789-_]")); // count or op code
                }
            }
        }

    }

    @Test
    @DisplayName("Verify first hs Sizes matches hs in Codes for same first char")
    void testHards() {
        Map<String, Sizage> sizageMap = Matter.sizes;
        Map<String, Integer> hardsMap = Matter.hards;

        for (Map.Entry<String, Sizage> entry : sizageMap.entrySet()) {
            String code = entry.getKey();
            Sizage sizage = entry.getValue();
            String firstChar = code.substring(0, 1);
            assertEquals(sizage.hs, hardsMap.get(firstChar));
        }
    }

    @Test
    @DisplayName("Verify Matter instances with raw code is Ed25519N")
    void testMatterInstanceWithRawCodeIsEd25519N() {
        // byte array for verkey
        byte[] verkey = new byte[]{(byte) 0x69, (byte) 0x4E, (byte) 0x89, (byte) 0x47, (byte) 0x69, (byte) 0xE6, (byte) 0xC3, (byte) 0x26,
                (byte) 0x7E, (byte) 0x8B, (byte) 0x47, (byte) 0x7C, (byte) 0x25, (byte) 0x90, (byte) 0x28, (byte) 0x4C,
                (byte) 0xD6, (byte) 0x47, (byte) 0xDD, (byte) 0x42, (byte) 0xEF, (byte) 0x60, (byte) 0x07, (byte) 0xD2,
                (byte) 0x54, (byte) 0xFC, (byte) 0xE1, (byte) 0xCD, (byte) 0x2E, (byte) 0x9B, (byte) 0xE4, (byte) 0x23};

        // String for prefix
        String prefix = "BGlOiUdp5sMmfotHfCWQKEzWR91C72AH0lT84c0um-Qj";

        // byte array for prefixb
        byte[] prefixb = prefix.getBytes(StandardCharsets.UTF_8);

        // byte array for prebin
        byte[] prebin = new byte[]{(byte) 0x04, (byte) 0x69, (byte) 0x4E, (byte) 0x89, (byte) 0x47, (byte) 0x69, (byte) 0xE6, (byte) 0xC3,
                (byte) 0x26, (byte) 0x7E, (byte) 0x8B, (byte) 0x47, (byte) 0x7C, (byte) 0x25, (byte) 0x90, (byte) 0x28,
                (byte) 0x4C, (byte) 0xD6, (byte) 0x47, (byte) 0xDD, (byte) 0x42, (byte) 0xEF, (byte) 0x60, (byte) 0x07,
                (byte) 0xD2, (byte) 0x54, (byte) 0xFC, (byte) 0xE1, (byte) 0xCD, (byte) 0x2E, (byte) 0x9B, (byte) 0xE4,
                (byte) 0x23};

        RawArgs rawArgs = RawArgs.builder()
                .raw(verkey)
                .code(MatterCodex.Ed25519N.getValue()) // default code is MtrDex.Ed25519N
                .build();
        Matter matter = new Matter(rawArgs);
        assertArrayEquals(matter.getRaw(), verkey);
        assertEquals(matter.getCode(), MatterCodex.Ed25519N.getValue());
        assertEquals(matter.getQb64(), prefix);
        assertArrayEquals(matter.getQb64b(), prefixb);
        assertEquals(matter.getSize(), -1);
        matter._exfil(prefix);
        assertEquals(matter.getCode(), MatterCodex.Ed25519N.getValue());
        assertArrayEquals(matter.getRaw(), verkey);
        assertFalse(matter.isTransferable());


        // test from qb64
        matter = new Matter(prefix);
        assertArrayEquals(matter.getRaw(), verkey);
        assertEquals(matter.getCode(), MatterCodex.Ed25519N.getValue());

        // test non-zero pad bits in qb64 init ps == 1
        String badPrefix1 = "B_AAY2RlZmdoaWprbG1ub3BxcnN0dXYwMTIzNDU2Nzg5";
        Exception ex = assertThrows(ConversionException.class, () -> new Matter(badPrefix1));
        assertEquals(ex.getMessage(), "Non zeroed prepad bits = 000002 in _");

        // test non-zero pad bits in qb64 init ps == 2
        String badPrefix2 = "0A_wMTIzNDU2Nzg5YWJjZGVm";
        ex = assertThrows(ConversionException.class, () -> new Matter(badPrefix2));
        assertEquals(ex.getMessage(), "Non zeroed prepad bits = 000008 in _");

        // test truncates extra bytes from qb64 parameter
        String longPrefix = prefix + "ABCD";
        matter = new Matter(longPrefix);
        assertEquals(matter.getQb64().length(), Matter.sizes.get(matter.getCode()).fs);

        // test raises ShortageError if not enough bytes in qb64 parameter
        String shortPrefix = prefix.substring(0, prefix.length() - 4);
        ex = assertThrows(ShortageException.class, () -> new Matter(shortPrefix));
        assertEquals(ex.getMessage(), "Need 4 more chars.");


        // test truncates extra bytes from raw parameter
        final byte[] longVerkey = new byte[verkey.length + 3];
        System.arraycopy(verkey, 0, longVerkey, 0, verkey.length);
        longVerkey[verkey.length] = 10;
        longVerkey[verkey.length + 1] = 11;
        longVerkey[verkey.length + 2] = 12;
        matter = new Matter(RawArgs.builder()
                .raw(longVerkey)
                .code(MatterCodex.Ed25519N.getValue())
                .build());
        assertArrayEquals(matter.getRaw(), verkey);

        // test raises ShortageError if not enough bytes in raw parameter
        byte[] shortVerkey = Arrays.copyOf(verkey, verkey.length - 3);
        ex = assertThrows(RawMaterialException.class, () -> new Matter(RawArgs.builder()
                .raw(shortVerkey)
                .code(MatterCodex.Ed25519N.getValue())
                .build()));
        assertEquals(ex.getMessage(), "Not enougth raw bytes for code=B expected 32 got 29.");

        // test prefix on full identifier

        String fullIdentifier = ":mystuff/mypath/toresource?query=what#fragment";
        String both = prefix + fullIdentifier;
        matter = new Matter(both);
        assertEquals(matter.getQb64(), prefix);
        assertArrayEquals(matter.getQb64b(), prefixb);
        assertArrayEquals(matter.getRaw(), verkey);

        // test nongreedy prefixb on full identifier
        byte[] bothb = new byte[prefixb.length + fullIdentifier.getBytes().length];
        System.arraycopy(prefixb, 0, bothb, 0, prefixb.length);
        System.arraycopy(fullIdentifier.getBytes(), 0, bothb, prefixb.length, fullIdentifier.getBytes().length);
        matter = new Matter(bothb);
        assertEquals(matter.getQb64(), prefix);
        assertArrayEquals(matter.getQb64b(), prefixb);
        assertArrayEquals(matter.getRaw(), verkey);
    }

    @Test
    @DisplayName("Verify Matter instances with other codes")
    void testMatterInstanceWithOtherRawCodes() {
        // with raw code is Ed25519_Sig
        byte[] sig64b = new byte[]{
                (byte) 0x99, (byte) 0xd2, 0x3c, 0x39, 0x24, 0x24, 0x30, (byte) 0x9f, 0x6b, (byte) 0xfb, 0x18, (byte) 0xa0, (byte) 0x8c, 0x40, 0x72, 0x12,
                0x32, 0x2e, 0x6b, (byte) 0xb2, (byte) 0xc7, 0x1f, 0x70, 0x0e, 0x27, 0x6d, (byte) 0x8f, 0x40, (byte) 0xaa, (byte) 0xa5, (byte) 0x8c, (byte) 0xc8,
                0x6e, (byte) 0x85, (byte) 0xc8, 0x21, (byte) 0xf6, 0x71, (byte) 0x91, 0x70, (byte) 0xa9, (byte) 0xec, (byte) 0xcf, (byte) 0x92, (byte) 0xaf, 0x29, (byte) 0xde, (byte) 0xca,
                (byte) 0xfc, 0x7f, 0x7e, (byte) 0xd7, 0x6f, 0x7c, 0x17, (byte) 0x82, 0x1d, (byte) 0xd4, 0x3c, 0x6f, 0x22, (byte) 0x81, 0x26, 0x09
        };

        String sig64 = CoreUtil.encodeBase64Url(sig64b);

        assertEquals("mdI8OSQkMJ9r-xigjEByEjIua7LHH3AOJ22PQKqljMhuhcgh9nGRcKnsz5KvKd7K_H9-1298F4Id1DxvIoEmCQ", sig64);
        assertArrayEquals(sig64b, CoreUtil.decodeBase64Url(sig64));


        String qsig64 = "0BCZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ";
        byte[] qsig64b = qsig64.getBytes();

        Matter matter = new Matter(RawArgs.builder()
                .code(MatterCodex.Ed25519_Sig.getValue())
                .raw(sig64b)
                .build());

        assertEquals(matter.getCode(), MatterCodex.Ed25519_Sig.getValue());
        assertArrayEquals(matter.getRaw(), sig64b);
        assertEquals(matter.getQb64(), qsig64);
        assertArrayEquals(matter.getQb64b(), qsig64b);
        assertTrue(matter.isTransferable());

        matter = new Matter(qsig64);
        assertEquals(matter.getCode(), MatterCodex.Ed25519_Sig.getValue());
        assertArrayEquals(matter.getRaw(), sig64b);
        assertEquals(matter.getQb64(), qsig64);
        assertArrayEquals(matter.getQb64b(), qsig64b);
        assertTrue(matter.isTransferable());

        matter = new Matter(qsig64b);
        assertEquals(matter.getCode(), MatterCodex.Ed25519_Sig.getValue());
        assertArrayEquals(matter.getRaw(), sig64b);
        assertEquals(matter.getQb64(), qsig64);
        assertArrayEquals(matter.getQb64b(), qsig64b);
        assertTrue(matter.isTransferable());

    }
}
