package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SinglesigDRT {
    static SignifyClient delegator, delegate;
    static String name1_id, name1_oobi;
    static String contact1_id;

    @BeforeAll
    public static void getCreateClients() {
        try {
            List<SignifyClient> clients = TestUtils.getOrCreateClients(2, null);
            delegator = clients.get(0);
            delegate = clients.get(1);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void getIdentifier() {}

    @BeforeAll
    public static void getContact() {}
}
