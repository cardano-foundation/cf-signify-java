package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

public class MultisigTest extends TestUtils {
    SignifyClient client1, client2, client3, client4;
    States.HabState aid1, aid2, aid3, aid4;
    Object oobi1, oobi2, oobi3, oobi4;
    String oobis1, oobis2, oobis3, oobis4;

    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    ArrayList<String> WITNESS_AIDS = new ArrayList<>(Arrays.asList(
            "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
            "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
            "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
    ));
    String SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    String SCHEMA_OOBI = env.vleiServerUrl() + "/oobi/" + SCHEMA_SAID;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void multisig() throws Exception {
        // Boot Four clients
        client1 = getOrCreateClient(null);
        client2 = getOrCreateClient(null);
        client3 = getOrCreateClient(null);
        client4 = getOrCreateClient(null);

        // Create four identifiers, one for each client
        aid1 = createAid(client1, "member1", WITNESS_AIDS);
        aid2 = createAid(client2, "member2", WITNESS_AIDS);
        aid3 = createAid(client3, "member3", WITNESS_AIDS);
        aid4 = createAid(client4, "holder", WITNESS_AIDS);

        // Exchange OOBIs
        System.out.println("Resolving OOBIs");
        oobi1 = client1.getOobis().get("member1", "agent");
        oobi2 = client2.getOobis().get("member2", "agent");
        oobi3 = client3.getOobis().get("member3", "agent");
        oobi4 = client4.getOobis().get("holder", "agent");

        oobis1 = getOobisIndexAt0(oobi1);
        oobis2 = getOobisIndexAt0(oobi2);
        oobis3 = getOobisIndexAt0(oobi3);
        oobis4 = getOobisIndexAt0(oobi4);

        Object op1 = client1.getOobis().resolve(oobis2, "member2");
        op1 = waitOperation(client1, op1);
        op1 = client1.getOobis().resolve(oobis3, "member3");
        op1 = waitOperation(client1, op1);
        op1 = client1.getOobis().resolve(SCHEMA_OOBI, "schema");
        op1 = waitOperation(client1, op1);
        op1 = client1.getOobis().resolve(oobis4, "holder");
        op1 = waitOperation(client1, op1);
        System.out.println("Member1 resolved 4 OOBIs");

        Object op2 = client2.getOobis().resolve(oobis1, "member1");
        op2 = waitOperation(client2, op2);
        op2 = client2.getOobis().resolve(oobis3, "member3");
        op2 = waitOperation(client2, op2);
        op2 = client2.getOobis().resolve(SCHEMA_OOBI, "schema");
        op2 = waitOperation(client2, op2);
        op2 = client2.getOobis().resolve(oobis4, "holder");
        op2 = waitOperation(client2, op2);
        System.out.println("Member2 resolved 4 OOBIs");

        Object op3 = client3.getOobis().resolve(oobis1, "member1");
        op3 = waitOperation(client3, op3);
        op3 = client3.getOobis().resolve(oobis2, "member2");
        op3 = waitOperation(client3, op3);
        op3 = client3.getOobis().resolve(SCHEMA_OOBI, "schema");
        op3 = waitOperation(client3, op3);
        op3 = client3.getOobis().resolve(oobis4, "holder");
        op3 = waitOperation(client3, op3);
        System.out.println("Member3 resolved 4 OOBIs");

        Object op4 = client4.getOobis().resolve(oobis1, "member1");
        op4 = waitOperation(client4, op4);
        op4 = client4.getOobis().resolve(oobis2, "member2");
        op4 = waitOperation(client4, op4);
        op4 = client4.getOobis().resolve(oobis3, "member3");
        op4 = waitOperation(client4, op4);
        op4 = client4.getOobis().resolve(SCHEMA_OOBI, "schema");
        op4 = waitOperation(client4, op4);
        System.out.println("Member4 resolved 4 OOBIs");

        // TO-DO: miss challenges().generate() function
    }

    public States.HabState createAid(SignifyClient client, String name, List<String> wits) throws Exception {
        CreateIdentifierArgs iargs = new CreateIdentifierArgs();
        iargs.setWits(wits);
        iargs.setToad(wits.size());
        getOrCreateIdentifier(client, name, iargs);
        return client.getIdentifier().get(name);
    }

    public String getOobisIndexAt0(Object oobi) throws Exception {
        Map<String, Object> oobiBody = (Map<String, Object>) oobi;
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }
}
