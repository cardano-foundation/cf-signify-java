package keri.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoringTest {

    @Test
    public void testRandomPasscode() {
        final String passcode = Coring.randomPasscode();
        assertEquals(passcode.length(), 21);
    }
}
