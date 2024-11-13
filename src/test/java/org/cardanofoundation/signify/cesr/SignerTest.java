package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SignerTest {

    @Test
    @DisplayName("Test Signer instance")
    void testSignerInstance() throws SodiumException {

        // defaults provide Ed25519 signer Ed25519 verfer
        Signer signer = new Signer();
        assertEquals(signer.getCode(), Codex.MatterCodex.Ed25519_Seed.getValue());
        assertEquals(signer.getRaw().length, Matter.getRawSize(signer.getCode()));
        assertEquals(signer.getVerfer().getCode(), Codex.MatterCodex.Ed25519.getValue());
        assertEquals(signer.getVerfer().getRaw().length, Matter.getRawSize(signer.getVerfer().getCode()));


        // create something to sign and verify
        String ser = "abcdefghijklmnopqrstuvwxyz0123456789";
        Cigar cigar = (Cigar) signer.sign(ser.getBytes());
        assertEquals(cigar.getCode(), Codex.MatterCodex.Ed25519_Sig.getValue());
        assertEquals(cigar.getRaw().length, Matter.getRawSize(cigar.getCode()));

        boolean result = signer.getVerfer().verify(cigar.getRaw(), ser.getBytes());
        assertTrue(result);

        int index = 0;
        Siger siger = (Siger) signer.sign(ser.getBytes(), index, false, null);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), index);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        result = signer.getVerfer().verify(siger.getRaw(), (ser + "ABCDEFG").getBytes());
        assertFalse(result);
        assertArrayEquals(siger.getRaw(), cigar.getRaw());

        assertThrows(UnsupportedOperationException.class, () -> {
            new Signer(RawArgs.builder().code(Codex.MatterCodex.Ed25519N.getValue()).build());
        });

        // Non transferable defaults
        signer = new Signer(RawArgs.builder()
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build(), 
                false);

        assertEquals(signer.getCode(), Codex.MatterCodex.Ed25519_Seed.getValue());
        assertEquals(signer.getRaw().length, Matter.getRawSize(signer.getCode()));
        assertEquals(signer.getVerfer().getCode(), Codex.MatterCodex.Ed25519N.getValue());
        assertEquals(signer.getVerfer().getRaw().length, Matter.getRawSize(signer.getVerfer().getCode()));

        cigar = (Cigar) signer.sign(ser.getBytes());
        assertEquals(cigar.getCode(), Codex.MatterCodex.Ed25519_Sig.getValue());
        assertEquals(cigar.getRaw().length, Matter.getRawSize(cigar.getCode()));

        result = signer.getVerfer().verify(cigar.getRaw(), ser.getBytes());
        assertTrue(result);

        siger = (Siger) signer.sign(ser.getBytes(), index, false, null);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), index);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        result = signer.getVerfer().verify(siger.getRaw(), (ser + "ABCDEFG").getBytes());
        assertFalse(result);

        // non default seed
        byte[] seed = LazySodiumInstance.getInstance().randomBytesBuf(32); // crypto_sign_SEEDBYTES
        signer = new Signer(RawArgs.builder()
                .raw(seed)
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build());

        assertEquals(signer.getCode(), Codex.MatterCodex.Ed25519_Seed.getValue());
        assertEquals(signer.getRaw().length, Matter.getRawSize(signer.getCode()));
        assertArrayEquals(signer.getRaw(), seed);
        assertEquals(signer.getVerfer().getCode(), Codex.MatterCodex.Ed25519.getValue());
        assertEquals(signer.getVerfer().getRaw().length, Matter.getRawSize(signer.getVerfer().getCode()));

        cigar = (Cigar) signer.sign(ser.getBytes());
        assertEquals(cigar.getCode(), Codex.MatterCodex.Ed25519_Sig.getValue());
        assertEquals(cigar.getRaw().length, Matter.getRawSize(cigar.getCode()));

        result = signer.getVerfer().verify(cigar.getRaw(), ser.getBytes());
        assertTrue(result);

        index = 1;
        siger = (Siger) signer.sign(ser.getBytes(), index, false, null);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), index);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);
        assertArrayEquals(siger.getRaw(), cigar.getRaw());

        // different both so Big
        int ondex = 3;
        siger = (Siger) signer.sign(ser.getBytes(), index, false, ondex);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), ondex);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        // same but Big
        index = 67;
        siger = (Siger) signer.sign(ser.getBytes(), index, false, null);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), index);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        // different both so Big
        ondex = 67;
        siger = (Siger) signer.sign(ser.getBytes(), index, false, ondex);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Big_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), ondex);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        // current only
        index = 4;
        siger = (Siger) signer.sign(ser.getBytes(), index, true, null);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Crt_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), null);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        // ignores ondex if only
        siger = (Siger) signer.sign(ser.getBytes(), index, true, index + 2);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Crt_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), null);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        // big current only
        index = 65;
        siger = (Siger) signer.sign(ser.getBytes(), index, true, null);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Big_Crt_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), null);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        // ignores ondex if only
        siger = (Siger) signer.sign(ser.getBytes(), index, true, index + 2);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Big_Crt_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));
        assertEquals(siger.getIndex(), index);
        assertEquals(siger.getOndex(), null);

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);

        // use invalid code not SEED type code
        assertThrows(UnsupportedOperationException.class, () -> {
            new Signer(RawArgs.builder().raw(seed).code(Codex.MatterCodex.Ed25519N.getValue()).build());
        });
    }
}
