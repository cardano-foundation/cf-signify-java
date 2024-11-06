package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SalterTest {

    @Test
    @DisplayName("Should generate salts")
    void testGenerateSalts() {
        RawArgs rawArgs = RawArgs.builder().code(Codex.MatterCodex.Salt_128.getValue()).build();

        Salter salter = new Salter(rawArgs);
        assertNotNull(salter);
        assertEquals(salter.getQb64().length(), 24);

        final byte[] salt = new byte[]{-110, 78, -114, -70, -67, 77, -126, 3, -24, -8, -70, -59, 8, 0, 73, -74};

        rawArgs.setRaw(salt);
        salter = new Salter(rawArgs);
        assertNotNull(salter);
        assertEquals(salter.getQb64().length(), 24);
        assertEquals(salter.getQb64(), "0ACSTo66vU2CA-j4usUIAEm2");

        salter = new Salter("0ACSTo66vU2CA-j4usUIAEm2");
        assertArrayEquals(salter.getRaw(), salt);

        salter = new Salter("0ABa4cx6f0SdfwFawI0A7mOZ");
        System.out.println("0");
        final byte[] expectedRaw = new byte[]{90, -31, -52, 122, 127, 68, -99, 127, 1, 90, -64, -115, 0, -18, 99, -103};
        assertArrayEquals(salter.getRaw(), expectedRaw);
    }

    @Test
    @DisplayName("Salter.signer Should return a Signer")
    void shouldReturnASigner() throws SodiumException {
        Salter salter = new Salter("0ACSTo66vU2CA-j4usUIAEm2");
        Signer signer = salter.signer();
        assertNotNull(signer);
        assertEquals(signer.getVerfer().getQb64(), "DD28x2a4KCZ8f6OAcA856jAD1chNOo4pT8ICxyzJUJhj");
    }
}