package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;

import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ShortageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SalterTest {

    @Test
    @DisplayName("Test the support functionality for salter subclass of crymat")
    void testSalterInstance() throws SodiumException {
        Salter salter = new Salter();  // defaults to CryTwoDex.Salt_128
        assertEquals(salter.getCode(), MatterCodex.Salt_128.getValue());
        assertEquals(salter.getRaw().length, Matter.getRawSize(salter.getCode()));

        byte[] raw = "0123456789abcdef".getBytes();
        salter = new Salter(RawArgs.builder()
                .code(MatterCodex.Salt_128.getValue())
                .raw(raw)
                .build());
        assertArrayEquals(salter.getRaw(), raw);
        assertEquals(salter.getQb64(), "0AAwMTIzNDU2Nzg5YWJjZGVm");

        Signer signer = salter.signer(MatterCodex.Ed25519_Seed.getValue(), true, "01", Tier.low, true);
        assertEquals(signer.getCode(), Codex.MatterCodex.Ed25519_Seed.getValue());
        assertEquals(signer.getRaw().length, Matter.getRawSize(signer.getCode()));
        assertEquals(signer.getVerfer().getCode(), MatterCodex.Ed25519.getValue());
        assertEquals(signer.getVerfer().getRaw().length, Matter.getRawSize(signer.getVerfer().getCode()));
        assertEquals(signer.getQb64(), "AMPsqBZxWdtYpBhrWnKYitwFa77s902Q-nX3sPTzqs0R");
        assertEquals(signer.getVerfer().getQb64(), "DFYFwZJOMNy3FknECL8tUaQZRBUyQ9xCv6F8ckG-UCrC");

        signer = salter.signer(MatterCodex.Ed25519_Seed.getValue(), true, "01", Tier.low, false);
        assertEquals(signer.getCode(), MatterCodex.Ed25519_Seed.getValue());
        assertEquals(signer.getRaw().length, Matter.getRawSize(signer.getCode()));
        assertEquals(signer.getVerfer().getCode(), MatterCodex.Ed25519.getValue());
        assertEquals(signer.getVerfer().getRaw().length, Matter.getRawSize(signer.getVerfer().getCode()));
        assertEquals(signer.getQb64(), "AEkqQiNTexWB9fTLpgJp_lXW63tFlT-Y0_mgQww4o-dC");
        assertEquals(signer.getVerfer().getQb64(), "DPJGyH9H1M_SUSf18RzX8OqdyhxEyZJpKm5Em0PnpsWd");

        salter = new Salter("0AAwMTIzNDU2Nzg5YWJjZGVm");
        assertArrayEquals(salter.getRaw(), raw);
        assertEquals(salter.getQb64(), "0AAwMTIzNDU2Nzg5YWJjZGVm");

        assertThrows(ShortageException.class, () -> new Salter(""));

        salter = new Salter(RawArgs.builder()
                .code(MatterCodex.Salt_128.getValue())
                .raw(raw)
                .build(), Tier.low);
        assertArrayEquals(salter.getRaw(), raw);

        byte[] stretchTierNull = salter.stretch(32, "", null, true);
        byte[] stretchTierLow = salter.stretch(32, "", Tier.low, false);
        byte[] stretchTierMed = salter.stretch(32, "", Tier.med, false);
        byte[] stretchTierHigh = salter.stretch(32, "", Tier.high, false);

        byte[] expectedStretchTierNull = {(byte) 0xd4, 0x40, (byte) 0xeb, (byte) 0xa6, 0x78, (byte) 0x86, (byte) 0xdf, (byte) 0x93, (byte) 0xd6, 0x43, (byte) 0xdc, (byte) 0xb8, (byte) 0xa6, (byte) 0x9b, 0x02, (byte) 0xaf, 0x68, (byte) 0xc1, 0x6d, 0x28, 0x4c, (byte) 0xd6, (byte) 0xf6, (byte) 0x86, 0x59, 0x55, 0x3e, 0x24, 0x5b, (byte) 0xf9, (byte) 0xef, (byte) 0xc0};
        assertArrayEquals(stretchTierNull, expectedStretchTierNull);

        byte[] expectedStretchTierLow = new byte[]{(byte) 0xf8, 0x65, (byte) 0x80, (byte) 0xba, 0x58, 0x08, (byte) 0xb9, (byte) 0xba, (byte) 0xc6, 0x1e, (byte) 0x84, 0x0d, 0x1d, (byte) 0xac, (byte) 0xa7, 0x5c, (byte) 0x82, 0x57, 0x63, 0x40, 0x60, 0x13, (byte) 0xfd, 0x02, 0x34, 0x74, (byte) 0x8c, 0x74, (byte) 0xd3, 0x01, 0x19, (byte) 0xe9};
        assertArrayEquals(stretchTierLow, expectedStretchTierLow);

        byte[] expectedStretchTierMed = new byte[]{(byte) 0x2c, (byte) 0xf3, (byte) 0x8c, (byte) 0xbb, (byte) 0xe9, (byte) 0x29, (byte) 0x0a, (byte) 0x53, (byte) 0x51, (byte) 0xec, (byte) 0xad, (byte) 0x8c, (byte) 0x39, (byte) 0x3f, (byte) 0xaf, (byte) 0xb8, (byte) 0xb0, (byte) 0xb3, (byte) 0xcd, (byte) 0x42, (byte) 0xda, (byte) 0xd8, (byte) 0xb6, (byte) 0xf7, (byte) 0x0d, (byte) 0xf6, (byte) 0x44, (byte) 0x7d, (byte) 0x5a, (byte) 0xb9, (byte) 0x59, (byte) 0x16};
        assertArrayEquals(stretchTierMed, expectedStretchTierMed);

        byte[] expectedStretchTierHigh = new byte[]{(byte) 0x28, (byte) 0xcd, (byte) 0xc4, (byte) 0xb8, (byte) 0x35, (byte) 0xcd, (byte) 0xe8, (byte) 0x3a, (byte) 0xfc, (byte) 0x00, (byte) 0x8b, (byte) 0xfd, (byte) 0xa6, (byte) 0x09, (byte) 0x6a, (byte) 0x2e, (byte) 0x79, (byte) 0x98, (byte) 0x0b, (byte) 0x04, (byte) 0x1c, (byte) 0xe3, (byte) 0x68, (byte) 0x42, (byte) 0x63, (byte) 0x21, (byte) 0x49, (byte) 0xe4, (byte) 0x39, (byte) 0x4b, (byte) 0x16, (byte) 0x2d};
        assertArrayEquals(stretchTierHigh, expectedStretchTierHigh);
    }

}