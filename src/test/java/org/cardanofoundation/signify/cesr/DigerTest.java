package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigerTest {

    @Test
    @DisplayName("should generate digests")
    void shouldGenerateDigests() {
        // Create something to digest and verify
        byte[] ser = "abcdefghijklmnopqrstuvwxyz0123456789".getBytes();
        byte[] digest = CoreUtil.blake3_256(ser, 32);

        Diger diger = new Diger(
            RawArgs.builder()
                .raw(digest)
                .build()
        );
        assertEquals(MatterCodex.Blake3_256.getValue(), diger.getCode());

        Matter.Sizage sizage = Matter.sizes.get(diger.getCode());
        assertEquals(sizage.fs, diger.getQb64().length());
        boolean result = diger.verify(ser);
        assertTrue(result);

        byte[] modifiedSer = new byte[ser.length + "2j2idjpwjfepjtgi".length()];
        System.arraycopy(ser, 0, modifiedSer, 0, ser.length);
        System.arraycopy("2j2idjpwjfepjtgi".getBytes(), 0, modifiedSer, ser.length, "2j2idjpwjfepjtgi".length());
        result = diger.verify(modifiedSer);
        assertFalse(result);

        diger = new Diger(
            RawArgs.builder()
                .raw(digest)
                .code(MatterCodex.Blake3_256.getValue())
                .build()
        );
        assertEquals(MatterCodex.Blake3_256.getValue(), diger.getCode());

        assertEquals(
            "ELC5L3iBVD77d_MYbYGGCUQgqQBju1o4x1Ud-z2sL-ux",
            diger.getQb64()
        );
        sizage = Matter.sizes.get(diger.getCode());
        assertEquals(sizage.fs, diger.getQb64().length());

        result = diger.verify(ser);
        assertTrue(result);

        diger = new Diger(RawArgs.builder().build(), ser);
        assertEquals(
            "ELC5L3iBVD77d_MYbYGGCUQgqQBju1o4x1Ud-z2sL-ux",
            diger.getQb64()
        );
        sizage = Matter.sizes.get(diger.getCode());
        assertEquals(sizage.fs, diger.getQb64().length());
        result = diger.verify(ser);
        assertTrue(result);
    }
} 