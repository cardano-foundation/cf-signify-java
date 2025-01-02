package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.e2e.utils.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ChallengesTest extends TestUtils {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private static SignifyClient client1, client2;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HashMap<String, Object> opResponse1, opResponse2;

    @Test
    void ChallengeTest() throws Exception {
        String bran1 = Coring.randomPasscode();
        String bran2 = Coring.randomPasscode();
        client1 = new SignifyClient(
                url,
                bran1,
                Salter.Tier.low,
                bootUrl,
                null
        );
        client2 = new SignifyClient(
                url,
                bran2,
                Salter.Tier.low,
                bootUrl,
                null
        );
        client1.boot();
        client2.boot();
        client1.connect();
        client2.connect();
        client1.state();
        client2.state();

        // Generate challenge words
        // TO DO

        CreateIdentifierArgs kargs1 = new CreateIdentifierArgs();
        kargs1.setToad(3);
        kargs1.setWits(List.of(
                "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
        ));
        EventResult icpResult1 = client1.getIdentifier().create("alice", kargs1);
        Object op1 = operationToObject(waitOperation(client1, icpResult1.op()));
        if (op1 instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op1, new TypeReference<>() {
                });
                opResponse1 = (HashMap<String, Object>) opMap.get("response");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        EventResult rpyResult1 = client1.getIdentifier().addEndRole(
                "alice",
                "agent",
                client1.getAgent().getPre(),
                null);
        waitOperation(client1, rpyResult1.op());
        System.out.println("Alice's AID: " + opResponse1.get("i"));

        CreateIdentifierArgs kargs2 = new CreateIdentifierArgs();
        kargs2.setToad(3);
        kargs2.setWits(List.of(
                "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
        ));
        EventResult icpResult2 = client2.getIdentifier().create("bob", kargs2);
        Object op2 = operationToObject(waitOperation(client2, icpResult2.op()));
        if (op2 instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op2, new TypeReference<>() {
                });
                opResponse2 = (HashMap<String, Object>) opMap.get("response");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        EventResult rpyResult2 = client2.getIdentifier().addEndRole(
                "bob",
                "agent",
                client2.getAgent().getPre(),
                null);
        waitOperation(client2, rpyResult2.op());

        // Exchenge OOBIs
        Object oobi1 = client1.getOobis().get("alice", "agent");
        Map<String, Object> oobiBody1 = (Map<String, Object>) oobi1;
        ArrayList<String> oobiResponse1 = (ArrayList<String>) oobiBody1.get("oobis");

        Object oobi2 = client2.getOobis().get("bob", "agent");
        Map<String, Object> oobiBody2 = (Map<String, Object>) oobi2;
        ArrayList<String> oobiResponse2 = (ArrayList<String>) oobiBody2.get("oobis");

        resolveOobi(client1, oobiResponse2.getFirst(), "bob");
        resolveOobi(client2, oobiResponse1.getFirst(), "alice");

        // List Client 1 contacts
        Contacting.Contact[] contacts1 = client1.getContacts().list(null, null, null);
        Contacting.Contact bobContact = Arrays.stream(contacts1).filter(contact -> "bob".equals(contact.getAlias()))
                .findFirst()
                .orElse(null);

        assert bobContact != null;
        assertEquals("bob", bobContact.getAlias());
        assertEquals(0, bobContact.getAdditionalProperties().size());

        // Bob responds to Alice challenge
        // TO DO

        // Alice verifies Bob's response
        // TO DO

        // Alice verifies Bob's response
        // TO DO
    }
}
