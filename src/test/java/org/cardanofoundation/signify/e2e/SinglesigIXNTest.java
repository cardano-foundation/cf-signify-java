package org.cardanofoundation.signify.e2e;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

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
    public void getContact() throws SodiumException, IOException, InterruptedException {
        contact1_id = TestUtils.getOrCreateContact(client2, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_ixn_step1() throws Exception {
        assertEquals(name1_id, contact1_id);

        List<HashMap<String, Object>> keyState1List = (List<HashMap<String, Object>>) client1.getKeyStates().get(name1_id);
        assertEquals(1, keyState1List.size());
        List<HashMap<String, Object>> keyState2List = (List<HashMap<String, Object>>) client2.getKeyStates().get(contact1_id);
        assertEquals(keyState2List.getFirst().get("s"), keyState1List.getFirst().get("s"));
    }

    @Test
    public void singlesig_ixn_ixn1() throws Exception {
        // local keystate before rot
        List<Map<String, Object>> listKeyState0 = (List<Map<String, Object>>) client1.getKeyStates().get(name1_id);
        assertNotNull(listKeyState0);

        // ixn
        EventResult result = client1.getIdentifier().interact("name1", null);
        waitOperation(client1, result.op());

        // local keystate after rot
        List<Map<String, Object>> listKeyState1 = (List<Map<String, Object>>) client1.getKeyStates().get(name1_id);
        assertTrue(parseInteger(listKeyState1.getFirst().get("s").toString()) > 0);

        // sequence has incremented
        assertEquals(parseInteger(listKeyState1.getFirst().get("s").toString()),
                parseInteger(listKeyState0.getFirst().get("s").toString()) + 1
        );

        // remote keystate after ixn
        List<Map<String, Object>> listKeyState2 = (List<Map<String, Object>>) client2.getKeyStates().get(contact1_id);
        // remote keystate is one behind
        assertEquals(parseInteger(listKeyState2.getFirst().get("s").toString()),
                parseInteger(listKeyState1.getFirst().get("s").toString()) - 1
        );

        // refresh remote keystate
        String sn = listKeyState1.getFirst().get("s").toString();
        Object op = client2.getKeyStates().query(contact1_id, sn, null);
        op = waitOperation(client2, op);

        response = (HashMap<String, Object>)  Operation.fromObject(op).getResponse();
        HashMap<String, Object> keyState3 = response;

        // local and remote keystate match
        assertEquals(keyState3.get("s"),
                listKeyState1.getFirst().get("s")
        );
    }
}
