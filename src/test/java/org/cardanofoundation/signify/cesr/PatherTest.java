package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class PatherTest {

    @Test
    @DisplayName("should path-ify stuff (and back again)")
    public void shouldPathifyStuff() {
        assertThrows(EmptyMaterialException.class, () -> new Pather(new RawArgs()));

        String[] path = new String[0];
        Pather pather = new Pather(new RawArgs(), null, path);
        assertEquals("-", pather.getBext());
        assertEquals("6AABAAA-", pather.getQb64());
        assertArrayEquals(">".getBytes(), pather.getRaw());
        assertArrayEquals(path, pather.getPath());

        path = new String[]{"a", "b", "c"};
        pather = new Pather(new RawArgs(), null, path);
        assertEquals("-a-b-c", pather.getBext());
        assertEquals("5AACAA-a-b-c", pather.getQb64());
        assertArrayEquals(
            new byte[]{15, (byte)154, (byte)249, (byte)191, (byte)156},
            pather.getRaw()
        );
        assertArrayEquals(path, pather.getPath());

        path = new String[]{"0", "1", "2"};
        pather = new Pather(new RawArgs(), null, path);
        assertEquals("-0-1-2", pather.getBext());
        assertEquals("5AACAA-0-1-2", pather.getQb64());
        assertArrayEquals(
            new byte[]{15, (byte)180, (byte)251, 95, (byte)182},
            pather.getRaw()
        );
        assertArrayEquals(path, pather.getPath());

        path = new String[]{"field0", "1", "0"};
        pather = new Pather(new RawArgs(), null, path);
        assertEquals("-field0-1-0", pather.getBext());
        assertEquals("4AADA-field0-1-0", pather.getQb64());
        assertArrayEquals(
            new byte[]{3, (byte)231, (byte)226, 122, 87, 116, (byte)251, 95, (byte)180},
            pather.getRaw()
        );
        assertArrayEquals(path, pather.getPath());

        String[] newPath = new String[]{"Not$Base64", "@moreso", "*again"};
        assertThrows(InvalidValueException.class, () -> new Pather(new RawArgs(), null, newPath));
    }
}
