package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;


public class SinglesigDIP {
    static SignifyClient client1, client2;
    static String contact1_id;
    static String name1_id;
    static String name1_oobi;

    @BeforeAll
    public static void getClients() {
        try {
            List<SignifyClient> clients = TestUtils.getOrCreateClients(2, null);
            client1 = clients.get(0);
            client2 = clients.get(1);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
//            e.printStackTrace();
        }
    }

    @BeforeAll
    public static void getIdentifier() {
        try {
            String[] clients = TestUtils.getOrCreateIdentifier(client1, "name1", null);
            name1_id = clients[0];
            name1_oobi = clients[1];
        } catch (ExecutionException | InterruptedException | SodiumException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    public static void getContact() {
    }

    @Test
    public void testSinglesigDIP() {
        System.out.println("test1: " + client1);
        System.out.println("test2: " + client2);
        System.out.println("/////////////////");
        System.out.println("Name 1: " + name1_id);
        System.out.println("Name 2: " + name1_oobi);
    }
}
