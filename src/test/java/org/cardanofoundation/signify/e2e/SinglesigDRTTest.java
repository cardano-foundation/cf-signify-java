package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.app.util.HabStateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.getOrCreateIdentifier;

@SuppressWarnings("unchecked")
public class SinglesigDRTTest extends BaseIntegrationTest {
    private static SignifyClient delegator, delegate;
    private static String name1_id, name1_oobi;
    private String opResponseName, opResponseT, opResponseS;

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> clients = getOrCreateClientsAsync(2);
        delegator = clients.get(0);
        delegate = clients.get(1);
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients = getOrCreateIdentifier(delegator, "name1", null);
        name1_id = clients[0];
        name1_oobi = clients[1];
    }

    @BeforeEach
    public void getContact() throws IOException, InterruptedException, LibsodiumException {
        TestUtils.getOrCreateContact(delegate, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_drt() throws Exception {
        // delegate creates identifier without witnesses
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setDelpre(name1_id);

        EventResult result = delegate.identifiers().create("delegate1", kargs);
        Operation<?> op = Operation.fromObject(result.op());
        HabState delegate1 = delegate.identifiers().get("delegate1").get();
        opResponseName = op.getName();

        Assertions.assertEquals(opResponseName, "delegation." + HabStateUtil.getHabPrefix(delegate1));

        // delegator approves delegate
        Map<String, String> seal = new LinkedHashMap<>();
        seal.put("i", HabStateUtil.getHabPrefix(delegate1));
        seal.put("s", "0");
        seal.put("d", HabStateUtil.getHabPrefix(delegate1));

        result = delegator.identifiers().interact("name1", seal);
        Object op1 = result.op();
        Object op2 = delegate.keyStates().query(name1_id, "1", null);

        waitOperationAsync(
            new WaitOperationArgs(delegate, op),
            new WaitOperationArgs(delegator, op1),
            new WaitOperationArgs(delegate, op2)
        );

        RotateIdentifierArgs karg = RotateIdentifierArgs.builder().build();
        result = delegate.identifiers().rotate("delegate1", karg);
        op = Operation.fromObject(result.op());
        opResponseName = op.getName();

        Assertions.assertEquals(opResponseName, "delegation." + result.serder().getKed().get("d"));

        // delegator approves delegate
        delegate1 = delegate.identifiers().get("delegate1").get();
        seal = new LinkedHashMap<>();
        seal.put("i", HabStateUtil.getHabPrefix(delegate1));
        seal.put("s", "1");
        seal.put("d", HabStateUtil.getHabState(delegate1).getD());

        result = delegator.identifiers().interact("name1", seal);
        op1 = result.op();
        op2 = delegate.keyStates().query(name1_id, "2", null);

        List<Operation<?>> operationList = waitOperationAsync(
                new WaitOperationArgs(delegate, op),
                new WaitOperationArgs(delegator, op1),
                new WaitOperationArgs(delegate, op2)
        );

        op = operationList.getFirst();
        HashMap<String, String> opResponse = (HashMap<String, String>) op.getResponse();
        opResponseT = opResponse.get("t");
        opResponseS = opResponse.get("s");

        Assertions.assertEquals("drt", opResponseT);
        Assertions.assertEquals("1", opResponseS);
    }
}
