package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SinglesigDRT extends TestUtils {
    static SignifyClient delegator, delegate;
    static String name1_id, name1_oobi;
    static String contact1_id;
    String opResponseName, opResponseT, opResponseS;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void getClients() {
        try {
            List<SignifyClient> clients = getOrCreateClients(2, null);
            delegator = clients.get(0);
            delegate = clients.get(1);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients = getOrCreateIdentifier(delegator, "name1");
        try {
            name1_id = clients[0];
            name1_oobi = clients[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void getContact() throws SodiumException, JsonProcessingException, InterruptedException {
        contact1_id = getOrCreateContact(delegate, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_drt() throws Exception {
        // delegate creates identifier without witnesses
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setDelpre(name1_id);

        EventResult result = delegate.getIdentifier().create("delegate1", kargs);
        Object op = result.op();
        States.HabState delegate1 = delegate.getIdentifier().get("delegate1");
        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<HashMap<String, Object>>() {});
                opResponseName = opMap.get("name").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Assertions.assertEquals(opResponseName, "delegation." + delegate1.getPrefix());

        // delegator approves delegate
        Map<String, String> seal = new HashMap<>();
        seal.put("i", delegate1.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate1.getPrefix());

        result = delegator.getIdentifier().interact("name1", seal);
        Object op1 = result.op();
        Object op2 = delegate.getKeyStates().query(name1_id, 1, null);

        operationToObject(waitOperation(delegate, op));
        operationToObject(waitOperation(delegator, op1));
        operationToObject(waitOperation(delegate, op2));

        // TO-DO .rotate()
//        result = delegate.getIdentifier().rotate();
        op = result.op();
        Assertions.assertEquals(opResponseName, "delegation." + result.serder().getKed().get("d"));

        // delegator approves delegate
        delegate1 = delegate.getIdentifier().get("delegate1");
        seal = new HashMap<>();
        seal.put("i", delegate1.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate1.getPrefix());

        result = delegator.getIdentifier().interact("name1", seal);
        op1 = result.op();
        op2 = delegate.getKeyStates().query(name1_id, 2, null);

        op = operationToObject(waitOperation(delegate, op));
        operationToObject(waitOperation(delegator, op1));
        operationToObject(waitOperation(delegate, op2));

        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<HashMap<String, Object>>() {});
                HashMap <String, String> opResponse = (HashMap<String, String>) opMap.get("response");
                opResponseT = opResponse.get("t");
                opResponseS = opResponse.get("s");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Assertions.assertEquals("drt", opResponseT);
        Assertions.assertEquals("1", opResponseS);
    }
}
