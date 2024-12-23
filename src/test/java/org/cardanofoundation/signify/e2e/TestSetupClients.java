package org.cardanofoundation.signify.e2e;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSetupClients extends TestUtils {
    private static SignifyClient client1, client2;
    private static String name1_id, name2_id;
    private static String name1_oobi, name2_oobi;
    private static String contact1_id, contact2_id;

    @BeforeAll
    public static void getClients() throws ExecutionException, InterruptedException {
        List<SignifyClient> clients = getOrCreateClients(2, null);
        client1 = clients.get(0);
        client2 = clients.get(1);
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients1 = getOrCreateIdentifier(client1, "name1");
        name1_id = clients1[0];
        name1_oobi = clients1[1];

        String[] clients2 = getOrCreateIdentifier(client2, "name2");
        name2_id = clients2[0];
        name2_oobi = clients2[1];
    }

    @BeforeEach
    public void getContact() throws SodiumException, IOException, InterruptedException {
        contact1_id = getOrCreateContact(client2, "contact1", name1_oobi);
        contact2_id = getOrCreateContact(client1, "contact2", name2_oobi);
    }

    @Test
    public void test_setup_clients_step1() {
        // Step 1
        assertEquals(name1_id, contact1_id);
        System.out.println("STEP 1 is Passed");
    }

    @Test
    public void test_setup_clients_step2() {
        // Step 2
        assertEquals(name2_id, contact2_id);
        System.out.println("STEP 2 is Passed");
    }
}
