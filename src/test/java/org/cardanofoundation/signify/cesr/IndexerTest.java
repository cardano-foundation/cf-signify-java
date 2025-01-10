package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ConversionException;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ShortageException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidVarIndexException;
import org.cardanofoundation.signify.cesr.exceptions.material.RawMaterialException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class IndexerTest {


    @Test
    @DisplayName("should encode and decode dual indexed signatures")
    void shouldEncodeAndDecodeDualIndexedSignatures() {

        assertEquals(Indexer.sizes.get("A").hs, 1);
        assertEquals(Indexer.sizes.get("A").ss, 1);
        assertEquals(Indexer.sizes.get("A").os, 0);
        assertEquals(Indexer.sizes.get("A").fs, 88);
        assertEquals(Indexer.sizes.get("A").ls, 0);

        Indexer.sizes.forEach((key, value) -> assertEquals(Indexer.hards.get(key.substring(0, 1)), value.hs, key + " hs not set"));

        Indexer.sizes.forEach((key, value) -> {
            assertTrue(value.hs > 0, key + " hs incorrect");
            assertTrue(value.ss > 0, key + " ss incorrect");
            assertTrue(value.os >= 0, key + " os incorrect");

            if (value.os > 0) {
                assertEquals(value.os, (int) Math.floor((double) value.ss / 2), key + " os/ss incorrect");
            }

            if (value.fs != null) {
                assertTrue(value.fs > value.hs + value.ss, key + " fs incorrect");
                assertEquals(0, value.fs % 4, key + " fs mod incorrect");
            }
        });

        assertThrows(ShortageException.class, () -> new Indexer(""));
        assertThrows(Exception.class, () -> new Indexer(RawArgs.builder().build()));

        byte[] sig = new byte[]{
                -103, -46, 60, 57, 36, 36, 48, -97, 107, -5, 24, -96, -116, 64, 114, 18, 50, 46, 107, -78, -57, 31,
                112, 14, 39, 109, -113, 64, -86, -91, -116, -56, 110, -123, -56, 33, -10, 113, -111, 112, -87, -20,
                -49, -110, -81, 41, -34, -54, -4, 127, 126, -41, 111, 124, 23, -126, 29, -44, 60, 111, 34, -127, 38, 9
        };

        assertEquals(sig.length, 64);
        final int ps = (3 - (sig.length % 3)) % 3;
        final byte[] bytes = new byte[sig.length + ps];

        for (int i = 0; i < ps; i++) {
            bytes[i] = 0;
        }
        System.arraycopy(sig, 0, bytes, ps, sig.length);

        final String sig64 = CoreUtil.encodeBase64Url(bytes);
        assertEquals(sig64.length(), 88);
        assertEquals(sig64, "AACZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ");

        final String qsc = Codex.IndexerCodex.Ed25519_Sig.getValue() + CoreUtil.intToB64(0, 1);
        assertEquals(qsc, "AA");
        String qsig64 = qsc + sig64.substring(ps);
        assertEquals(qsig64, "AACZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ");
        assertEquals(qsig64.length(), 88);

        byte[] qsig2b = CoreUtil.decodeBase64Url(qsig64);
        assertEquals(qsig2b.length, 66);

        assertArrayEquals(qsig2b, new byte[]{
                0, 0, -103, -46, 60, 57, 36, 36, 48, -97, 107, -5, 24, -96, -116, 64, 114, 18, 50, 46, 107, -78, -57,
                31, 112, 14, 39, 109, -113, 64, -86, -91, -116, -56, 110, -123, -56, 33, -10, 113, -111, 112, -87, -20,
                -49, -110, -81, 41, -34, -54, -4, 127, 126, -41, 111, 124, 23, -126, 29, -44, 60, 111, 34, -127, 38, 9});

        RawArgs rawArgs = RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(sig)
                .build();
        Indexer indexer = new Indexer(rawArgs);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), 0);
        assertEquals(indexer.getOndex(), 0);
        assertArrayEquals(indexer.getQb64b(), qsig64.getBytes());

        indexer._exfil(qsig64);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), 0);
        assertEquals(indexer.getOndex(), 0);
        assertArrayEquals(indexer.getQb64b(), qsig64.getBytes());

        final byte[] longsig = new byte[sig.length + 3];
        System.arraycopy(sig, 0, longsig, 0, sig.length);
        longsig[sig.length] = 10;
        longsig[sig.length + 1] = 11;
        longsig[sig.length + 2] = 12;

        rawArgs = RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(longsig)
                .build();

        indexer = new Indexer(rawArgs);

        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertEquals(indexer.getIndex(), 0);
        assertEquals(indexer.getOndex(), 0);

        final byte[] shortsig = Arrays.copyOf(sig, sig.length - 3);

        assertThrows(RawMaterialException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(shortsig)
                .build())
        );

        indexer = new Indexer(qsig64);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), 0);
        assertEquals(indexer.getOndex(), 0);
        assertArrayEquals(indexer.getQb64b(), qsig64.getBytes());
        assertEquals(indexer.getQb64(), qsig64);

        final String badq64sig2 = "AA_Z0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ";
        assertThrows(ConversionException.class, () -> new Indexer(badq64sig2));

        final String longqsig64 = qsig64 + "ABCD";
        indexer = new Indexer(longqsig64);
        assertEquals(indexer.getQb64().length(), Indexer.sizes.get(indexer.getCode()).fs);

        final String shortqsig64 = qsig64.substring(0, qsig64.length() - 4);
        assertThrows(ShortageException.class, () -> new Indexer(shortqsig64));

        qsig64 = "AFCZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ";
        qsig2b = CoreUtil.decodeBase64Url(qsig64);
        assertEquals(qsig2b.length, 66);

        rawArgs = RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(sig)
                .build();

        indexer = new Indexer(rawArgs, 5, null);
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertEquals(indexer.getIndex(), 5);
        assertEquals(indexer.getOndex(), 5);
        assertArrayEquals(indexer.getQb64b(), qsig64.getBytes());
        assertEquals(indexer.getQb64(), qsig64);

        indexer._exfil(qsig64);
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertArrayEquals(indexer.getQb64b(), qsig64.getBytes());
        assertEquals(indexer.getQb64(), qsig64);

        indexer = new Indexer(rawArgs, 5, 5);
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertEquals(indexer.getIndex(), 5);
        assertEquals(indexer.getOndex(), 5);
        assertArrayEquals(indexer.getQb64b(), qsig64.getBytes());
        assertEquals(indexer.getQb64(), qsig64);

        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(sig)
                .build(), 5, 0)
        );

        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(sig)
                .build(), 5, 64)
        );

        indexer = new Indexer(qsig64);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), 5);
        assertEquals(indexer.getOndex(), 5);
        assertArrayEquals(indexer.getQb64b(), qsig64.getBytes());
        assertEquals(indexer.getQb64(), qsig64);

        // test index too big
        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(sig)
                .build(), 65, null)
        );

        // test negative index
        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Sig.getValue())
                .raw(sig)
                .build(), -1, null)
        );


        int index = 67;
        String qb64 = "2ABDBDCZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ";
        byte[] qb64b = qb64.getBytes();

        indexer = new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Big_Sig.getValue())
                .raw(sig)
                .build(), index, null);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertEquals(indexer.getOndex(), index);
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        indexer = new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Big_Sig.getValue())
                .raw(sig)
                .build(), index, index);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertEquals(indexer.getOndex(), index);
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        indexer = new Indexer(qb64);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertEquals(indexer.getOndex(), index);
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        index = 90;
        int ondex = 65;
        qb64 = "2ABaBBCZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ";
        qb64b = qb64.getBytes();

        indexer = new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Big_Sig.getValue())
                .raw(sig)
                .build(), index, ondex);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertEquals(indexer.getOndex(), ondex);
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        indexer = new Indexer(qb64);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertEquals(indexer.getOndex(), ondex);
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        index = 3;
        qb64 = "BDCZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ";
        qb64b = qb64.getBytes();

        indexer = new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Crt_Sig.getValue())
                .raw(sig)
                .build(), index, null);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Crt_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertNull(indexer.getOndex());
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        indexer = new Indexer(qb64);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Crt_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertNull(indexer.getOndex());
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Crt_Sig.getValue())
                .raw(sig)
                .build(), 3, 3)
        );

        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Crt_Sig.getValue())
                .raw(sig)
                .build(), 3, 5)
        );

        index = 68;
        qb64 = "2BBEAACZ0jw5JCQwn2v7GKCMQHISMi5rsscfcA4nbY9AqqWMyG6FyCH2cZFwqezPkq8p3sr8f37Xb3wXgh3UPG8igSYJ";
        qb64b = qb64.getBytes();

        indexer = new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Big_Crt_Sig.getValue())
                .raw(sig)
                .build(), index, null);

        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Big_Crt_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertNull(indexer.getOndex());
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        indexer = new Indexer(qb64);
        assertEquals(indexer.getCode(), Codex.IndexerCodex.Ed25519_Big_Crt_Sig.getValue());
        assertArrayEquals(indexer.getRaw(), sig);
        assertEquals(indexer.getIndex(), index);
        assertNull(indexer.getOndex());
        assertArrayEquals(indexer.getQb64b(), qb64b);
        assertEquals(indexer.getQb64(), qb64);

        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Big_Crt_Sig.getValue())
                .raw(sig)
                .build(), 68, 68)
        );

        assertThrows(InvalidVarIndexException.class, () ->
            new Indexer(RawArgs.builder()
                .code(Codex.IndexerCodex.Ed25519_Big_Crt_Sig.getValue())
                .raw(sig)
                .build(), 68, 70)
        );
    }
}