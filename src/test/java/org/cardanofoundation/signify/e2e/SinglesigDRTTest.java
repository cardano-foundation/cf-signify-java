package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.CompletedDelegationOperation;
import org.cardanofoundation.signify.generated.keria.model.CompletedDelegationOperationResponse;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.Operation;
import org.cardanofoundation.signify.generated.keria.model.QueryOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.getOrCreateIdentifier;

public class SinglesigDRTTest extends BaseIntegrationTest {
    private static SignifyClient delegator, delegate;
    private static String name1_id, name1_oobi;
    private static String contact1_id;
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
        contact1_id = TestUtils.getOrCreateContact(delegate, "contact1", name1_oobi);
    }

    @Test
    public void singlesig_drt() throws Exception {
        // delegate creates identifier without witnesses
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setDelpre(name1_id);

        var result = delegate.identifiers().create("delegate1", kargs);
        Operation op = result.op();
        HabState delegate1 = delegate.identifiers().get("delegate1").get();
        opResponseName = op.getName();

        Assertions.assertEquals(opResponseName, "delegation." + delegate1.getPrefix());

        // delegator approves delegate
        Map<String, String> seal = new LinkedHashMap<>();
        seal.put("i", delegate1.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate1.getPrefix());

        var interactResult1 = delegator.identifiers().interact("name1", seal);
        Operation op1 = interactResult1.op();
        QueryOperation op2 = delegate.keyStates().query(name1_id, "1", null);

        waitOperationAsync(
            new WaitOperationArgs(delegate, op),
            new WaitOperationArgs(delegator, op1),
            new WaitOperationArgs(delegate, op2)
        );

        RotateIdentifierArgs karg = RotateIdentifierArgs.builder().build();
        var rotResult = delegate.identifiers().rotate("delegate1", karg);
        op = rotResult.op();
        opResponseName = op.getName();

        Assertions.assertEquals(opResponseName, "delegation." + rotResult.serder().getKed().get("d"));

        // delegator approves delegate
        delegate1 = delegate.identifiers().get("delegate1").get();
        seal = new LinkedHashMap<>();
        seal.put("i", delegate1.getPrefix());
        seal.put("s", "1");
        seal.put("d", delegate1.getState().getD());

        var interactResult2 = delegator.identifiers().interact("name1", seal);
        op1 = interactResult2.op();
        op2 = delegate.keyStates().query(name1_id, "2", null);

        List<Operation> operationList = waitOperationAsync(
                new WaitOperationArgs(delegate, op),
                new WaitOperationArgs(delegator, op1),
                new WaitOperationArgs(delegate, op2)
        );

        op = operationList.getFirst();
        CompletedDelegationOperationResponse opResponse = ((CompletedDelegationOperation) op).getResponse();
        opResponseT = opResponse.getT();
        opResponseS = opResponse.getS();

        Assertions.assertEquals("drt", opResponseT);
        Assertions.assertEquals("1", opResponseS);
    }
}
