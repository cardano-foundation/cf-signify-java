package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;

import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.getOrCreateContact;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.getOrCreateIdentifier;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSetupClients extends BaseIntegrationTest {
    private static SignifyClient client1, client2;
    private static String name1_id, name2_id;
    private static String name1_oobi, name2_oobi;
    private static String contact1_id, contact2_id;

    @BeforeAll
    public static void getClients() throws Exception {
        // create two clients with random secrets
        List<SignifyClient> clients = getOrCreateClientsAsync(2);
        client1 = clients.get(0);
        client2 = clients.get(1);
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients1 = getOrCreateIdentifier(client1, "name1", null);
        name1_id = clients1[0];
        name1_oobi = clients1[1];

        String[] clients2 = getOrCreateIdentifier(client2, "name2", null);
        name2_id = clients2[0];
        name2_oobi = clients2[1];
    }

    @BeforeEach
    public void getContact() throws IOException, InterruptedException, LibsodiumException {
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
