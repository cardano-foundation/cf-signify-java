package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class SinglesigIXNTest extends TestUtils {
    static SignifyClient client1, client2;
    static String name1_id, name1_oobi;
    static String contact1_id;
    private HashMap<String, Object> response;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void getClients() {
        try {
            List<SignifyClient> clients = getOrCreateClients(2, null);
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
    public void getContact() throws SodiumException, IOException, InterruptedException {
        contact1_id = getOrCreateContact(client2, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_ixn_step1() throws Exception {
        assertEquals(name1_id, contact1_id);

        Object keyState1 = client1.getKeyStates().get(name1_id);
        String resKeyState1 = objectMapper.writeValueAsString(keyState1);
        List<HashMap<String, Object>> keyState1List = objectMapper.readValue(
                resKeyState1, new TypeReference<>() {}
        );
        assertEquals(1, keyState1List.size());

        Object keyState2 = client2.getKeyStates().get(contact1_id);
        String resKeyState2 = objectMapper.writeValueAsString(keyState2);
        List<HashMap<String, Object>> keyState2List = objectMapper.readValue(
                resKeyState2, new TypeReference<>() {}
        );
        assertEquals(keyState2List.getFirst().get("s"), keyState1List.getFirst().get("s"));
    }

    @Test
    public void singlesig_ixn_ixn1() throws Exception {
        // local keystate before rot
        Object keyStates0 = client1.getKeyStates().get(name1_id);

        String respDataKeyState0 = objectMapper.writeValueAsString(keyStates0);
        List<Map<String, Object>> listKeyState0 = objectMapper.readValue(
                respDataKeyState0,
                new TypeReference<>() {
        });
        assertNotNull(listKeyState0);

        // ixn
        EventResult result = client1.getIdentifier().interact("name1", null);
        waitOperation(client1, result.op());

        // local keystate after rot
        Object keyState1 = client1.getKeyStates().get(name1_id);
        String respDataKeyState1 = objectMapper.writeValueAsString(keyState1);
        List<Map<String, Object>> listKeyState1 = objectMapper.readValue(
                respDataKeyState1,
                new TypeReference<>() {
        });
        assertTrue(parseInteger(listKeyState1.getFirst().get("s").toString()) > 0);

        // sequence has incremented
        assertEquals(parseInteger(listKeyState1.getFirst().get("s").toString()),
                parseInteger(listKeyState0.getFirst().get("s").toString()) + 1
        );

        // remote keystate after ixn
        Object keyState2 = client2.getKeyStates().get(contact1_id);
        String respDataKeyState2 = objectMapper.writeValueAsString(keyState2);
        List<Map<String, Object>> listKeyState2 = objectMapper.readValue(
                respDataKeyState2,
                new TypeReference<>() {
        });

        // remote keystate is one behind
        assertEquals(parseInteger(listKeyState2.getFirst().get("s").toString()),
                parseInteger(listKeyState1.getFirst().get("s").toString()) - 1
        );

        // refresh remote keystate
        String sn = listKeyState1.getFirst().get("s").toString();
        Object op = client2.getKeyStates().query(contact1_id, sn, null);
        op = operationToObject(waitOperation(client2, op));
        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<>() {
                });
                response = (HashMap<String, Object>) opMap.get("response");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        HashMap<String, Object> keyState3 = response;

        // local and remote keystate match
        assertEquals(keyState3.get("s"),
                listKeyState1.getFirst().get("s")
        );
    }
}
