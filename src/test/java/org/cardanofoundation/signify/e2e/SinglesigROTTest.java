package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;

public class SinglesigROTTest extends BaseIntegrationTest {
    static SignifyClient client1, client2;
    static String contact1_id;
    static String name1_id, name1_oobi;
    private HashMap<String, Object> response;

    @BeforeAll
    public static void getClients() throws Exception {
        try {
            List<SignifyClient> clients = getOrCreateClientsAsync(2);
            client1 = clients.get(0);
            client2 = clients.get(1);
        } catch (ExecutionException | InterruptedException e) {
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
        contact1_id = getOrCreateContact(client2, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_rot_step1() throws Exception {
        assertEquals(name1_id, contact1_id);

        List<Map<String, Object>> keyState1List = (List<Map<String, Object>>) client1.keyStates().get(name1_id).get();
        assertEquals(1, keyState1List.size());

        List<Map<String, Object>> keyState2List = (List<Map<String, Object>>) client1.keyStates().get(contact1_id).get();
        assertEquals(1, keyState2List.size());

        // local and remote keystate sequence match
        assertEquals(keyState1List.getFirst().get("s"), keyState2List.getFirst().get("s"));
    }

    @Test
    public void singlesig_rot_rot1() throws Exception {
        // local keystate before rot
        List<Map<String, Object>> listKeyState0 = (List<Map<String, Object>>) client1.keyStates().get(name1_id).get();
        assertNotNull(listKeyState0);

        ArrayList<String> responseList = (ArrayList<String>) listKeyState0.getFirst().get("k");
        assertEquals(1, responseList.size());
        responseList.get(0);

        responseList = (ArrayList<String>) listKeyState0.getFirst().get("n");
        assertEquals(1, responseList.size());

        // rot
        RotateIdentifierArgs args = RotateIdentifierArgs.builder().build();
        EventResult result = client1.identifiers().rotate("name1", args);
        waitOperation(client1, result.op());

        // local keystate after rot
        List<Map<String, Object>> listKeyState1 = (List<Map<String, Object>>) client1.keyStates().get(name1_id).get();
        assertTrue(parseInteger(listKeyState1.getFirst().get("s").toString()) > 0);

        // sequence has incremented
        assertEquals(parseInteger(listKeyState1.getFirst().get("s").toString()),
                parseInteger(listKeyState0.getFirst().get("s").toString()) + 1
        );
        // current keys changed
        assertNotEquals(listKeyState1.getFirst().get("k"),
                listKeyState0.getFirst().get("k")
        );
        // next key hashes changed
        assertNotEquals(listKeyState1.getFirst().get("n"),
                listKeyState0.getFirst().get("n")
        );

        // remote keystate after rot
        List<Map<String, Object>> listKeyState2 = (List<Map<String, Object>>) client2.keyStates().get(contact1_id).get();
        assertEquals(parseInteger(listKeyState2.getFirst().get("s").toString()),
                parseInteger(listKeyState1.getFirst().get("s").toString()) - 1
        );

        // refresh remote keystate
        String sn = listKeyState1.getFirst().get("s").toString();
        Operation op = Operation.fromObject(client2.keyStates().query(contact1_id, sn, null));
        op = waitOperation(client2, op);
        response = (HashMap<String, Object>) op.getResponse();

        HashMap<String, Object> keyState3 = response;
        // local and remote keystate match
        assertEquals(keyState3.get("s"),
                listKeyState1.getFirst().get("s")
        );
        assertEquals(keyState3.get("k"),
                listKeyState1.getFirst().get("k")
        );
        assertEquals(keyState3.get("n"),
                listKeyState1.getFirst().get("n")
        );
    }
}
