package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class SinglesigIXNTest extends BaseIntegrationTest {
    static SignifyClient client1, client2;
    static String name1_id, name1_oobi;
    static String contact1_id;
    private HashMap<String, Object> response;

    @BeforeAll
    public static void getClients() {
        try {
            List<SignifyClient> clients = getOrCreateClientsAsync(2);
            client1 = clients.get(0);
            client2 = clients.get(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients = getOrCreateIdentifier(client1, "name1", null);
        try {
            name1_id = clients[0];
            name1_oobi = clients[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void getContact() throws IOException, InterruptedException, LibsodiumException {
        contact1_id = TestUtils.getOrCreateContact(client2, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_ixn_step1() throws Exception {
        assertEquals(name1_id, contact1_id);

        KeyStateRecord keyState1 = client1.keyStates().get(name1_id).get();
        KeyStateRecord keyState2  = client1.keyStates().get(contact1_id).get();
        assertEquals(keyState1.getS(), keyState2.getS());
    }

    @Test
    public void singlesig_ixn_ixn1() throws Exception {
        // local keystate before rot
        KeyStateRecord keyStateRecord0 = client1.keyStates().get(name1_id).get();
        assertNotNull(keyStateRecord0);

        // ixn
        EventResult result = client1.identifiers().interact("name1", null);
        waitOperation(client1, result.op());

        // local keystate after rot
        KeyStateRecord keyStateRecord1 = client1.keyStates().get(name1_id).get();

        assertTrue(parseInteger(keyStateRecord1.getS()) > 0);

        // sequence has incremented
        assertEquals(parseInteger(keyStateRecord1.getS()),
                parseInteger(keyStateRecord0.getS()) + 1
        );

        // remote keystate after ixn
        KeyStateRecord keyStateRecord2 = client2.keyStates().get(contact1_id).get();
        // remote keystate is one behind
        assertEquals(parseInteger(keyStateRecord2.getS()),
                parseInteger(keyStateRecord1.getS()) - 1
        );

        // refresh remote keystate
        String sn = keyStateRecord1.getS();
        Object op = client2.keyStates().query(contact1_id, sn, null);
        op = waitOperation(client2, op);

        response = (HashMap<String, Object>)  Operation.fromObject(op).getResponse();
        HashMap<String, Object> keyState3 = response;

        // local and remote keystate match
        assertEquals(keyState3.get("s"),
                keyStateRecord1.getS());
    }
}
