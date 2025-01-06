package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils.AcceptMultisigInceptArgs;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils.StartMultisigInceptArgs;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.acceptMultisigIncept;
import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.startMultisigIncept;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class MultisigHolderTest extends BaseIntegrationTest {
    SignifyClient client1, client2, client3;
    States.HabState aid1, aid2, aid3;
    Object oobi1, oobi2, oobi3;
    String oobis1, oobis2, oobis3;
    private HashMap<String, Object> opResponseName;
    private List<HashMap<String, Object>> registryList, indentifierMap1, indentifierMap2;

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
    @DisplayName("Multisig Holder Test")
    void multisigHolderTest() throws Exception {
        // Boot four clients
        List<SignifyClient> signifyClients = getOrCreateClientsAsync(3);
        client1 = signifyClients.get(0);
        client2 = signifyClients.get(1);
        client3 = signifyClients.get(2);

        // Create four identifiers, one for each client
        aid1 = createAid(client1, "member1", WITNESS_AIDS);
        aid2 = createAid(client2, "member2", WITNESS_AIDS);
        aid3 = createAid(client3, "issuer", WITNESS_AIDS);

        createRegistry(client3, "issuer", "issuer-reg");

        // Exchange OOBIs
        System.out.println("Resolving OOBIs");
        oobi1 = client1.getOobis().get("member1", "agent");
        oobi2 = client2.getOobis().get("member2", "agent");
        oobi3 = client3.getOobis().get("issuer", "agent");

        oobis1 = getOobisIndexAt0(oobi1);
        oobis2 = getOobisIndexAt0(oobi2);
        oobis3 = getOobisIndexAt0(oobi3);

        Object op1 = client1.getOobis().resolve(oobis2, "member2");
        op1 = waitOperation(client1, op1);
        op1 = client1.getOobis().resolve(oobis3, "member3");
        op1 = waitOperation(client1, op1);
        op1 = client1.getOobis().resolve(SCHEMA_OOBI, "schema");
        op1 = waitOperation(client1, op1);
        System.out.println("Member1 resolved 3 OOBIs");

        Object op2 = client2.getOobis().resolve(oobis1, "member1");
        op2 = waitOperation(client2, op2);
        op2 = client2.getOobis().resolve(oobis3, "member3");
        op2 = waitOperation(client2, op2);
        op2 = client2.getOobis().resolve(SCHEMA_OOBI, "schema");
        op2 = waitOperation(client2, op2);
        System.out.println("Member2 resolved 3 OOBIs");

        Object op3 = client3.getOobis().resolve(oobis1, "member1");
        op3 = waitOperation(client3, op3);
        op3 = client3.getOobis().resolve(oobis2, "member2");
        op3 = waitOperation(client3, op3);
        op3 = client3.getOobis().resolve(SCHEMA_OOBI, "schema");
        op3 = waitOperation(client3, op3);
        System.out.println("Member3 resolved 3 OOBIs");

        // First member start the creation of a multisig identifier
        op1 = startMultisigIncept(client1, new StartMultisigInceptArgs(
                "holder",
                aid1.getName(),
                Arrays.asList(aid1.getPrefix(), aid2.getPrefix()),
                2,
                2,
                aid1.getState().getB().size(),
                aid1.getState().getB(),
                null
        ));
        System.out.println("Member1 initiated multisig, waiting for others to join...");

        // Second member check notifications and join the multisig
        String msgSaid = waitAndMarkNotification(client2, "/multisig/icp");
        System.out.println("Member2 received exchange message to join multisig");
        op2 = acceptMultisigIncept(client2, new AcceptMultisigInceptArgs(
                "holder",
                aid2.getName(),
                msgSaid
        ));
        System.out.println("Member2 joined multisig, waiting for others...");

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        System.out.println("Multisig created!");

        IdentifierListResponse identifiers1 = client1.getIdentifier().list();
        try {
            indentifierMap1 = objectMapper.readValue(
                    identifiers1.aids().toString(),
                    new TypeReference<>() {
                    });
            assertEquals(2, indentifierMap1.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        IdentifierListResponse identifiers2 = client1.getIdentifier().list();
        try {
            indentifierMap2 = objectMapper.readValue(
                    identifiers2.aids().toString(),
                    new TypeReference<>() {
                    });
            assertEquals(2, indentifierMap2.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.printf(
                "Client 1 managed AIDs:\n%s [%s]\n%s [%s]%n",
                Utils.toMap(indentifierMap1.get(0)).get("name"),
                Utils.toMap(indentifierMap1.get(0)).get("prefix"),
                Utils.toMap(indentifierMap1.get(1)).get("name"),
                Utils.toMap(indentifierMap1.get(1)).get("prefix")
        );

        System.out.printf(
                "Client 2 managed AIDs:\n%s [%s]\n%s [%s]%n",
                Utils.toMap(indentifierMap2.get(0)).get("name"),
                Utils.toMap(indentifierMap2.get(0)).get("prefix"),
                Utils.toMap(indentifierMap2.get(1)).get("name"),
                Utils.toMap(indentifierMap2.get(1)).get("prefix")
        );

        // Multisig end role
        // TO DO
        aid1 = client1.getIdentifier().get("member1");
        aid2 = client2.getIdentifier().get("member2");
        Object members = client1.getIdentifier().members("holder");
        States.HabState ghab1 = client1.getIdentifier().get("holder");
        List<Map<String, Object>> signing = (List<Map<String, Object>>) Utils.toMap(members).get("signing");
        String eid1 = Utils.toList(Utils.toMap(Utils.toMap(signing.getFirst().get("ends")).get("agent")).keySet()).getFirst();
        String eid2 = Utils.toList(Utils.toMap(Utils.toMap(signing.get(1).get("ends")).get("agent")).keySet()).getFirst();

        System.out.println("Starting multisig end role authorization for agent " + eid1);
    }

    public States.HabState createAid(SignifyClient client, String name, List<String> wits) throws Exception {
        getOrCreateIdentifier(client, name, null);
        States.HabState aid = client.getIdentifier().get(name);
        System.out.println(name + "AID:" + aid.getPrefix());
        return aid;
    }


    public Object createRegistry(SignifyClient client, String name, String registryName) throws Exception {
        CreateRegistryArgs args = CreateRegistryArgs.builder()
                .name(name)
                .registryName(registryName)
                .build();

        RegistryResult result = client.getRegistries().create(args);
        Object op = result.op();
        waitOperation(client, op);

        Object registries = client.getRegistries().list(name);
        try {
            if (registries instanceof String) {
                registryList = objectMapper.readValue((String) registries, new TypeReference<>() {});
            } else {
                registryList = objectMapper.convertValue(registries, new TypeReference<>() {});
            }

        } catch (Exception ex) {
           ex.printStackTrace();
        }
        opResponseName = registryList.getFirst();

        assertEquals(1, registryList.size());
        assertEquals(registryName, opResponseName.get("name"));
        return opResponseName;
    }

    public String getOobisIndexAt0(Object oobi) {
        Map<String, Object> oobiBody = (Map<String, Object>) oobi;
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }

}
