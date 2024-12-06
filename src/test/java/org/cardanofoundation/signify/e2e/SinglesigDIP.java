package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class SinglesigDIP {
    static SignifyClient client1, client2;
    static String contact1_id;
    static String name1_id, name1_oobi;

    @BeforeAll
    public static void getClients() {
        try {
            List<SignifyClient> clients = TestUtils.getOrCreateClients(2, null);
            client1 = clients.get(0);
            client2 = clients.get(1);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients = TestUtils.getOrCreateIdentifier(client1, "name1");
        try {
//            String[] result = clients.get();
            name1_id = clients[0];
            name1_oobi = clients[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @BeforeEach
    public  void getContact() throws SodiumException, JsonProcessingException, InterruptedException {
//        contact1_id = TestUtils.getOrCreateContact(client2, "contact1", name1_oobi);
    }

    @Test
    public void testSinglesigDIP() {
        System.out.println("Client 1: " + client1);
        System.out.println("Client 2: " + client2);
        System.out.println("Contact: " + contact1_id);
    }

//    @Test
    public void delegate1a() throws Exception {
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setDelpre(name1_id);
        EventResult result = client2.getIdentifier().create("delegate1", kargs);
        Map<String, Object> op = (Map<String, Object>) result.op();
        States.HabState delegate1 = client2.getIdentifier().get("delegate1");
        Assertions.assertEquals(op.get("name"), "delegation." + delegate1.getPrefix());

        delegate1 = client2.getIdentifier().get("delegate1");

        Map<String, String> seal = new HashMap<>();
        seal.put("i", delegate1.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate1.getPrefix());

        result = client1.getIdentifier().interact("name1", seal);
        Map<String, Object> op1 = (Map<String, Object>) result.op();

        // Refresh keystate to sn=1
        Object op2 = client2.getKeyStates().query(name1_id, 1, null);

        CompletableFuture<Operation<Object>> opFuture = TestUtils.waitOperation(client2, op);
        CompletableFuture<Operation<Object>> op1Future = TestUtils.waitOperation(client1, op1);
        CompletableFuture<Operation<Object>> op2Future = TestUtils.waitOperation(client2, op2);
        CompletableFuture<Void> allOfOb = CompletableFuture.allOf(opFuture, op1Future, op2Future);
        allOfOb.get();

        delegate1 = client2.getIdentifier().get("delegate1");
        Assertions.assertEquals(delegate1.getPrefix(), op.get("i"));

        // Delegate creates identifier with default witness config
        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
        kargs.setDelpre(name1_id);
        kargs.setToad(env.witnessIds().size());
        kargs.setWits(env.witnessIds());
        result = client2.getIdentifier().create("delegate2", kargs);
        op = (Map<String, Object>) result.op();
        States.HabState delegate2 = client2.getIdentifier().get("delegate2");
        Assertions.assertEquals(op.get("name"), "delegation." + delegate2.getPrefix());

        // Delegator approves delegate
        delegate2 = client2.getIdentifier().get("delegate2");
        seal.put("i", delegate2.getPrefix());
        seal.put("s", "0");
        seal.put("d", delegate2.getPrefix());

        result = client1.getIdentifier().interact("name1", seal);
        op1 = (Map<String, Object>) result.op();

        // refresh keystate to seal event
        op2 = client2.getKeyStates().query(name1_id, null, seal.toString());

        CompletableFuture<Operation<Object>> op_Future = TestUtils.waitOperation(client2, op);
        CompletableFuture<Operation<Object>> op1_1Future = TestUtils.waitOperation(client1, op1);
        CompletableFuture<Operation<Object>> op2_1Future = TestUtils.waitOperation(client2, op2);
        CompletableFuture<Void> allOfOb1 = CompletableFuture.allOf(op_Future, op1_1Future, op2_1Future);
        allOfOb1.get();

        // Delegate waits for completion
        delegate2 = client2.getIdentifier().get("delegate2");
        Assertions.assertEquals(delegate2.getPrefix(), op.get("i"));

        // Make sure query with seal is idempotent
        String ops = (String) client2.getKeyStates().query(name1_id, null, seal.toString());
        TestUtils.waitOperation(client2, ops);
    }
}
