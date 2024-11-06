package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.MessageEncoder;
import com.goterl.lazysodium.utils.Base64MessageEncoder;
import com.goterl.lazysodium.utils.HexMessageEncoder;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.goterl.lazysodium.LazySodiumJava;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class VerferTest {

    LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    @Test
    @DisplayName("should verify digests with Ed25519")
    public void testVerifyDigests() throws SodiumException {

        final byte[] seed = lazySodium.randomBytesBuf(32);
        KeyPair keyPair = lazySodium.cryptoSignSeedKeypair(seed);

        final Key verkey = keyPair.getPublicKey();
        final Key sigkey = keyPair.getSecretKey();

        RawArgs rawArgs = RawArgs.builder()
                .raw(verkey.getAsBytes())
                .code(Codex.MatterCodex.Ed25519N.getValue())
                .build();

        Verfer verfer = new Verfer(rawArgs);
        assertNotEquals(verfer, null);
        assertArrayEquals(verfer.getRaw(), verkey.getAsBytes());
        assertEquals(verfer.getCode(), Codex.MatterCodex.Ed25519N.getValue());

        final String ser = "abcdefghijklmnopqrstuvwxyz0123456789";
        final String sigEncoded = lazySodium.cryptoSignDetached(ser, sigkey);
        final byte[] sig = new HexMessageEncoder().decode(sigEncoded);

        assertTrue(verfer.verify(sig, ser.getBytes()));

        verfer = new Verfer("BGgVB5Aar1pOr70nRpJmRA_RP68HErflNovoEMP7b7mJ");

        final byte[] expectedRaw = new byte[]{104, 21, 7, -112, 26, -81, 90, 78, -81, -67, 39, 70, -110, 102, 68, 15,
                -47, 63, -81, 7, 18, -73, -27, 54, -117, -24, 16, -61, -5, 111, -71, -119};
        assertArrayEquals(expectedRaw, verfer.getRaw());
    }
}
