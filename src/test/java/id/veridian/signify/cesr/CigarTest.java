package org.cardanofoundation.signify.cesr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CigarTest {

    @Test
    @DisplayName("Test Cigar subclass of Matter")
    public void testCigarSubclassOfMatter() {
        final String qsig64 = "0BCdI8OSQkMJ9r-xigjEByEjIua7LHH3AOJ22PQKqljMhuhcgh9nGRcKnsz5KvKd7K_H9-1298F4Id1DxvIoEmCQ";
        Cigar cigar = new Cigar(qsig64);

        assertEquals(cigar.getCode(), Codex.MatterCodex.Ed25519_Sig.getValue());
        assertEquals(cigar.getQb64(), qsig64);
        assertNull(cigar.getVerfer());
    }
}
