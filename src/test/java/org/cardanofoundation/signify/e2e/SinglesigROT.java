package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.KeyStates;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class SinglesigROT extends TestUtils {
    static SignifyClient client1, client2;
    static String contact1_id;
    static String name1_id, name1_oobi;
    private HashMap<String, Object> response;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void getClients() throws Exception {
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
        String[] clients = getOrCreateIdentifier(client1, "name1");
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
    public void singlesig_rot_step1() throws Exception {
        assertEquals(name1_id, contact1_id);

        Object keyState1 = client1.getKeyStates().get(name1_id);
        String respDataKeyState1 = objectMapper.writeValueAsString(keyState1);
        List<Map<String, Object>> keyState1List = objectMapper.readValue(respDataKeyState1, new TypeReference<>() {
        });
        assertEquals(1, keyState1List.size());

        Object ketState2 = client2.getKeyStates().get(contact1_id);
        String respDataKeyState2 = objectMapper.writeValueAsString(ketState2);
        List<Map<String, Object>> keyState2List = objectMapper.readValue(respDataKeyState2, new TypeReference<>() {
        });
        assertEquals(1, keyState2List.size());

        // local and remote keystate sequence match
        assertEquals(keyState1List.getFirst().get("s"), keyState2List.getFirst().get("s"));
    }

    @Test
    public void singlesig_rot_rot1() throws Exception {
        // local keystate before rot
        Object keyStates0 = client1.getKeyStates().get(name1_id);

        String respDataKeyState0 = objectMapper.writeValueAsString(keyStates0);
        List<Map<String, Object>> listKeyState0 = objectMapper.readValue(respDataKeyState0, new TypeReference<>() {
        });
        assertNotNull(listKeyState0);

        ArrayList<String> responseList = (ArrayList<String>) listKeyState0.getFirst().get("k");
        assertEquals(1, responseList.size());
        responseList.get(0);

        responseList = (ArrayList<String>) listKeyState0.getFirst().get("n");
        assertEquals(1, responseList.size());

        // rot
        RotateIdentifierArgs args = RotateIdentifierArgs.builder().build();
        EventResult result = client1.getIdentifier().rotate("name1", args);
        waitOperation(client1, result.op());

        // local keystate after rot
        Object keyState1 = client1.getKeyStates().get(name1_id);
        String respDataKeyState1 = objectMapper.writeValueAsString(keyState1);
        List<Map<String, Object>> listKeyState1 = objectMapper.readValue(respDataKeyState1, new TypeReference<>() {
        });
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
        Object keyState2 = client2.getKeyStates().get(contact1_id);
        String respDataKeyState2 = objectMapper.writeValueAsString(keyState2);
        List<Map<String, Object>> listKeyState2 = objectMapper.readValue(respDataKeyState2, new TypeReference<>() {
        });

        assertEquals(parseInteger(listKeyState2.getFirst().get("s").toString()),
                parseInteger(listKeyState1.getFirst().get("s").toString()) - 1
        );

        // refresh remote keystate
        int sn = parseInteger(listKeyState1.getFirst().get("s").toString());
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
        assertEquals(keyState3.get("k"),
                listKeyState1.getFirst().get("k")
        );
        assertEquals(keyState3.get("n"),
                listKeyState1.getFirst().get("n")
        );
    }
}
