package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class SinglesigDIP {
    static SignifyClient client1, client2;
    static String contact1_id;

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
    public static void getIdentifier() {}

    @BeforeAll
    public static void getContact() {}

    @Test
    public void testSinglesigDIP() {
        System.out.println("test1: " + client1);
        System.out.println("test2: " + client2);
    }
}
