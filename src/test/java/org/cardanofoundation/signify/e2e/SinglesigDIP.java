package org.cardanofoundation.signify.e2e;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class SinglesigDIP {
    static SignifyClient client1, client2;
    static String contact1_id;
    static String name1_id, name1_oobi;

    @BeforeAll
    public static void getClients() {
        try {
            List<SignifyClient> clients = TestUtils.getOrCreateClients(2, null);
            client1 = clients.get(0);
            client2 = clients.get(1);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void getIdentifier() {
        CompletableFuture<String[]> clients = TestUtils.getOrCreateIdentifiers(client1, "name1");
        try {
            String[] result = clients.get();
            System.out.println("ID: " + result[0]);
            System.out.println("OOBI: " + result[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @BeforeEach
    public  void getContact() {
        contact1_id = TestUtils.getOrCreateContact(client2, "contact1", name1_oobi).toString();
    }

    @Test
    public void testSinglesigDIP() {
        System.out.println("Client 1: " + client1);
        System.out.println("Client 2: " + client2);
        System.out.println("Contact: " + contact1_id);
    }

    @Test
    public void delegate1a() throws SodiumException, ExecutionException, InterruptedException {
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setDelpre(name1_id);
        EventResult result = client2.getIdentifier().create("delegate1", kargs);
        Map<String, Object> op = (Map<String, Object>) result.op();
        States.HabState delegate1 = client2.getIdentifier().get("delegate1");
        Assertions.assertEquals(op.get("name"), "delegation." + delegate1.getPrefix());
    }
}
