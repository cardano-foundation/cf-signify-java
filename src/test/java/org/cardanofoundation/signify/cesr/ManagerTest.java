package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.SalterArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class ManagerTest {

    @Test
    @DisplayName("should create sets of random signers")
    void testRandyCreator() {
        Manager manager = new Manager();
        Manager.RandyCreator randy = manager.new RandyCreator();

        // test default arguments
        Manager.Keys keys = randy.create();
        assertEquals(1, keys.getSigners().size());
        assertEquals(44, keys.getSigners().getFirst().getQb64().length());
        assertEquals(MatterCodex.Ed25519_Seed.getValue(), keys.getSigners().getFirst().getCode());
        assertTrue(keys.getSigners().getFirst().isTransferable());

        // Create 5 with default code
        keys = randy.create(null, 5);
        assertEquals(5, keys.getSigners().size());
        keys.getSigners().forEach(signer -> {
            assertEquals(44, signer.getQb64().length());
            assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer.getCode());
            assertTrue(signer.isTransferable());
        });

        // Create 3 with specified codes (the only one we support)
        List<String> codes = Arrays.asList(
            MatterCodex.Ed25519_Seed.getValue(),
            MatterCodex.Ed25519_Seed.getValue(),
            MatterCodex.Ed25519_Seed.getValue()
        );
        keys = randy.create(codes);
        assertEquals(3, keys.getSigners().size());
        keys.getSigners().forEach(signer -> {
            assertEquals(44, signer.getQb64().length());
            assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer.getCode());
            assertTrue(signer.isTransferable());
        });
    }

    @Test
    @DisplayName("should create sets of salty signers")
    void testSaltyCreator() {
        Manager manager = new Manager();
        Manager.SaltyCreator salty = manager.new SaltyCreator();

        // Test default arguments
        assertEquals(salty.salter.getCode(), MatterCodex.Salt_128.getValue());
        assertEquals(salty.salt(), salty.salter.getQb64());
        assertEquals(salty.stem(), "");
        assertEquals(salty.tier(), salty.salter.getTier());

        var keys = salty.create();
        assertEquals(keys.getSigners().size(), 1);
        assertEquals(keys.getSigners().getFirst().getQb64().length(), 44);
        assertEquals(keys.getSigners().getFirst().getCode(), MatterCodex.Ed25519_Seed.getValue());
        assertTrue(keys.getSigners().getFirst().isTransferable());

        keys = salty.create(null, 2, MatterCodex.Ed25519_Seed.getValue(), false, 0, 0, 0, false);
        assertEquals(keys.getSigners().size(), 2);
        keys.getSigners().forEach(signer -> {
            assertEquals(44, signer.getQb64().length());
            assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer.getCode());
            assertEquals(MatterCodex.Ed25519N.getValue(), signer.getVerfer().getCode());
        });

        final String raw = "0123456789abcdef";
        final Salter salter = new Salter(SalterArgs.builder().raw(raw.getBytes()).build());
        final String salt = salter.getQb64();
        assertEquals(salter.getQb64(), "0AAwMTIzNDU2Nzg5YWJjZGVm");
        salty = manager.new SaltyCreator(salt, null, null);
        assertEquals(salty.salter.getCode(), MatterCodex.Salt_128.getValue());
        assertArrayEquals(salty.salter.getRaw(), raw.getBytes());
        assertEquals(salty.salter.getQb64(), salt);
        assertEquals(salty.salt(), salty.salter.getQb64());
        assertEquals(salty.stem(), "");
        assertEquals(salty.tier(), salty.salter.getTier());

        keys = salty.create();
        assertEquals(keys.getSigners().size(), 1);
        assertEquals(keys.getSigners().getFirst().getCode(), MatterCodex.Ed25519_Seed.getValue());
        assertEquals(keys.getSigners().getFirst().getQb64(), "AO0hmkIVsjCoJY1oUe3-QqHlMBVIhFX1tQfN_8SPKiNF");
        assertEquals(keys.getSigners().getFirst().getVerfer().getCode(), MatterCodex.Ed25519.getValue());
        assertEquals(keys.getSigners().getFirst().getVerfer().getQb64(), "DHHneREQ1eZyQNc5nEsQYx1FqFVL1OTXmvmatTE77Cfe");

        keys = salty.create(null, 1, MatterCodex.Ed25519_Seed.getValue(), false, 0, 0, 0, true);
        assertEquals(keys.getSigners().size(), 1);
        assertEquals(keys.getSigners().getFirst().getCode(), MatterCodex.Ed25519_Seed.getValue());
        assertEquals(keys.getSigners().getFirst().getQb64(), "AOVkNmL_dZ5pjvp-_nS5EJbs0xe32MODcOUOym-0aCBL");
        assertEquals(keys.getSigners().getFirst().getVerfer().getCode(), MatterCodex.Ed25519N.getValue());
        assertEquals(keys.getSigners().getFirst().getVerfer().getQb64(), "BB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
    }

    @Test
    @DisplayName("should create Randy or Salty creator")
    void testManagerCreator() {
        Manager manager = new Manager();

        final String raw = "0123456789abcdef";
        final Salter salter = new Salter(SalterArgs.builder().raw(raw.getBytes()).build());
        final String salt = salter.getQb64();

        Manager.Creator creator = manager.new Creatory(Manager.Algos.salty).make(salt);
        assertInstanceOf(Manager.SaltyCreator.class, creator);
        assertEquals(((Manager.SaltyCreator) creator).salter.getQb64(), salt);

        creator = manager.new Creatory().make(salt);
        assertInstanceOf(Manager.SaltyCreator.class, creator);
        assertEquals(((Manager.SaltyCreator) creator).salter.getQb64(), salt);

        creator = manager.new Creatory(Manager.Algos.randy).make(salt);
        assertInstanceOf(Manager.RandyCreator.class, creator);
    }
}
