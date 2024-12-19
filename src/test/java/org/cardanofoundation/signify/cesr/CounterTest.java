package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.Codex.CounterCodex;
import org.cardanofoundation.signify.cesr.args.CounterArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ShortageException;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CounterTest {

    @Test
    @DisplayName("should encode and decode stuff")
    public void shouldEncodeAndDecodeStuff() {  // int to b64 and back
        assertEquals(2, Counter.sizes.get("-A").hs); // hard size
        assertEquals(2, Counter.sizes.get("-A").ss); // soft size
        assertEquals(4, Counter.sizes.get("-A").fs); // full size
        assertEquals(0, Counter.sizes.get("-A").ls); // lead size

        // verify first hs Sizes matches hs in Codes for same first char
        for (Map.Entry<String, Matter.Sizage> entry : Counter.sizes.entrySet()) {
            String ckey = entry.getKey();
            String key = ckey.substring(0, 2);
            assertEquals(Counter.hards.get(key), Counter.sizes.get(ckey).hs);
        }

        // verify all Codes have hs > 0 and ss > 0 and fs = hs + ss and not fs % 4
        for (Matter.Sizage val : Counter.sizes.values()) {
            assertTrue(val.hs > 0 &&
                val.ss > 0 &&
                val.hs + val.ss == val.fs &&
                val.fs % 4 == 0);
        }

        // Bizes maps bytes of sextet of decoded first character of code with hard size of code
        // verify equivalents of items for Sizes and Bizes
        // Counter.Hards.forEach((sval, skey) => {
        //     let ckey = codeB64ToB2(skey)
        //     assert.equal(Counter.Bards[ckey], sval)
        // })

        // Test empty constructor
        assertThrows(EmptyMaterialException.class, () -> new Counter(CounterArgs.builder().build()));

        int count = 1;
        String qsc = CounterCodex.ControllerIdxSigs.getValue() + CoreUtil.intToB64(count, 2);
        assertEquals("-AAB", qsc);
        byte[] qscb = qsc.getBytes();

        // default count = 1
        Counter counter = new Counter(CounterArgs.builder()
            .code(CounterCodex.ControllerIdxSigs.getValue())
            .build());
        assertEquals(CounterCodex.ControllerIdxSigs.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        // default count = 1
        counter = new Counter(CounterArgs.builder()
            .qb64(qsc)
            .build()
        );
        assertEquals(CounterCodex.ControllerIdxSigs.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        // default count = 1
        counter = new Counter(CounterArgs.builder()
            .qb64b(qscb)
            .build());
        assertEquals(CounterCodex.ControllerIdxSigs.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        String longQs64 = qsc + "ABCD";
        counter = new Counter(CounterArgs.builder()
            .qb64(longQs64)
            .build());
        assertEquals(Counter.sizes.get(counter.getCode()).fs, counter.getQb64().length());

        String shortQcs = qsc.substring(0, qsc.length() - 1);
        assertThrows(ShortageException.class, () -> new Counter(
            CounterArgs.builder()
                .qb64(shortQcs)
                .build())
        );

        count = 5;
        qsc = CounterCodex.ControllerIdxSigs.getValue() + CoreUtil.intToB64(count, 2);
        assertEquals("-AAF", qsc);
        qscb = qsc.getBytes();

        counter = new Counter(CounterArgs.builder()
            .code(CounterCodex.ControllerIdxSigs.getValue())
            .count(count)
            .build());
        assertEquals(CounterCodex.ControllerIdxSigs.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        counter = new Counter(CounterArgs.builder()
            .qb64(qsc)
            .build());
        assertEquals(CounterCodex.ControllerIdxSigs.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        counter = new Counter(CounterArgs.builder()
            .qb64b(qscb)
            .build());
        assertEquals(CounterCodex.ControllerIdxSigs.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        // test with big codes index=1024
        count = 1024;
        qsc = CounterCodex.BigAttachedMaterialQuadlets.getValue() + CoreUtil.intToB64(count, 5);
        assertEquals("-0VAAAQA", qsc);
        qscb = qsc.getBytes();

        counter = new Counter(CounterArgs.builder()
            .code(CounterCodex.BigAttachedMaterialQuadlets.getValue())
            .count(count)
            .build());
        assertEquals(CounterCodex.BigAttachedMaterialQuadlets.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        counter = new Counter(CounterArgs.builder()
            .qb64(qsc)
            .build());
        assertEquals(CounterCodex.BigAttachedMaterialQuadlets.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        counter = new Counter(CounterArgs.builder()
            .qb64b(qscb)
            .build());
        assertEquals(CounterCodex.BigAttachedMaterialQuadlets.getValue(), counter.getCode());
        assertEquals(count, counter.getCount());
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        int verint = 0;
        String version = CoreUtil.intToB64(verint, 3);
        assertEquals("AAA", version);
        assertEquals(verint, CoreUtil.b64ToInt(version));
        qsc = CounterCodex.KERIProtocolStack.getValue() + version;
        assertEquals("--AAAAAA", qsc); // keri Cesr version 0.0.0
        qscb = qsc.getBytes();

        counter = new Counter(CounterArgs.builder()
            .code(CounterCodex.KERIProtocolStack.getValue())
            .count(verint)
            .build());
        assertEquals(CounterCodex.KERIProtocolStack.getValue(), counter.getCode());
        assertEquals(verint, counter.getCount());
        assertEquals(version, counter.countToB64(3));
        assertEquals(version, counter.countToB64()); // default length
        assertArrayEquals(qscb, counter.getQb64b());
        assertEquals(qsc, counter.getQb64());

        assertEquals("BCD", Counter.semVerToB64("1.2.3"));
        assertEquals("AAA", Counter.semVerToB64());
        assertEquals("BAA", Counter.semVerToB64("", 1));
        assertEquals("ABA", Counter.semVerToB64("", 0, 1));
        assertEquals("AAB", Counter.semVerToB64("", 0, 0, 1));
        assertEquals("DEF", Counter.semVerToB64("", 3, 4, 5));
        
        assertEquals("BBA", Counter.semVerToB64("1.1"));
        assertEquals("BAA", Counter.semVerToB64("1."));
        assertEquals("BAA", Counter.semVerToB64("1"));
        assertEquals("BCA", Counter.semVerToB64("1.2."));
        assertEquals("AAA", Counter.semVerToB64(".."));
        assertEquals("BAD", Counter.semVerToB64("1..3"));
        assertEquals("ECD", Counter.semVerToB64("4", 1, 2, 3));
    }
}
