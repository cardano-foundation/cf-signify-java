package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.getOrCreateClients;

public class SetUpClientsTests {
    @Test
    public void TestSetUpClients() throws Exception {
    // create two clients with random secrets
        List<SignifyClient> clientList = TestUtils.getOrCreateClients(2, null);
        System.out.println("Client 1 is: " + clientList.getFirst() +
                "\n" +
                "Client 2 is: " + clientList.getLast());
    }
}
