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
        Contacting.Challenge challenge1_small = client1.getChallenges().generate(128);
        assertEquals(12, challenge1_small.words.size());
        Contacting.Challenge challenge1_big = client1.getChallenges().generate(256);
        assertEquals(24, challenge1_big.words.size());

        // Create two identifiers, one for each client
        CreateIdentifierArgs kargs1 = new CreateIdentifierArgs();
        kargs1.setToad(3);
        kargs1.setWits(List.of(
                "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
        ));
        EventResult icpResult1 = client1.getIdentifier().create("alice", kargs1);
        Operation op1 = Operation.fromObject(waitOperation(client1, icpResult1.op()));
        opResponse1 = (HashMap<String, Object>) op1.getResponse();
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
        Operation op2 = Operation.fromObject(waitOperation(client2, icpResult2.op()));
        opResponse2 = (HashMap<String, Object>) op2.getResponse();

        EventResult rpyResult2 = client2.getIdentifier().addEndRole(
                "bob",
                "agent",
                client2.getAgent().getPre(),
                null);
        waitOperation(client2, rpyResult2.op());

        // Exchange OOBIs
        Object oobi1 = client1.getOobis().get("alice", "agent");
        Map<String, Object> oobiBody1 = (Map<String, Object>) oobi1;
        ArrayList<String> oobiResponse1 = (ArrayList<String>) oobiBody1.get("oobis");

        Object oobi2 = client2.getOobis().get("bob", "agent");
        Map<String, Object> oobiBody2 = (Map<String, Object>) oobi2;
        ArrayList<String> oobiResponse2 = (ArrayList<String>) oobiBody2.get("oobis");

        resolveOobi(client1, oobiResponse2.getFirst(), "bob");
        resolveOobi(client2, oobiResponse1.getFirst(), "alice");

        // List Client 1 contacts
        Contacting.Contacts contacts1 = client1.getContacts();
        Contacting.Contact[] client1Contacts = contacts1.list();
        Contacting.Contact bobContact = findContact(client1Contacts, "bob");
        assert bobContact != null;
        assertEquals("bob", bobContact.getAlias());
        assertEquals(((List<Object>) bobContact.get("challenges")).size(), 0);

        // Bob responds to Alice's challenge
        client2.getChallenges().respond("bob", (String) opResponse1.get("i"), challenge1_small.words);
        System.out.println("Bob responded to Alice's challenge with signed words");

        // Alice verifies Bob's response
        Object verifyResult = client1.getChallenges().verify((String) opResponse2.get("i"), challenge1_small.words);
        Operation op = Operation.fromObject(waitOperation(client1, verifyResult));
        System.out.println("Alice verified challenge response");
        opResponse = (HashMap<String, Object>) op.getResponse();

        Serder exn = new Serder((Map<String, Object>) opResponse.get("exn"));
        client1.getChallenges().responded((String) opResponse2.get("i"), (String) exn.getKed().get("d"));
        System.out.println("Alice marked challenge response as accepted");

        // Check Bob's challenge in contacts
        client1Contacts = client1.getContacts().list();
        bobContact = findContact(client1Contacts, "bob");

        assertNotNull(bobContact);
        Object challenges = bobContact.get("challenges");
        assertInstanceOf(List.class, challenges);
        assertTrue((Boolean) Utils.toMap(((List<?>) challenges).getFirst()).get("authenticated"));

        List<SignifyClient> clientList = new ArrayList<>(Arrays.asList(client1, client2));
        assertOperations(clientList);
    }

    private static Contacting.Contact findContact(Contacting.Contact[] contacts, String alias) {
        for (Contacting.Contact contact : contacts) {
            if (alias.equals(contact.getAlias())) {
                return contact;
            }
        }
        return null;
    }
}
