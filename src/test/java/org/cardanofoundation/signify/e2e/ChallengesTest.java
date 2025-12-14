package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.Contacting;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.Utils;

import org.cardanofoundation.signify.generated.keria.model.Challenge;
import org.cardanofoundation.signify.generated.keria.model.Contact;
import org.cardanofoundation.signify.generated.keria.model.OOBI;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;

public class ChallengesTest {
    private static final Logger log = LoggerFactory.getLogger(ChallengesTest.class);
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private static SignifyClient client1, client2;
    private HashMap<String, Object> opResponse, opResponse1, opResponse2;

    @Test
    void ChallengeTest() throws Exception {
        String bran1 = Coring.randomPasscode();
        String bran2 = Coring.randomPasscode();
        client1 = new SignifyClient(
                url,
                bran1,
                Tier.LOW,
                bootUrl,
                null
        );
        client2 = new SignifyClient(
                url,
                bran2,
                Tier.LOW,
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
        Challenge challenge1_small = client1.challenges().generate(128);
        assertEquals(12, challenge1_small.getWords().size());
        Challenge challenge1_big = client1.challenges().generate(256);
        assertEquals(24, challenge1_big.getWords().size());

        // Create two identifiers, one for each client
        CreateIdentifierArgs kargs1 = new CreateIdentifierArgs();
        kargs1.setToad(3);
        kargs1.setWits(List.of(
                "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
        ));
        EventResult icpResult1 = client1.identifiers().create("alice", kargs1);
        Operation op1 = Operation.fromObject(waitOperation(client1, icpResult1.op()));
        opResponse1 = (HashMap<String, Object>) op1.getResponse();
        EventResult rpyResult1 = client1.identifiers().addEndRole(
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
        EventResult icpResult2 = client2.identifiers().create("bob", kargs2);
        Operation op2 = Operation.fromObject(waitOperation(client2, icpResult2.op()));
        opResponse2 = (HashMap<String, Object>) op2.getResponse();

        EventResult rpyResult2 = client2.identifiers().addEndRole(
                "bob",
                "agent",
                client2.getAgent().getPre(),
                null);
        waitOperation(client2, rpyResult2.op());

        // Exchange OOBIs
        OOBI oobi1 = client1.oobis().get("alice", "agent").get();
        List<String> oobiResponse1 = oobi1.getOobis();

        OOBI oobi2 = client2.oobis().get("bob", "agent").get();
        List<String> oobiResponse2 = oobi2.getOobis();

        resolveOobi(client1, oobiResponse2.getFirst(), "bob");
        resolveOobi(client2, oobiResponse1.getFirst(), "alice");

        // List Client 1 contacts
        Contacting.Contacts contacts1 = client1.contacts();
        Contact[] client1Contacts = contacts1.list();
        Contact bobContact = findContact(client1Contacts, "bob");
        assert bobContact != null;
        assertEquals("bob", bobContact.getAlias());
        assertEquals(0, bobContact.getChallenges().size());

        // Bob responds to Alice's challenge
        client2.challenges().respond("bob", (String) opResponse1.get("i"), challenge1_small.getWords());
        System.out.println("Bob responded to Alice's challenge with signed words");

        // Alice verifies Bob's response
        Object verifyResult = client1.challenges().verify((String) opResponse2.get("i"), challenge1_small.getWords());
        Operation op = Operation.fromObject(waitOperation(client1, verifyResult));
        System.out.println("Alice verified challenge response");
        opResponse = (HashMap<String, Object>) op.getResponse();

        Serder exn = new Serder((Map<String, Object>) opResponse.get("exn"));
        client1.challenges().responded((String) opResponse2.get("i"), (String) exn.getKed().get("d"));
        System.out.println("Alice marked challenge response as accepted");

        // Check Bob's challenge in contacts
        client1Contacts = client1.contacts().list();
        bobContact = findContact(client1Contacts, "bob");

        assertNotNull(bobContact);
        List<Challenge> challenges = bobContact.getChallenges();
        assertTrue(challenges.getFirst().getAuthenticated());

        List<SignifyClient> clientList = new ArrayList<>(Arrays.asList(client1, client2));
        assertOperations(clientList);
    }

    private static Contact findContact(Contact[] contacts, String alias) {
        for (Contact contact : contacts) {
            if (alias.equals(contact.getAlias())) {
                return contact;
            }
        }
        return null;
    }
}
