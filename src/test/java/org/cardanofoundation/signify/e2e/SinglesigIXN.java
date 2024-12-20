package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SinglesigIXN extends TestUtils {
    static SignifyClient client1, client2;
    static String name1_id, name1_oobi;
    static String contact1_id;
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
}
