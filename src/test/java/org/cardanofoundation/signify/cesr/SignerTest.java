package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignerTest {

    @Test
    @DisplayName("should sign things")
    void testSignThings() throws SodiumException {

        RawArgs rawArgs = RawArgs.builder()
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build();
        final Signer signer = new Signer(rawArgs);

        assertEquals(signer.getCode(), Codex.MatterCodex.Ed25519_Seed.getValue());
        assertEquals(signer.getRaw().length, Matter.getRawSize(signer.getCode()));
        assertEquals(signer.getVerfer().getCode(), Codex.MatterCodex.Ed25519.getValue());
        assertEquals(signer.getVerfer().getRaw().length, Matter.getRawSize(signer.getVerfer().getCode()));

        final String ser = "abcdefghijklmnopqrstuvwxyz0123456789";
        Cigar cigar = (Cigar) signer.sign(ser.getBytes());

        assertEquals(cigar.getCode(), Codex.MatterCodex.Ed25519_Sig.getValue());
        assertEquals(cigar.getRaw().length, Matter.getRawSize(cigar.getCode()));

        boolean result = signer.getVerfer().verify(cigar.getRaw(), ser.getBytes());
        assertTrue(result);

        Siger siger = (Siger) signer.sign(ser.getBytes(), 60, false, null);
        assertEquals(siger.getCode(), Codex.IndexerCodex.Ed25519_Sig.getValue());
        assertEquals(siger.getRaw().length, Indexer.getRawSize(siger.getCode()));

        result = signer.getVerfer().verify(siger.getRaw(), ser.getBytes());
        assertTrue(result);
    }
}