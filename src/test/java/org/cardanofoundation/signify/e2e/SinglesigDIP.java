package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class SinglesigDIP extends TestUtils {
    private static SignifyClient client1, client2;
    private static String contact1_id;
    private static String name1_id, name1_oobi;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> clients = getOrCreateClients(2, null);
        client1 = clients.get(0);
        client2 = clients.get(1);
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients = getOrCreateIdentifier(client1, "name1", null);
        name1_id = clients[0];
        name1_oobi = clients[1];
    }

    @BeforeEach
    public void getContact() throws IOException, InterruptedException, LibsodiumException {
        contact1_id = getOrCreateContact(client2, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_dip() throws Exception {
        String opResponseName = null, opResponseI = null;

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setDelpre(name1_id);
        EventResult result = client2.identifiers().create("delegate1", kargs);
        Object op = result.op();
        States.HabState delegate1 = client2.identifiers().get("delegate1");
        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<>() {
                });
                opResponseName = opMap.get("name").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Assertions.assertEquals(opResponseName, "delegation." + delegate1.getPrefix());

        delegate1 = client2.identifiers().get("delegate1");
        Map<String, String> seal = new LinkedHashMap<>();
        seal.put("i", delegate1.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate1.getPrefix());

        result = client1.identifiers().interact("name1", seal);
        Object op1 = result.op();

        // Refresh keystate to sn=1
        Object op2 = client2.keyStates().query(name1_id, 1, null);

        op = operationToObject(waitOperation(client2, op));
        op1 = operationToObject(waitOperation(client1, op1));
        op2 = operationToObject(waitOperation(client2, op2));

        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<>() {
                });
                HashMap<String, Object> responseMap = (HashMap<String, Object>) opMap.get("response");
                opResponseI = responseMap.get("i").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        delegate1 = client2.identifiers().get("delegate1");
        Assertions.assertEquals(delegate1.getPrefix(), opResponseI);

        // Delegate creates identifier with default witness config
        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
        kargs.setDelpre(name1_id);
        kargs.setToad(env.witnessIds().size());
        kargs.setWits(env.witnessIds());
        result = client2.identifiers().create("delegate2", kargs);
        op = result.op();
        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, HashMap.class);
                opResponseName = opMap.get("name").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        States.HabState delegate2 = client2.identifiers().get("delegate2");
        Assertions.assertEquals(opResponseName, "delegation." + delegate2.getPrefix());

        // Delegator approves delegate
        delegate2 = client2.identifiers().get("delegate2");
        seal.put("i", delegate2.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate2.getPrefix());

        result = client1.identifiers().interact("name1", seal);
        op1 = result.op();

        // refresh keystate to seal event
        op2 = client2.keyStates().query(name1_id, null, seal);

        op = operationToObject(waitOperation(client2, op));
        op1 = operationToObject(waitOperation(client1, op1));
        op2 = operationToObject(waitOperation(client2, op2));

        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, HashMap.class);
                HashMap<String, Object> responseMap = (HashMap<String, Object>) opMap.get("response");
                opResponseI = responseMap.get("i").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Delegate waits for completion
        delegate2 = client2.identifiers().get("delegate2");
        Assertions.assertEquals(delegate2.getPrefix(), opResponseI);

        // Make sure query with seal is idempotent
        op = client2.keyStates().query(name1_id, null, seal);
        operationToObject(waitOperation(client2, op));
    }
}
