package org.cardanofoundation.signify.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CoringTest {

    @Test
    public void testRandomPasscode() {
        final String passcode = Coring.randomPasscode();
        assertEquals(passcode.length(), 21);

        final String passcode2 = Coring.randomPasscode();
        assertEquals(passcode2.length(), 21);

        // passcode should be unique
        assertNotEquals(passcode, passcode2);
    }

    @Test
    public void testRandomNonce() {
        final String nonce = Coring.randomNonce();
        assertEquals(nonce.length(), 44);

        final String nonce2 = Coring.randomNonce();
        assertEquals(nonce2.length(), 44);

        // nonce should be unique
        assertNotEquals(nonce, nonce2);
    }
}
