package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.generated.keria.model.CompletedQueryOperation;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.Operation;
import org.cardanofoundation.signify.generated.keria.model.QueryOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;

public class SinglesigROTTest extends BaseIntegrationTest {
    static SignifyClient client1, client2;
    static String contact1_id;
    static String name1_id, name1_oobi;

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

        KeyStateRecord keyState1 = client1.keyStates().get(name1_id).get();
        KeyStateRecord keyState2 = client1.keyStates().get(contact1_id).get();

        // local and remote keystate sequence match
        assertEquals(keyState1.getS(), keyState2.getS());
    }

    @Test
    public void singlesig_rot_rot1() throws Exception {
        // local keystate before rot
        KeyStateRecord keyStateRecord0 = client1.keyStates().get(name1_id).get();

        List<String> responseList = keyStateRecord0.getK();
        assertEquals(1, responseList.size());
        responseList.get(0);

        responseList = keyStateRecord0.getN();
        assertEquals(1, responseList.size());

        // rot
        RotateIdentifierArgs args = RotateIdentifierArgs.builder().build();
        var result = client1.identifiers().rotate("name1", args);
        waitOperation(client1, result.op());

        // local keystate after rot
        KeyStateRecord keyStateRecord1 = client1.keyStates().get(name1_id).get();
        assertTrue(parseInteger(keyStateRecord1.getS()) > 0);

        // sequence has incremented
        assertEquals(parseInteger(keyStateRecord1.getS()),
                parseInteger(keyStateRecord0.getS()) + 1
        );
        // current keys changed
        assertNotEquals(keyStateRecord1.getK(), keyStateRecord0.getK());
        // next key hashes changed
        assertNotEquals(keyStateRecord1.getN(), keyStateRecord0.getN());

        // remote keystate after rot
        KeyStateRecord keyStateRecord2 = client2.keyStates().get(contact1_id).get();

        assertEquals(parseInteger(keyStateRecord2.getS()),
                parseInteger(keyStateRecord1.getS()) - 1
        );

        // refresh remote keystate
        String sn = keyStateRecord1.getS();
        QueryOperation queryOp = client2.keyStates().query(contact1_id, sn, null);
        Operation completedOp = waitOperation(client2, queryOp);
        KeyStateRecord keyState3 = ((CompletedQueryOperation) completedOp).getResponse();

        // local and remote keystate match
        assertEquals(keyState3.getS(), keyStateRecord1.getS());
        assertEquals(keyState3.getK(), keyStateRecord1.getK());
        assertEquals(keyState3.getN(), keyStateRecord1.getN());
    }
}
