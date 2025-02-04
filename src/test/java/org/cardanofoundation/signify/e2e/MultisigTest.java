package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialFilter;
import org.cardanofoundation.signify.app.credentialing.credentials.IssueCredentialResult;
import org.cardanofoundation.signify.app.credentialing.credentials.RevokeCredentialResult;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexAdmitArgs;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexGrantArgs;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Siger;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultisigTest extends BaseIntegrationTest {
    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    ArrayList<String> WITNESS_AIDS = new ArrayList<>(Arrays.asList(
            "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
            "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
            "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
    ));
    String SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    String SCHEMA_OOBI = env.vleiServerUrl() + "/oobi/" + SCHEMA_SAID;

    @Test
    public void multisig() throws Exception {
        // Boot Four clients
        List<SignifyClient> signifyClients = getOrCreateClientsAsync(4);
        SignifyClient client1 = signifyClients.get(0);
        SignifyClient client2 = signifyClients.get(1);
        SignifyClient client3 = signifyClients.get(2);
        SignifyClient client4 = signifyClients.get(3);

        // Create four identifiers, one for each client
        CreateIdentifierArgs createIdentifierArgs = CreateIdentifierArgs
                .builder()
                .wits(WITNESS_AIDS)
                .toad(WITNESS_AIDS.size())
                .build();
        List<States.HabState> aids = createAidAndGetHabStateAsync(
                new CreateAidArgs(client1, "member1", createIdentifierArgs),
                new CreateAidArgs(client2, "member2", createIdentifierArgs),
                new CreateAidArgs(client3, "member3", createIdentifierArgs),
                new CreateAidArgs(client4, "holder", createIdentifierArgs)
        );
        States.HabState aid1 = aids.get(0);
        States.HabState aid2 = aids.get(1);
        States.HabState aid3 = aids.get(2);
        States.HabState aid4 = aids.get(3);

        // Exchange OOBIs
        System.out.println("Resolving OOBIs");
        List<Object> oobisLst = getOobisAsync(
                new GetOobisArgs(client1, "member1", "agent"),
                new GetOobisArgs(client2, "member2", "agent"),
                new GetOobisArgs(client3, "member3", "agent"),
                new GetOobisArgs(client4, "holder", "agent")
        );

        Object oobi1 = oobisLst.get(0);
        Object oobi2 = oobisLst.get(1);
        Object oobi3 = oobisLst.get(2);
        Object oobi4 = oobisLst.get(3);

        String oobis1 = getOobisIndexAt0(oobi1);
        String oobis2 = getOobisIndexAt0(oobi2);
        String oobis3 = getOobisIndexAt0(oobi3);
        String oobis4 = getOobisIndexAt0(oobi4);

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
        System.out.println("Holder resolved 4 OOBIs");


        // First member challenge the other members with a random list of words
        // List of words should be passed to the other members out of band
        // The other members should do the same challenge/response flow, not shown here for brevity
        List<String> words = client1.getChallenges().generate(128).words;
        System.out.println("Member1 generated challenge words: " + words);

        client2.getChallenges().respond("member2", aid1.getPrefix(), words);
        System.out.println("Member2 responded challenge with signed words");

        client3.getChallenges().respond("member3", aid1.getPrefix(), words);
        System.out.println("Member3 responded challenge with signed words");

        op1 = client1.getChallenges().verify(aid2.getPrefix(), words);
        op1 = waitOperation(client1, op1);
        System.out.println("Member1 verified challenge response from member2");
        Map<String, Object> exnValue = (Map<String, Object>) Utils.toMap(Operation.fromObject(op1).getResponse()).get("exn");
        Serder exnwords = new Serder(exnValue);
        op1 = client1.getChallenges().responded(aid2.getPrefix(), exnwords.getKed().get("d").toString());
        System.out.println("Member1 marked challenge response as accepted");

        op1 = client1.getChallenges().verify(aid3.getPrefix(), words);
        op1 = operationToObject(waitOperation(client1, op1));
        System.out.println("Member1 verified challenge response from member3");
        exnValue = (Map<String, Object>) Utils.toMap(Operation.fromObject(op1).getResponse()).get("exn");
        exnwords = new Serder(exnValue);
        op1 = client1.getChallenges().responded(aid3.getPrefix(), exnwords.getKed().get("d").toString());
        System.out.println("Member1 marked challenge response as accepted");

        // First member start the creation of a multisig identifier
        List<Object> rstates = List.of(aid1.getState(), aid2.getState(), aid3.getState());
        List<Object> states = List.copyOf(rstates);

        CreateIdentifierArgs kargsMultisigAID = CreateIdentifierArgs
                .builder()
                .algo(Manager.Algos.group)
                .isith(3)
                .nsith(3)
                .toad(aid1.getState().getB().size())
                .wits(aid1.getState().getB())
                .states(states)
                .rstates(rstates)
                .build();

        kargsMultisigAID.setMhab(aid1);
        op1 = MultisigUtils.createAIDMultisig(
                client1,
                aid1,
                List.of(aid2, aid3),
                "multisig",
                kargsMultisigAID,
                true
        );
        System.out.println("Member1 initiated multisig, waiting for others to join...");

        kargsMultisigAID.setMhab(aid2);
        op2 = MultisigUtils.createAIDMultisig(
                client2,
                aid2,
                List.of(aid1, aid3),
                "multisig",
                kargsMultisigAID,
                false
        );
        System.out.println("Member2 joins multisig group, waiting for others...");

        kargsMultisigAID.setMhab(aid3);
        op3 = MultisigUtils.createAIDMultisig(
                client3,
                aid3,
                List.of(aid1, aid2),
                "multisig",
                kargsMultisigAID,
                false
        );
        System.out.println("Member3 joins multisig group, waiting for others...");

        // Check for completion
        waitOperationAsync(
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2),
                new WaitOperationArgs(client3, op3)
        );
        System.out.println("Multisig created!");

        IdentifierListResponse identifiers1 = client1.getIdentifier().list();
        List<HashMap<String, Object>> aids1 = Utils.fromJson(identifiers1.aids().toString(), new TypeReference<>() {});
        assertEquals(2, aids1.size());
        assertEquals("member1", aids1.get(0).get("name"));
        assertEquals("multisig", aids1.get(1).get("name"));

        IdentifierListResponse identifiers2 = client2.getIdentifier().list();
        List<HashMap<String, Object>> aids2 = Utils.fromJson(identifiers2.aids().toString(), new TypeReference<>() {});
        assertEquals(2, aids2.size());
        assertEquals("member2", aids2.get(0).get("name"));
        assertEquals("multisig", aids2.get(1).get("name"));

        IdentifierListResponse identifiers3 = client3.getIdentifier().list();
        List<HashMap<String, Object>> aids3 = Utils.fromJson(identifiers3.aids().toString(), new TypeReference<>() {});
        assertEquals(2, aids3.size());
        assertEquals("member3", aids3.get(0).get("name"));
        assertEquals("multisig", aids3.get(1).get("name"));

        System.out.printf(
                "Client 1 managed AIDs:\n%s [%s]\n%s [%s]%n",
                Utils.toMap(aids1.get(0)).get("name"),
                Utils.toMap(aids1.get(0)).get("prefix"),
                Utils.toMap(aids1.get(1)).get("name"),
                Utils.toMap(aids1.get(1)).get("prefix")
        );

        System.out.printf(
                "Client 2 managed AIDs:\n%s [%s]\n%s [%s]%n",
                Utils.toMap(aids2.get(0)).get("name"),
                Utils.toMap(aids2.get(0)).get("prefix"),
                Utils.toMap(aids2.get(1)).get("name"),
                Utils.toMap(aids2.get(1)).get("prefix")
        );

        System.out.printf(
                "Client 3 managed AIDs:\n%s [%s]\n%s [%s]%n",
                Utils.toMap(aids3.get(0)).get("name"),
                Utils.toMap(aids3.get(0)).get("prefix"),
                Utils.toMap(aids3.get(1)).get("name"),
                Utils.toMap(aids3.get(1)).get("prefix")
        );

        String multisig = Utils.toMap(aids3.get(1)).get("prefix").toString();

        States.HabState multisigAID = client1.getIdentifier().get("multisig");

        String timestamp = TestUtils.createTimestamp();
        List<Object> opList1 = MultisigUtils.addEndRoleMultisigs(
                client1,
                "multisig",
                aid1,
                List.of(aid2, aid3),
                multisigAID,
                timestamp,
                true
        );

        List<Object> opList2 = MultisigUtils.addEndRoleMultisigs(
                client2,
                "multisig",
                aid2,
                List.of(aid1, aid3),
                multisigAID,
                timestamp,
                false
        );
        List<Object> opList3 = MultisigUtils.addEndRoleMultisigs(
                client3,
                "multisig",
                aid3,
                List.of(aid1, aid2),
                multisigAID,
                timestamp,
                false
        );
        List<WaitOperationArgs> waitOperationArgs =
                Stream.concat(
                        Stream.concat(
                                opList1.stream().map(op -> new WaitOperationArgs(client1, op)),
                                opList2.stream().map(op -> new WaitOperationArgs(client2, op))
                        ),
                        opList3.stream().map(op -> new WaitOperationArgs(client3, op))
                ).toList();

        waitOperationAsync(waitOperationArgs.toArray(new WaitOperationArgs[0]));
        System.out.println("End role authorization completed!");

        // Holder resolve multisig OOBI
        Object oobimultisig = client1.getOobis().get("multisig", "agent");
        op4 = client4.getOobis().resolve(getOobisIndexAt0(oobimultisig), "multisig");
        waitOperation(client4, op4);
        System.out.println("Holder resolved multisig OOBI");

        // MultiSig Interaction
        // Member1 initiates an interaction event
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("i", "EBgew7O4yp8SBle0FU-wwN3GtnaroI0BQfBGAj33QiIG");
        data.put("s", "0");
        data.put("d", "EBgew7O4yp8SBle0FU-wwN3GtnaroI0BQfBGAj33QiIG");

        op1 = MultisigUtils.interactMultisig(
                client1,
                "multisig",
                aid1,
                List.of(aid2, aid3),
                data,
                states,
                true
        );
        System.out.println("Member1 initiates interaction event, waiting for others to join...");

        op2 = MultisigUtils.interactMultisig(
                client2,
                "multisig",
                aid2,
                List.of(aid1, aid3),
                data,
                states,
                false
        );
        System.out.println("Member2 joins interaction event, waiting for others...");

        op3 = MultisigUtils.interactMultisig(
                client3,
                "multisig",
                aid3,
                List.of(aid1, aid2),
                data,
                states,
                false
        );
        System.out.println("Member3 joins interaction event, waiting for others...");

        // Check for completion
        waitOperationAsync(
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2),
                new WaitOperationArgs(client3, op3)
        );
        System.out.println("Multisig interaction completed!");

        // Members agree out of band to rotate keys
        System.out.println("Members agree out of band to rotate keys");
        EventResult icpResult1 = client1.getIdentifier().rotate("member1");
        op1 = icpResult1.op();
        op1 = waitOperation(client1, op1);
        aid1 = client1.getIdentifier().get("member1");
        System.out.println("Member1 rotated keys");

        EventResult icpResult2 = client2.getIdentifier().rotate("member2");
        op2 = icpResult2.op();
        op2 = waitOperation(client2, op2);
        aid2 = client2.getIdentifier().get("member2");
        System.out.println("Member2 rotated keys");

        EventResult icpResult3 = client3.getIdentifier().rotate("member3");
        op3 = icpResult3.op();
        op3 = waitOperation(client3, op3);
        aid3 = client3.getIdentifier().get("member3");
        System.out.println("Member3 rotated keys");

        // Update new key states
        op1 = client1.getKeyStates().query(aid2.getPrefix(), "1");
        op1 = waitOperation(client1, op1);
        Object aid2State = Operation.fromObject(op1).getResponse();
        op1 = client1.getKeyStates().query(aid3.getPrefix(), "1");
        op1 = waitOperation(client1, op1);
        Object aid3State = Operation.fromObject(op1).getResponse();

        op2 = client2.getKeyStates().query(aid3.getPrefix(), "1");
        op2 = waitOperation(client2, op2);
        op2 = client2.getKeyStates().query(aid1.getPrefix(), "1");
        op2 = waitOperation(client2, op2);
        Object aid1State = Operation.fromObject(op2).getResponse();

        op3 = client3.getKeyStates().query(aid1.getPrefix(), "1");
        op3 = waitOperation(client3, op3);
        op3 = client3.getKeyStates().query(aid2.getPrefix(), "1");
        op3 = waitOperation(client3, op3);

        op4 = client4.getKeyStates().query(aid1.getPrefix(), "1");
        op4 = waitOperation(client4, op4);
        op4 = client4.getKeyStates().query(aid2.getPrefix(), "1");
        op4 = waitOperation(client4, op4);
        op4 = client4.getKeyStates().query(aid3.getPrefix(), "1");
        op4 = waitOperation(client4, op4);

        List<States.State> rstateLst = List.of(
                Utils.fromJson(Utils.jsonStringify(aid1State), States.State.class),
                Utils.fromJson(Utils.jsonStringify(aid2State), States.State.class),
                Utils.fromJson(Utils.jsonStringify(aid3State), States.State.class)
        );
        List<States.State> stateLst = rstateLst;

        // Multisig Rotation

        // Member1 initiates a rotation event
        RotateIdentifierArgs rotateIdentifierArgs = RotateIdentifierArgs.builder()
                .states(stateLst)
                .rstates(rstateLst)
                .build();

        op1 = MultisigUtils.rotateMultisig(
                client1,
                "multisig",
                aid1,
                List.of(aid2, aid3),
                rotateIdentifierArgs,
                "/multisig/rot",
                true
        );
        System.out.println("Member1 initiates rotation event, waiting for others to join...");


        op2 = MultisigUtils.rotateMultisig(
                client2,
                "multisig",
                aid2,
                List.of(aid1, aid3),
                rotateIdentifierArgs,
                "/multisig/ixn",
                false
        );
        System.out.println("Member2 joins rotation event, waiting for others...");

        op3 = MultisigUtils.rotateMultisig(
                client3,
                "multisig",
                aid3,
                List.of(aid1, aid2),
                rotateIdentifierArgs,
                "/multisig/ixn",
                false
        );
        System.out.println("Member3 joins rotation event, waiting for others...");

        waitOperationAsync(
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2),
                new WaitOperationArgs(client3, op3)
        );

        States.HabState hab = client1.getIdentifier().get("multisig");
        String aid = hab.getPrefix();

        // Multisig Registry creation
        aid1 = client1.getIdentifier().get("member1");
        aid2 = client2.getIdentifier().get("member2");
        aid3 = client3.getIdentifier().get("member3");
        System.out.println("Starting multisig registry creation");

        String nonce = Coring.randomNonce();
        List<Object> registryMultisigList = createRegistryMultisig(
                client1,
                aid1,
                List.of(aid2, aid3),
                multisigAID,
                "vLEI Registry",
                nonce,
                true
        );
        op1 = registryMultisigList.get(0);
        String regk = registryMultisigList.get(1).toString();
        System.out.println("Member1 initiated registry, waiting for others to join...");

        // Member2 check for notifications and join the create registry event
        op2 = MultisigUtils.createRegistryMultisig(
                client2,
                aid2,
                List.of(aid1, aid3),
                multisigAID,
                "vLEI Registry",
                nonce,
                false
        );
        System.out.println("Member2 joins registry event, waiting for others...");

        // Member3 check for notifications and join the create registry event
        op3 = MultisigUtils.createRegistryMultisig(
                client3,
                aid3,
                List.of(aid1, aid2),
                multisigAID,
                "vLEI Registry",
                nonce,
                "multisig",
                false
        );

        waitOperationAsync(
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2),
                new WaitOperationArgs(client3, op3)
        );
        System.out.println("Multisig create registry completed!");

        //Create Credential
        System.out.println("Starting multisig credential creation");
        Map<String, Object> vcdata = new HashMap<>();
        vcdata.put("LEI", "5493001KJTIIGC8Y1R17");
        String holder = aid4.getPrefix();
        String TIME = new Date().toInstant().toString().replace("Z", "000+00:00");

        CredentialData.CredentialSubject subject = CredentialData.CredentialSubject.builder()
                .i(holder)
                .dt(TIME)
                .additionalProperties(vcdata)
                .build();

        CredentialData credentialData = CredentialData.builder()
                .ri(regk)
                .s(SCHEMA_SAID)
                .a(subject)
                .build();

        IssueCredentialResult credRes = client1.getCredentials().issue("multisig", credentialData);
        op1 = credRes.getOp();

        multisigIssue(client1, "member1", "multisig", credRes);

        System.out.println("Member1 initiated credential creation, waiting for others to join...");

        // Member2 check for notifications and join the credential create event
        String msgSaid = waitAndMarkNotification(client2, "/multisig/iss");
        System.out.println("Member2 received exchange message to join the credential create event");

        Object res = client2.getGroups().getRequest(msgSaid);
        Map<String, Object> exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );
        String credentialSaid = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(exn.get("e")).get("acdc")).get("d").toString();

        Object acdcMap = Utils.toMap(Utils.toMap(exn.get("e"))).get("acdc");
        String i = Utils.toMap(Utils.toMap(acdcMap).get("a")).get("i").toString();
        String dt = Utils.toMap(Utils.toMap(acdcMap).get("a")).get("dt").toString();
        String LEI = Utils.toMap(Utils.toMap(acdcMap).get("a")).get("LEI").toString();

        CredentialData credentialData2 = Utils.fromJson(Utils.jsonStringify(acdcMap), CredentialData.class);
        CredentialData.CredentialSubject credentialSubject = CredentialData.CredentialSubject.builder()
                .i(i)
                .dt(dt)
                .additionalProperties(Map.of("LEI", LEI))
                .build();
        credentialData2.setA(credentialSubject);
        IssueCredentialResult credRes2 = client2.getCredentials().issue("multisig", credentialData2);

        op2 = credRes2.getOp();
        multisigIssue(client2, "member2", "multisig", credRes2);
        System.out.println("Member2 joins credential create event, waiting for others...");

        // Member3 check for notifications and join the create registry event
        msgSaid = waitAndMarkNotification(client3, "/multisig/iss");
        System.out.println("Member3 received exchange message to join the credential create event");
        res = client3.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );
        acdcMap = Utils.toMap(Utils.toMap(exn.get("e"))).get("acdc");
        i = Utils.toMap(Utils.toMap(acdcMap).get("a")).get("i").toString();
        dt = Utils.toMap(Utils.toMap(acdcMap).get("a")).get("dt").toString();
        LEI = Utils.toMap(Utils.toMap(acdcMap).get("a")).get("LEI").toString();
        CredentialData credentialData3 = Utils.fromJson(Utils.jsonStringify(acdcMap), CredentialData.class);
        credentialSubject = CredentialData.CredentialSubject.builder()
                .i(i)
                .dt(dt)
                .additionalProperties(Map.of("LEI", LEI))
                .build();
        credentialData3.setA(credentialSubject);
        IssueCredentialResult credRes3 = client3.getCredentials().issue("multisig", credentialData3);

        op3 = credRes3.getOp();
        multisigIssue(client3, "member3", "multisig", credRes3);
        System.out.println("Member3 joins credential create event, waiting for others...");

        // Check completion
        waitOperationAsync(
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2),
                new WaitOperationArgs(client3, op3)
        );
        System.out.println("Multisig create credential completed!");

        States.HabState m = client1.getIdentifier().get("multisig");

        // Update states
        op1 = client1.getKeyStates().query(m.getPrefix(), "4");
        op1 = waitOperation(client1, op1);
        op2 = client2.getKeyStates().query(m.getPrefix(), "4");
        op2 = waitOperation(client2, op2);
        op3 = client3.getKeyStates().query(m.getPrefix(), "4");
        op3 = waitOperation(client3, op3);
        op4 = client4.getKeyStates().query(m.getPrefix(), "4");
        op4 = waitOperation(client4, op4);

        // IPEX grant message
        System.out.println("Starting grant message");
        String stamp = new Date().toInstant().toString().replace("Z", "000+00:00");

        Exchanging.ExchangeMessageResult grantResult = client1.getIpex().grant(IpexGrantArgs.builder()
                .senderName("multisig")
                .acdc(credRes.getAcdc())
                .anc(credRes.getAnc())
                .iss(credRes.getIss())
                .recipient(holder)
                .datetime(stamp)
                .build()
        );
        Serder grant = grantResult.exn();
        List<String> gsigs = grantResult.sigs();
        String end = grantResult.atc();

        op1 = client1.getIpex().submitGrant("multisig", grant, gsigs, end, List.of(holder));

        Map<String, Object> mstate = Utils.toMap(m.getState());
        List<Object> seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", m.getPrefix(),
                        "s", Utils.toMap(mstate.get("ee")).get("s"),
                        "d", Utils.toMap(mstate.get("ee")).get("d")
                )
        );
        List<Siger> sigers = gsigs.stream()
                .map(Siger::new)
                .collect(Collectors.toList());

        String gims = new String(Eventing.messagize(grant, sigers, seal));
        String atc = gims.substring(grant.getSize());
        atc += end;
        Map<String, List<Object>> gembeds = new LinkedHashMap<>();
        gembeds.put("exn", Arrays.asList(grant, atc));

        List<String> recp = Stream.of(aid2.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client1.getExchanges().send(
                "member1",
                "multisig",
                aid1,
                "/multisig/exn",
                Map.of("gid", m.getPrefix()),
                gembeds,
                recp
        );
        System.out.println("Member1 initiated grant message, waiting for others to join...");

        msgSaid = waitAndMarkNotification(client2, "/multisig/exn");
        System.out.println("Member2 received exchange message to join the grant message");
        res = client2.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );

        Exchanging.ExchangeMessageResult grantResult2 = client2.getIpex().grant(IpexGrantArgs.builder()
                .senderName("multisig")
                .recipient(holder)
                .acdc(credRes2.getAcdc())
                .anc(credRes2.getAnc())
                .iss(credRes2.getIss())
                .datetime(stamp)
                .build()
        );
        Serder grant2 = grantResult2.exn();
        List<String> gsigs2 = grantResult2.sigs();
        String end2 = grantResult2.atc();

        op2 = client2.getIpex().submitGrant("multisig", grant2, gsigs2, end2, List.of(holder));

        sigers = gsigs2.stream()
                .map(Siger::new)
                .collect(Collectors.toList());

        gims = new String(Eventing.messagize(grant2, sigers, seal));
        atc = gims.substring(grant2.getSize());
        atc += end2;

        gembeds = new LinkedHashMap<>();
        gembeds.put("exn", Arrays.asList(grant2, atc));
        recp = Stream.of(aid1.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client2.getExchanges().send(
                "member2",
                "multisig",
                aid2,
                "/multisig/exn",
                Map.of("gid", m.getPrefix()),
                gembeds,
                recp
        );

        System.out.println("Member2 joined grant message, waiting for others to join...");

        msgSaid = waitAndMarkNotification(client3, "/multisig/exn");
        System.out.println("Member3 received exchange message to join the grant message");
        res = client3.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );

        Exchanging.ExchangeMessageResult grantResult3 = client3.getIpex().grant(IpexGrantArgs.builder()
                .senderName("multisig")
                .recipient(holder)
                .acdc(credRes3.getAcdc())
                .anc(credRes3.getAnc())
                .iss(credRes3.getIss())
                .datetime(stamp)
                .build()
        );
        Serder grant3 = grantResult3.exn();
        List<String> gsigs3 = grantResult3.sigs();
        String end3 = grantResult3.atc();

        op3 = client3.getIpex().submitGrant("multisig", grant3, gsigs3, end3, List.of(holder));

        sigers = gsigs3.stream()
                .map(Siger::new)
                .collect(Collectors.toList());

        gims = new String(Eventing.messagize(grant3, sigers, seal));
        atc = gims.substring(grant3.getSize());
        atc += end3;

        gembeds = new LinkedHashMap<>();
        gembeds.put("exn", Arrays.asList(grant3, atc));
        recp = Stream.of(aid1.getState(), aid2.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client3.getExchanges().send(
                "member3",
                "multisig",
                aid3,
                "/multisig/exn",
                Map.of("gid", m.getPrefix()),
                gembeds,
                recp
        );

        System.out.println("Member3 joined grant message, waiting for others to join...");

        msgSaid = waitAndMarkNotification(client4, "/exn/ipex/grant");
        System.out.println("Holder received exchange message with the grant message");
        res = client4.getExchanges().get(msgSaid);

        Exchanging.ExchangeMessageResult admitResult = client4.getIpex().admit(IpexAdmitArgs.builder()
                .senderName("holder")
                .message("")
                .grantSaid(Utils.toMap(Utils.toMap(res).get("exn")).get("d").toString())
                .recipient(m.getPrefix())
                .build()
        );
        Serder admit = admitResult.exn();
        List<String> asigs = admitResult.sigs();
        String aend = admitResult.atc();

        op4 = client4.getIpex().submitAdmit("holder", admit, asigs, aend, List.of(m.getPrefix()));

        waitOperationAsync(
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2),
                new WaitOperationArgs(client3, op3),
                new WaitOperationArgs(client4, op4)
        );
        System.out.println("Holder creates and sends admit message");

        msgSaid = waitAndMarkNotification(client1, "/exn/ipex/admit");
        System.out.println("Member1 received exchange message with the admit response");
        List<Object> creds = (List<Object>) client4.getCredentials().list(CredentialFilter.builder().build());
        System.out.println("Holder holds " + creds.size() + " credential");

        assertOperations(List.of(client1, client2, client3, client4));
        warnNotifications(List.of(client1, client2, client3, client4));

        System.out.println("Revoking credential...");
        String REVTIME = new Date().toInstant().toString().replace("Z", "000+00:00");
        RevokeCredentialResult revokeRes = client1.getCredentials().revoke("multisig", credentialSaid, REVTIME);
        op1 = revokeRes.getOp();

        multisigRevoke(client1, "member1", "multisig", revokeRes.getRev(), revokeRes.getAnc());
        System.out.println("Member1 initiated credential revocation, waiting for others to join...");

        // Member2 check for notifications and join the credential create  event
        msgSaid = waitAndMarkNotification(client2, "/multisig/rev");
        System.out.println("Member2 received exchange message to join the credential revocation event");
        res = client2.getGroups().getRequest(msgSaid);

        RevokeCredentialResult revokeRes2 = client2.getCredentials().revoke("multisig", credentialSaid, REVTIME);
        op2 = revokeRes2.getOp();
        multisigRevoke(client2, "member2", "multisig", revokeRes2.getRev(), revokeRes2.getAnc());
        System.out.println("Member2 joins credential revoke event, waiting for others...");

        // Member3 check for notifications and join the create registry event
        msgSaid = waitAndMarkNotification(client3, "/multisig/rev");
        System.out.println("Member3 received exchange message to join the credential revocation event");
        res = client3.getGroups().getRequest(msgSaid);

        RevokeCredentialResult revokeRes3 = client3.getCredentials().revoke("multisig", credentialSaid, REVTIME);
        op3 = revokeRes3.getOp();
        multisigRevoke(client3, "member3", "multisig", revokeRes3.getRev(), revokeRes3.getAnc());
        System.out.println("Member3 joins credential revoke event, waiting for others...");

        // Check completion
        waitOperationAsync(
                new WaitOperationArgs(client1, op1),
                new WaitOperationArgs(client2, op2),
                new WaitOperationArgs(client3, op3)
        );
        System.out.println("Multisig credential revocation completed!");
    }

    public String getOobisIndexAt0(Object oobi) {
        Map<String, Object> oobiBody = Utils.toMap(oobi);
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }

    public void multisigIssue(
            SignifyClient client,
            String memberName,
            String groupName,
            IssueCredentialResult result
    ) throws Exception {
        States.HabState leaderHab = client.getIdentifier().get(memberName);
        States.HabState groupHab = client.getIdentifier().get(groupName);
        Object members = client.getIdentifier().members(groupName);

        Keeping.Keeper<?> keeper = client.getManager().get(groupHab);
        Keeping.SignResult sigs = keeper.sign(result.getAnc().getRaw().getBytes());
        List<Siger> sigers = sigs.signatures().stream()
                .map(Siger::new)
                .collect(Collectors.toList());

        String ims = new String(Eventing.messagize(result.getAnc(), sigers));
        String atc = ims.substring(result.getAnc().getSize());

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("acdc", Arrays.asList(result.getAcdc(), ""));
        embeds.put("iss", Arrays.asList(result.getIss(), ""));
        embeds.put("anc", Arrays.asList(result.getAnc(), atc));

        Map<String, Object> membersMap = Utils.toMap(members);
        List<Map<String, Object>> signing = (List<Map<String, Object>>) membersMap.get("signing");
        List<String> recipients = signing.stream()
                .map(m -> m.get("aid").toString())
                .filter(aid -> !aid.equals(leaderHab.getPrefix()))
                .collect(Collectors.toList());

        client.getExchanges().send(
                memberName,
                "multisig",
                leaderHab,
                "/multisig/iss",
                Map.of("gid", groupHab.getPrefix()),
                embeds,
                recipients
        );
    }

    public void multisigRevoke(
            SignifyClient client,
            String memberName,
            String groupName,
            Serder rev,
            Serder anc
    ) throws Exception {
        States.HabState leaderHab = client.getIdentifier().get(memberName);
        States.HabState groupHab = client.getIdentifier().get(groupName);
        Object members = client.getIdentifier().members(groupName);

        Keeping.Keeper<?> keeper = client.getManager().get(groupHab);
        Keeping.SignResult sigs = keeper.sign(anc.getRaw().getBytes());
        List<Siger> sigers = sigs.signatures().stream()
                .map(Siger::new)
                .collect(Collectors.toList());

        String ims = new String(Eventing.messagize(anc, sigers));
        String atc = ims.substring(anc.getSize());

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("iss", Arrays.asList(rev, ""));
        embeds.put("anc", Arrays.asList(anc, atc));

        Map<String, Object> membersMap = Utils.toMap(members);
        List<Map<String, Object>> signing = (List<Map<String, Object>>) membersMap.get("signing");
        List<String> recipients = signing.stream()
                .map(m -> m.get("aid").toString())
                .filter(aid -> !aid.equals(leaderHab.getPrefix()))
                .collect(Collectors.toList());

        client.getExchanges().send(
                memberName,
                "multisig",
                leaderHab,
                "/multisig/rev",
                Map.of("gid", groupHab.getPrefix()),
                embeds,
                recipients
        );
    }

    public static List<Object> createRegistryMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            States.HabState multisigAID,
            String registryName,
            String nonce,
            boolean isInitiator) throws Exception {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/vcp");
        }

        CreateRegistryArgs createRegistryArgs = CreateRegistryArgs
                .builder()
                .name(multisigAID.getName())
                .registryName(registryName)
                .nonce(nonce)
                .build();
        RegistryResult vcpResult = client.getRegistries().create(createRegistryArgs);
        Object op = vcpResult.op();

        Serder serder = vcpResult.getRegser();
        String regk = serder.getPre();
        Serder anc = vcpResult.getSerder();
        List<String> sigs = vcpResult.getSigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();

        String ims = new String(Eventing.messagize(anc, sigers, null, null, null, false));
        String atc = ims.substring(anc.getSize());

        Map<String, List<Object>> regbeds = new LinkedHashMap<>() {{
            put("vcp", List.of(serder, ""));
            put("anc", List.of(anc, atc));
        }};

        List<String> recp = otherMembersAIDs.stream()
                .map(States.HabState::getPrefix)
                .toList();

        client.getExchanges().send(
                aid.getName(),
                "registry",
                aid,
                "/multisig/vcp",
                Map.of("gid", multisigAID.getPrefix()),
                regbeds,
                recp
        );
        List<Object> list = List.of(op, regk);
        return list;
    }

    public static <T> Operation<T> waitOperations(
            SignifyClient client,
            Object op) throws SodiumException, IOException, InterruptedException {
        Operation operation = Operation.fromObject(op);
        String name = operation.getName();
        operation = client.getOperations().wait(operation);
        TestUtils.deleteOperations(client, operation);
        TestUtils.deleteOperation(client, name);
        return operation;
    }

    public void deleteOperations(List<SignifyClient> clients, List<String> name) throws Exception {
        for (int i = 1; i <= clients.size(); i++) {
            deleteOperation(clients.get(i - 1), name.get(i - 1));
        }
    }
}
