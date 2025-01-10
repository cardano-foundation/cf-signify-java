package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BexterTest {

    @Test
    @DisplayName("should bext-ify stuff (and back again)")
    public void shouldBextifyStuff() {
        assertThrows(EmptyMaterialException.class, () -> new Bexter(new RawArgs()));

        String invalidBext = "@!";
        assertThrows(InvalidValueException.class, () -> new Bexter(new RawArgs(), invalidBext));

        String bext = "";
        Bexter bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAA", bexter.getBoth());
        assertArrayEquals("".getBytes(), bexter.getRaw());
        assertEquals("4AAA", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "-";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L2.getValue(), bexter.getCode());
        assertEquals("6AAB", bexter.getBoth());
        assertArrayEquals(">".getBytes(), bexter.getRaw());
        assertEquals("6AABAAA-", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "-A";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L1.getValue(), bexter.getCode());
        assertEquals("5AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{15, (byte)128}, bexter.getRaw());
        assertEquals("5AABAA-A", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "-A-";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{3, (byte)224, 62}, bexter.getRaw());
        assertEquals("4AABA-A-", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "-A-B";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{(byte)248, 15, (byte)129}, bexter.getRaw());
        assertEquals("4AAB-A-B", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "A";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L2.getValue(), bexter.getCode());
        assertEquals("6AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{0}, bexter.getRaw());
        assertEquals("6AABAAAA", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "AA";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L1.getValue(), bexter.getCode());
        assertEquals("5AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{0, 0}, bexter.getRaw());
        assertEquals("5AABAAAA", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "AAA";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{0, 0, 0}, bexter.getRaw());
        assertEquals("4AABAAAA", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "AAAA";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{0, 0, 0}, bexter.getRaw());
        assertEquals("4AABAAAA", bexter.getQb64());
        assertEquals("AAA", bexter.getBext());
        assertNotEquals(bext, bexter.getBext());

        bext = "ABB";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{0, 0, 65}, bexter.getRaw());
        assertEquals("4AABAABB", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "BBB";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{0, 16, 65}, bexter.getRaw());
        assertEquals("4AABABBB", bexter.getQb64());
        assertEquals(bext, bexter.getBext());

        bext = "ABBB";
        bexter = new Bexter(new RawArgs(), bext);
        assertEquals(MatterCodex.StrB64_L0.getValue(), bexter.getCode());
        assertEquals("4AAB", bexter.getBoth());
        assertArrayEquals(new byte[]{0, 16, 65}, bexter.getRaw());
        assertEquals("4AABABBB", bexter.getQb64());
        assertEquals("BBB", bexter.getBext());
        assertNotEquals(bext, bexter.getBext());
    }
}
