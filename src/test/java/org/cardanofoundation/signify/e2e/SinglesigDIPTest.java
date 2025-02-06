package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.coring.Operation;
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

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;

class SinglesigDIPTest extends BaseIntegrationTest {
    private static SignifyClient client1, client2;
    private static String contact1_id;
    private static String name1_id, name1_oobi;

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> clients = getOrCreateClientsAsync(2);
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
        contact1_id = TestUtils.getOrCreateContact(client2, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_dip() throws Exception {
        String opResponseName = null, opResponseI = null;

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setDelpre(name1_id);
        EventResult result = client2.identifiers().create("delegate1", kargs);
        Operation op = Operation.fromObject(result.op());
        States.HabState delegate1 = client2.identifiers().get("delegate1");
        opResponseName = op.getName();
        Assertions.assertEquals(opResponseName, "delegation." + delegate1.getPrefix());

        delegate1 = client2.identifiers().get("delegate1");
        Map<String, String> seal = new LinkedHashMap<>();
        seal.put("i", delegate1.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate1.getPrefix());

        result = client1.identifiers().interact("name1", seal);
        Object op1 = result.op();

        // Refresh keystate to sn=1
        Object op2 = client2.keyStates().query(name1_id, "1", null);

        List<Operation> opList = waitOperationAsync(
                new WaitOperationArgs(client2, op),
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2)
        );
        op = opList.get(0);

        HashMap<String, Object> responseMap = (HashMap<String, Object>) op.getResponse();
        opResponseI = responseMap.get("i").toString();

        delegate1 = client2.identifiers().get("delegate1");
        Assertions.assertEquals(delegate1.getPrefix(), opResponseI);

        // Delegate creates identifier with default witness config
        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
        kargs.setDelpre(name1_id);
        kargs.setToad(env.witnessIds().size());
        kargs.setWits(env.witnessIds());
        result = client2.identifiers().create("delegate2", kargs);
        op = Operation.fromObject(result.op());
        opResponseName = op.getName();

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

        opList = waitOperationAsync(
                new WaitOperationArgs(client2, op),
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2)
        );
        op = opList.get(0);

        responseMap = (HashMap<String, Object>) op.getResponse();
        opResponseI = responseMap.get("i").toString();

        // Delegate waits for completion
        delegate2 = client2.identifiers().get("delegate2");
        Assertions.assertEquals(delegate2.getPrefix(), opResponseI);

        // Make sure query with seal is idempotent
        op = Operation.fromObject(client2.keyStates().query(name1_id, null, seal));
        waitOperation(client2, op);
    }
}
