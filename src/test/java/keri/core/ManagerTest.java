package keri.core;

import org.junit.jupiter.api.Test;

import keri.core.Codex.MatterCodex;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class ManagerTest {

    @Test 
    void testRandyCreator() {
        Manager manager = new Manager();
        Manager.RandyCreator randy = manager.new RandyCreator();

        // test default arguments
        Manager.Keys keys = randy.create();
        assertEquals(1, keys.getSigners().size());
        assertEquals(44, keys.getSigners().get(0).getQb64().length());
        assertEquals(MatterCodex.Ed25519_Seed.getValue(), keys.getSigners().get(0).getCode());
        assertTrue(keys.getSigners().get(0).isTransferable());

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
}
