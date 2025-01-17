package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.clienting.aiding.RotateIdentifierArgs;
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
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultisigTest extends TestUtils {
    SignifyClient client1, client2, client3, client4;
    States.HabState aid1, aid2, aid3, aid4;
    Object oobi1, oobi2, oobi3, oobi4;
    String oobis1, oobis2, oobis3, oobis4;
    Siger siger;
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
        op1 = operationToObject(waitOperation(client1, op1));
        System.out.println("Member1 verified challenge response from member2");
        Map<String, Object> exnValue = new LinkedHashMap<>();
        if (op1 instanceof String) {
            Map<String, Object> opMap = objectMapper.readValue(op1.toString(), new TypeReference<>() {
            });
            Map<String, Object> op1Response = Utils.toMap(opMap.get("response"));
            exnValue = Utils.toMap(op1Response.get("exn"));
        }
        Serder exnwords = new Serder(exnValue);
        op1 = client1.getChallenges().responded(aid2.getPrefix(), exnwords.getKed().get("d").toString());
        System.out.println("Member1 marked challenge response as accepted");

        op1 = client1.getChallenges().verify(aid3.getPrefix(), words);
        op1 = operationToObject(waitOperation(client1, op1));
        System.out.println("Member1 verified challenge response from member3");
        if (op1 instanceof String) {
            Map<String, Object> opMap = objectMapper.readValue(op1.toString(), new TypeReference<>() {
            });
            Map<String, Object> op1Response = Utils.toMap(opMap.get("response"));
            exnValue = Utils.toMap(op1Response.get("exn"));
        }
        exnwords = new Serder(exnValue);
        op1 = client1.getChallenges().responded(aid3.getPrefix(), exnwords.getKed().get("d").toString());
        System.out.println("Member1 marked challenge response as accepted");

        // First member start the creation of a multisig identifier
        List<States.State> rstates = Arrays.asList(aid1.getState(), aid2.getState(), aid3.getState());
        List<States.State> states = rstates;
        CreateIdentifierArgs iargs = new CreateIdentifierArgs();
        iargs.setAlgo(Manager.Algos.group);
        iargs.setMhab(aid1);
        iargs.setIsith(3);
        iargs.setNsith(3);
        iargs.setToad(aid1.getState().getB().size());
        iargs.setWits(aid1.getState().getB());
        iargs.setStates(Collections.singletonList(states));
        iargs.setRstates(Collections.singletonList(rstates));
        EventResult icpResult1 = client1.getIdentifier().create("multisig", iargs);

        op1 = icpResult1.op();
        Serder serder = icpResult1.serder();

        List<String> sigs = icpResult1.sigs();
        List<Siger> sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        String ims = new String(Eventing.messagize(serder, sigers));
        String atc = ims.substring(serder.getSize());
        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("icp", Arrays.asList(serder, atc));

        List<String> smids = states.stream()
                .map(state -> Utils.toMap(state).get("i").toString())
                .collect(Collectors.toList());

        List<String> recp = Stream.of(aid2.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", serder.getPre());
        payload.put("smids", smids);
        payload.put("rmids", smids);

        client1.getExchanges().send(
                "member1",
                "multisig",
                aid1,
                "/multisig/icp",
                payload,
                embeds,
                recp
        );
        System.out.println("Member1 initiated multisig, waiting for others to join...");

        // Second member check notifications and join the multisig
        String msgSaid = waitAndMarkNotification(client2, "/multisig/icp");
        System.out.println("Member2 received exchange message to join multisig");

        Object res = client2.getGroups().getRequest(msgSaid);
        Map<String, Object> exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );
        Map<String, Object> icp = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(exn.get("e")).get("icp")
        );

        CreateIdentifierArgs iargs2 = new CreateIdentifierArgs();
        iargs2.setAlgo(Manager.Algos.group);
        iargs2.setMhab(aid2);
        iargs2.setIsith(icp.get("kt"));
        iargs2.setNsith(icp.get("nt"));
        iargs2.setToad(Integer.parseInt(icp.get("bt").toString()));
        iargs2.setWits(Utils.toList(icp.get("b")));
        iargs2.setStates(Collections.singletonList(states));
        iargs2.setRstates(Collections.singletonList(rstates));

        EventResult icpResult2 = client2.getIdentifier().create("multisig", iargs2);

        op2 = icpResult2.op();
        serder = icpResult2.serder();
        sigs = icpResult2.sigs();
        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        embeds = new LinkedHashMap<>();
        embeds.put("icp", Arrays.asList(serder, atc));

        smids = Utils.toList(Utils.toMap(exn.get("a")).get("smids"));
        recp = Stream.of(aid1.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        payload = new LinkedHashMap<>();
        payload.put("gid", serder.getPre());
        payload.put("smids", smids);
        payload.put("rmids", smids);

        client2.getExchanges().send(
                "member2",
                "multisig",
                aid2,
                "/multisig/icp",
                payload,
                embeds,
                recp
        );
        System.out.println("Member2 joins multisig group, waiting for others...");

        // Third member check notifications and join the multisig
        msgSaid = waitAndMarkNotification(client3, "/multisig/icp");
        System.out.println("Member3 received exchange message to join multisig");

        res = client3.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(castObjectToListMap(res).getFirst().get("exn"));
        icp = castObjectToLinkedHashMap(castObjectToLinkedHashMap(exn.get("e")).get("icp"));

        CreateIdentifierArgs iargs3 = new CreateIdentifierArgs();
        iargs3.setAlgo(Manager.Algos.group);
        iargs3.setMhab(aid3);
        iargs3.setIsith(icp.get("kt"));
        iargs3.setNsith(icp.get("nt"));
        iargs3.setToad(Integer.parseInt(icp.get("bt").toString()));
        iargs3.setWits(Utils.toList(icp.get("b")));
        iargs3.setStates(Collections.singletonList(states));
        iargs3.setRstates(Collections.singletonList(rstates));

        EventResult icpResult3 = client3.getIdentifier().create("multisig", iargs3);

        op3 = icpResult3.op();
        serder = icpResult3.serder();
        sigs = icpResult3.sigs();
        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        embeds = new LinkedHashMap<>();
        embeds.put("icp", Arrays.asList(serder, atc));

        smids = Utils.toList(Utils.toMap(exn.get("a")).get("smids"));
        recp = Stream.of(aid1.getState(), aid2.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        payload = new LinkedHashMap<>();
        payload.put("gid", serder.getPre());
        payload.put("smids", smids);
        payload.put("rmids", smids);

        client3.getExchanges().send(
                "member3",
                "multisig",
                aid3,
                "/multisig/icp",
                payload,
                embeds,
                recp
        );
        System.out.println("Member3 joins multisig group, waiting for others...");

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
        System.out.println("Multisig created!");

        IdentifierListResponse identifiers1 = client1.getIdentifier().list();
        List<Object> aids1 = (List<Object>) identifiers1.aids();
        assertEquals(2, aids1.size());
        assertEquals("member1", Utils.toMap(aids1.get(0)).get("name"));
        assertEquals("multisig", Utils.toMap(aids1.get(1)).get("name"));

        IdentifierListResponse identifiers2 = client2.getIdentifier().list();
        List<Object> aids2 = (List<Object>) identifiers2.aids();
        assertEquals(2, aids2.size());
        assertEquals("member2", Utils.toMap(aids2.get(0)).get("name"));
        assertEquals("multisig", Utils.toMap(aids2.get(1)).get("name"));

        IdentifierListResponse identifiers3 = client3.getIdentifier().list();
        List<Object> aids3 = (List<Object>) identifiers3.aids();
        assertEquals(2, aids3.size());
        assertEquals("member3", Utils.toMap(aids3.get(0)).get("name"));
        assertEquals("multisig", Utils.toMap(aids3.get(1)).get("name"));

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

        // Multisig end role
        // for brevity, this script authorize only the agent of member 1
        // a full implementation should repeat the process to authorize all agents
        Object members = client1.getIdentifier().members("multisig");
        States.HabState hab = client1.getIdentifier().get("multisig");
        String aid = hab.getPrefix();
        List<Map<String, Object>> signing = (List<Map<String, Object>>) Utils.toMap(members).get("signing");
        String eid1 = Utils.toList(Utils.toMap(Utils.toMap(signing.getFirst().get("ends")).get("agent")).keySet()).getFirst(); //agent of member 1
        // Other agent EIDs can be obtained with:
        // String eid2 = Utils.toList(Utils.toMap(Utils.toMap(signing.get(1).get("ends")).get("agent")).keySet()).getFirst();
        // String eid3 = Utils.toList(Utils.toMap(Utils.toMap(signing.get(2).get("ends")).get("agent")).keySet()).getFirst();
        System.out.printf("Starting multisig end role authorization for agent %s%n", eid1);

        // initial stamp for the event that will be passed in the exn message
        // to the other members
        String stamp = new Date().toInstant().toString().replace("Z", "000+00:00");

        EventResult endRoleRes = client1
                .getIdentifier()
                .addEndRole("multisig", "agent", eid1, stamp);
        op1 = endRoleRes.op();
        Serder rpy = endRoleRes.serder();
        sigs = endRoleRes.sigs();

        Map<String, Object> mstate = Utils.toMap(hab.getState());
        List<Object> seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", hab.getPrefix(),
                        "s", Utils.toMap(mstate.get("ee")).get("s"),
                        "d", Utils.toMap(mstate.get("ee")).get("d")
                )
        );

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        String roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        atc = roleims.substring(rpy.getSize());

        Map<String, List<Object>> roleembeds = new LinkedHashMap<>();
        roleembeds.put("rpy", Arrays.asList(rpy, atc));

        recp = Stream.of(aid2.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client1.getExchanges().send(
                "member1",
                "multisig",
                aid1,
                "/multisig/rpy",
                Map.of("gid", aid),
                roleembeds,
                recp
        );

        System.out.printf("Member1 authorized agent role to %s, waiting for others to authorize...", eid1);

        // Member2 check for notifications and join the authorization
        msgSaid = waitAndMarkNotification(client2, "/multisig/rpy");
        System.out.println("Member2 received exchange message to join the end role authorization");

        res = client2.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(castObjectToListMap(res).getFirst().get("exn"));

        // stamp, eid and role are provided in the exn message
        String rpystamp = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(
                        castObjectToLinkedHashMap(exn.get("e"))).get("rpy")).get("dt")
                .toString();

        String rpyrole = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(
                        castObjectToLinkedHashMap(
                                castObjectToLinkedHashMap(
                                        exn.get("e"))).get("rpy")).get("a")).get("role")
                .toString();

        String rpyeid = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(
                        castObjectToLinkedHashMap(
                                castObjectToLinkedHashMap(
                                        exn.get("e"))).get("rpy")).get("a")).get("eid")
                .toString();

        endRoleRes = client2
                .getIdentifier()
                .addEndRole("multisig", rpyrole, rpyeid, rpystamp);
        op2 = endRoleRes.op();
        rpy = endRoleRes.serder();
        sigs = endRoleRes.sigs();

        hab = client2.getIdentifier().get("multisig");
        mstate = Utils.toMap(hab.getState());
        seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", hab.getPrefix(),
                        "s", Utils.toMap(mstate.get("ee")).get("s"),
                        "d", Utils.toMap(mstate.get("ee")).get("d")
                )
        );

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        atc = roleims.substring(rpy.getSize());

        roleembeds = new LinkedHashMap<>();
        roleembeds.put("rpy", Arrays.asList(rpy, atc));

        recp = Stream.of(aid1.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client2.getExchanges().send(
                "member2",
                "multisig",
                aid2,
                "/multisig/rpy",
                Map.of("gid", aid),
                roleembeds,
                recp
        );

        System.out.printf("Member2 authorized agent role to %s, waiting for others to authorize...", eid1);

        // Member3 check for notifications and join the authorization
        msgSaid = waitAndMarkNotification(client3, "/multisig/rpy");
        System.out.println("Member3 received exchange message to join the end role authorization");

        res = client3.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(castObjectToListMap(res).getFirst().get("exn"));

        // stamp, eid and role are provided in the exn message

        rpystamp = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(
                        castObjectToLinkedHashMap(exn.get("e"))).get("rpy")).get("dt")
                .toString();

        rpyrole = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(
                        castObjectToLinkedHashMap(
                                castObjectToLinkedHashMap(
                                        exn.get("e"))).get("rpy")).get("a")).get("role")
                .toString();

        rpyeid = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(
                        castObjectToLinkedHashMap(
                                castObjectToLinkedHashMap(
                                        exn.get("e"))).get("rpy")).get("a")).get("eid")
                .toString();

        endRoleRes = client3
                .getIdentifier()
                .addEndRole("multisig", rpyrole, rpyeid, rpystamp);
        op3 = endRoleRes.op();
        rpy = endRoleRes.serder();
        sigs = endRoleRes.sigs();

        hab = client3.getIdentifier().get("multisig");
        mstate = Utils.toMap(hab.getState());
        seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", hab.getPrefix(),
                        "s", Utils.toMap(mstate.get("ee")).get("s"),
                        "d", Utils.toMap(mstate.get("ee")).get("d")
                )
        );

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        atc = roleims.substring(rpy.getSize());

        roleembeds = new LinkedHashMap<>();
        roleembeds.put("rpy", Arrays.asList(rpy, atc));

        recp = Stream.of(aid1.getState(), aid2.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client3.getExchanges().send(
                "member3",
                "multisig",
                aid3,
                "/multisig/rpy",
                Map.of("gid", aid),
                roleembeds,
                recp
        );

        System.out.printf("Member3 authorized agent role to %s, waiting for others to authorize...", eid1);

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
        System.out.printf("End role authorization for agent %s completed!", eid1);

        // Holder resolve multisig OOBI
        Object oobimultisig = client1.getOobis().get("multisig", "agent");
        op4 = client4.getOobis().resolve(getOobisIndexAt0(oobimultisig), "multisig");
        op4 = waitOperation(client4, op4);
        System.out.println("Holder resolved multisig OOBI");

        // MultiSig Interaction

        // Member1 initiates an interaction event
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("i", "EBgew7O4yp8SBle0FU-wwN3GtnaroI0BQfBGAj33QiIG");
        data.put("s", "0");
        data.put("d", "EBgew7O4yp8SBle0FU-wwN3GtnaroI0BQfBGAj33QiIG");
        EventResult eventResponse1 = client1.getIdentifier().interact("multisig", data);
        op1 = eventResponse1.op();
        serder = eventResponse1.serder();
        sigs = eventResponse1.sigs();
        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        Map<String, List<Object>> xembeds = new LinkedHashMap<>();
        xembeds.put("ixn", Arrays.asList(serder, atc));

        smids = states.stream()
                .map(state -> Utils.toMap(state).get("i").toString())
                .collect(Collectors.toList());
        recp = Stream.of(aid2.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client1.getExchanges().send(
                "member1",
                "multisig",
                aid1,
                "/multisig/ixn",
                Map.of(
                        "gid", serder.getPre(),
                        "smids", smids,
                        "rmids", smids
                ),
                xembeds,
                recp
        );
        System.out.println("Member1 initiates interaction event, waiting for others to join...");

        // Member2 check for notifications and join the interaction event
        msgSaid = waitAndMarkNotification(client2, "/multisig/ixn");
        System.out.println("Member2 received exchange message to join the interaction event");

        Object res1 = client2.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(
                castObjectToListMap(res1).getFirst().get("exn")
        );
        Map<String, Object> ixn = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(exn.get("e")).get("ixn")
        );
        data = castObjectToListMap(ixn.get("a")).getFirst();

        icpResult2 = client2.getIdentifier().interact("multisig", data);
        op2 = icpResult2.op();
        serder = icpResult2.serder();
        sigs = icpResult2.sigs();
        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        xembeds = new LinkedHashMap<>();
        xembeds.put("ixn", Arrays.asList(serder, atc));

        smids = Utils.toList(Utils.toMap(exn.get("a")).get("smids"));
        recp = Stream.of(aid1.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client2.getExchanges().send(
                "member2",
                "multisig",
                aid2,
                "/multisig/ixn",
                Map.of(
                        "gid", serder.getPre(),
                        "smids", smids,
                        "rmids", smids
                ),
                xembeds,
                recp
        );
        System.out.println("Member2 joins interaction event, waiting for others...");

        // Member3 check for notifications and join the interaction event
        msgSaid = waitAndMarkNotification(client3, "/multisig/ixn");
        System.out.println("Member3 received exchange message to join the interaction event");

        res = client3.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );
        ixn = castObjectToLinkedHashMap(
                castObjectToLinkedHashMap(exn.get("e")).get("ixn")
        );
        data = castObjectToListMap(ixn.get("a")).getFirst();

        icpResult3 = client3.getIdentifier().interact("multisig", data);
        op3 = icpResult3.op();
        serder = icpResult3.serder();
        sigs = icpResult3.sigs();
        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        xembeds = new LinkedHashMap<>();
        xembeds.put("ixn", Arrays.asList(serder, atc));

        smids = Utils.toList(Utils.toMap(exn.get("a")).get("smids"));
        recp = Stream.of(aid1.getState(), aid2.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client3.getExchanges().send(
                "member3",
                "multisig",
                aid3,
                "/multisig/ixn",
                Map.of(
                        "gid", serder.getPre(),
                        "smids", smids,
                        "rmids", smids
                ),
                xembeds,
                recp
        );
        System.out.println("Member3 joins interaction event, waiting for others...");

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
        System.out.println("Multisig interaction completed!");

        // Members agree out of band to rotate keys
        System.out.println("Members agree out of band to rotate keys");
        icpResult1 = client1.getIdentifier().rotate("member1");
        op1 = icpResult1.op();
        op1 = waitOperation(client1, op1);
        aid1 = client1.getIdentifier().get("member1");
        System.out.println("Member1 rotated keys");

        icpResult2 = client2.getIdentifier().rotate("member2");
        op2 = icpResult2.op();
        op2 = waitOperation(client2, op2);
        aid2 = client2.getIdentifier().get("member2");
        System.out.println("Member2 rotated keys");

        icpResult3 = client3.getIdentifier().rotate("member3");
        op3 = icpResult3.op();
        op3 = waitOperation(client3, op3);
        aid3 = client3.getIdentifier().get("member3");
        System.out.println("Member3 rotated keys");

        // Update new key states
        op1 = client1.getKeyStates().query(aid2.getPrefix(), "1");
        op1 = waitOperation(client1, op1);
        States.State aid2State = convertValueToStateClass(Utils.toMap(op1).get("response"));
        op1 = client1.getKeyStates().query(aid3.getPrefix(), "1");
        op1 = waitOperation(client1, op1);
        States.State aid3State = convertValueToStateClass(Utils.toMap(op1).get("response"));

        op2 = client2.getKeyStates().query(aid3.getPrefix(), "1");
        op2 = waitOperation(client2, op2);
        op2 = client2.getKeyStates().query(aid1.getPrefix(), "1");
        op2 = waitOperation(client2, op2);
        States.State aid1State = convertValueToStateClass(Utils.toMap(op2).get("response"));

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

        rstates = Arrays.asList(aid1State, aid2State, aid3State);
        states = rstates;

        // Multisig Rotation

        // Member1 initiates a rotation event
        eventResponse1 = client1.getIdentifier().rotate("multisig", RotateIdentifierArgs.builder()
                .states(states)
                .rstates(rstates)
                .build());
        op1 = eventResponse1.op();
        serder = eventResponse1.serder();
        sigs = eventResponse1.sigs();

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        Map<String, List<Object>> rembeds = new LinkedHashMap<>();
        rembeds.put("rot", Arrays.asList(serder, atc));

        smids = states.stream()
                .map(state -> Utils.toMap(state).get("i").toString())
                .collect(Collectors.toList());
        recp = Stream.of(aid2State, aid3State)
                .map(States.State::getI)
                .collect(Collectors.toList());

        Map<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("gid", serder.getPre());
        payload1.put("smids", smids);
        payload1.put("rmids", smids);

        client1.getExchanges().send(
                "member1",
                "multisig",
                aid1,
                "/multisig/rot",
                payload1,
                rembeds,
                recp
        );
        System.out.println("Member1 initiates rotation event, waiting for others to join...");

        // Member2 check for notifications and join the rotation event
        msgSaid = waitAndMarkNotification(client2, "/multisig/rot");
        System.out.println("Member2 received exchange message to join the rotation event");

        Thread.sleep(5000);
        res = client2.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );

        icpResult2 = client2.getIdentifier().rotate("multisig", RotateIdentifierArgs.builder()
                .states(states)
                .rstates(rstates)
                .build());
        op2 = icpResult2.op();
        serder = icpResult2.serder();
        sigs = icpResult2.sigs();
        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        rembeds = new LinkedHashMap<>();
        rembeds.put("rot", Arrays.asList(serder, atc));

        smids = Utils.toList(Utils.toMap(exn.get("a")).get("smids"));
        recp = Stream.of(aid1State, aid3State)
                .map(States.State::getI)
                .collect(Collectors.toList());

        Map<String, Object> payload2 = new LinkedHashMap<>();
        payload2.put("gid", serder.getPre());
        payload2.put("smids", smids);
        payload2.put("rmids", smids);

        client2.getExchanges().send(
                "member2",
                "multisig",
                aid2,
                "/multisig/ixn",
                payload2,
                rembeds,
                recp
        );
        System.out.println("Member2 joins rotation event, waiting for others...");

        // Member3 check for notifications and join the rotation event
        msgSaid = waitAndMarkNotification(client3, "/multisig/rot");
        System.out.println("Member3 received exchange message to join the rotation event");
        res = client3.getGroups().getRequest(msgSaid);
        exn = castObjectToLinkedHashMap(
                castObjectToListMap(res).getFirst().get("exn")
        );

        icpResult3 = client3.getIdentifier().rotate("multisig", RotateIdentifierArgs.builder()
                .states(states)
                .rstates(rstates)
                .build());
        op3 = icpResult3.op();
        serder = icpResult3.serder();
        sigs = icpResult3.sigs();
        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(serder, sigers));
        atc = ims.substring(serder.getSize());
        rembeds = new LinkedHashMap<>();
        rembeds.put("rot", Arrays.asList(serder, atc));

        smids = Utils.toList(Utils.toMap(exn.get("a")).get("smids"));
        recp = Stream.of(aid1State, aid2State)
                .map(States.State::getI)
                .collect(Collectors.toList());

        Map<String, Object> payload3 = new LinkedHashMap<>();
        payload3.put("gid", serder.getPre());
        payload3.put("smids", smids);
        payload3.put("rmids", smids);

        client3.getExchanges().send(
                "member3",
                "multisig",
                aid3,
                "/multisig/ixn",
                payload3,
                rembeds,
                recp
        );
        System.out.println("Member3 joins rotation event, waiting for others...");

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
        System.out.println("Multisig rotation completed!");

        hab = client1.getIdentifier().get("multisig");
        aid = hab.getPrefix();

        // Multisig Registry creation
        aid1 = client1.getIdentifier().get("member1");
        aid2 = client2.getIdentifier().get("member2");
        aid3 = client3.getIdentifier().get("member3");
        System.out.println("Starting multisig registry creation");

        RegistryResult vcpRes1 = client1.getRegistries().create(CreateRegistryArgs.builder()
                .name("multisig")
                .registryName("vLEI Registry")
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .build());
        op1 = vcpRes1.op();
        serder = vcpRes1.getRegser();
        String regk = serder.getPre();
        Serder anc = vcpRes1.getSerder();
        sigs = vcpRes1.getSigs();

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(anc, sigers));
        atc = ims.substring(anc.getSize());
        Map<String, List<Object>> regbeds = new LinkedHashMap<>();
        regbeds.put("vcp", Arrays.asList(serder, ""));
        regbeds.put("anc", Arrays.asList(anc, atc));

        recp = Stream.of(aid2.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client1.getExchanges().send(
                "member1",
                "registry",
                aid1,
                "/multisig/vcp",
                Map.of(
                        "gid", multisig,
                        "usage", "Issue vLEIs"
                ),
                regbeds,
                recp
        );

        System.out.println("Member1 initiated registry, waiting for others to join...");

        // Member2 check for notifications and join the create registry event
        msgSaid = waitAndMarkNotification(client2, "/multisig/vcp");
        System.out.println("Member2 received exchange message to join the create registry event");

        res = client2.getGroups().getRequest(msgSaid);
        exn = Utils.toMap(Utils.toMap(Utils.toList(res).getFirst()).get("exn"));

        RegistryResult vcpRes2 = client2.getRegistries().create(CreateRegistryArgs.builder()
                .name("multisig")
                .registryName("vLEI Registry")
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .build());
        op2 = vcpRes2.op();
        serder = vcpRes2.getRegser();
        anc = vcpRes2.getSerder();
        sigs = vcpRes2.getSigs();

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(anc, sigers));
        atc = ims.substring(anc.getSize());
        regbeds = new LinkedHashMap<>();
        regbeds.put("vcp", Arrays.asList(serder, ""));
        regbeds.put("anc", Arrays.asList(anc, atc));

        recp = Stream.of(aid1.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client2.getExchanges().send(
                "member2",
                "registry",
                aid2,
                "/multisig/vcp",
                Map.of(
                        "gid", multisig,
                        "usage", "Issue vLEIs"
                ),
                regbeds,
                recp
        );
        System.out.println("Member2 joins registry event, waiting for others...");

        // Member3 check for notifications and join the create registry event
        msgSaid = waitAndMarkNotification(client3, "/multisig/vcp");
        System.out.println("Member3 received exchange message to join the create registry event");

        res = client3.getGroups().getRequest(msgSaid);
        exn = Utils.toMap(Utils.toMap(Utils.toList(res).getFirst()).get("exn"));

        RegistryResult vcpRes3 = client3.getRegistries().create(CreateRegistryArgs.builder()
                .name("multisig")
                .registryName("vLEI Registry")
                .nonce("AHSNDV3ABI6U8OIgKaj3aky91ZpNL54I5_7-qwtC6q2s")
                .build());
        op3 = vcpRes3.op();
        serder = vcpRes3.getRegser();
        anc = vcpRes3.getSerder();
        sigs = vcpRes3.getSigs();

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        ims = new String(Eventing.messagize(anc, sigers));
        atc = ims.substring(anc.getSize());
        regbeds = new LinkedHashMap<>();
        regbeds.put("vcp", Arrays.asList(serder, ""));
        regbeds.put("anc", Arrays.asList(anc, atc));

        recp = Stream.of(aid1.getState(), aid2.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        client3.getExchanges().send(
                "member3",
                "multisig",
                aid3,
                "/multisig/vcp",
                Map.of(
                        "gid", multisig,
                        "usage", "Issue vLEIs"
                ),
                regbeds,
                recp
        );

        // Done
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
        System.out.println("Multisig create registry completed!");

        // Create Credential
        System.out.println("Starting multisig credential creation");

        Map<String, Object> vcdata = Map.of(
                "LEI", "5493001KJTIIGC8Y1R17"
        );
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
        msgSaid = waitAndMarkNotification(client2, "/multisig/iss");
        System.out.println("Member2 received exchange message to join the credential create event");
        res = client2.getGroups().getRequest(msgSaid);
        exn = Utils.toMap(Utils.toMap(Utils.toList(res).getFirst()).get("exn"));

        String credentialSaid = Utils.toMap(Utils.toMap(exn.get("e")).get("acdc")).get("d").toString();
        Map<String, Object> acdcMap = Utils.toMap(Utils.toMap(exn.get("e")).get("acdc"));
        CredentialData credentialData2 = objectMapper.convertValue(acdcMap, CredentialData.class);
        IssueCredentialResult credRes2 = client2.getCredentials().issue("multisig", credentialData2);

        op2 = credRes2.getOp();
        multisigIssue(client2, "member2", "multisig", credRes2);
        System.out.println("Member2 joins credential create event, waiting for others...");

        // Member3 check for notifications and join the create registry event
        msgSaid = waitAndMarkNotification(client3, "/multisig/iss");
        System.out.println("Member3 received exchange message to join the credential create event");
        res = client3.getGroups().getRequest(msgSaid);
        exn = Utils.toMap(Utils.toMap(Utils.toList(res).getFirst()).get("exn"));

        acdcMap = Utils.toMap(Utils.toMap(exn.get("e")).get("acdc"));
        CredentialData credentialData3 = objectMapper.convertValue(acdcMap, CredentialData.class);
        IssueCredentialResult credRes3 = client3.getCredentials().issue("multisig", credentialData3);

        op3 = credRes3.getOp();
        multisigIssue(client3, "member3", "multisig", credRes3);
        System.out.println("Member3 joins credential create event, waiting for others...");

        // Check completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
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
        stamp = new Date().toInstant().toString().replace("Z", "000+00:00");

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

        client1.getIpex().submitGrant("multisig", grant, gsigs, end, List.of(holder));

        mstate = Utils.toMap(m.getState());
        seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", m.getPrefix(),
                        "s", Utils.toMap(mstate.get("ee")).get("s"),
                        "d", Utils.toMap(mstate.get("ee")).get("d")
                )
        );
        sigers = gsigs.stream()
                .map(Siger::new)
                .collect(Collectors.toList());

        String gims = new String(Eventing.messagize(grant, sigers, seal));
        atc = gims.substring(grant.getSize());
        atc += end;
        Map<String, List<Object>> gembeds = new LinkedHashMap<>();
        gembeds.put("exn", Arrays.asList(grant, atc));

        recp = Stream.of(aid2.getState(), aid3.getState())
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
        exn = Utils.toMap(Utils.toMap(Utils.toList(res).getFirst()).get("exn"));

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
        exn = Utils.toMap(Utils.toMap(Utils.toList(res).getFirst()).get("exn"));

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

        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
        op4 = waitOperation(client4, op4);

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
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        op3 = waitOperation(client3, op3);
        System.out.println("Multisig credential revocation completed!");
    }

    public States.HabState createAid(SignifyClient client, String name, List<String> wits) throws Exception {
        CreateIdentifierArgs iargs = new CreateIdentifierArgs();
        iargs.setWits(wits);
        iargs.setToad(wits.size());
        getOrCreateIdentifier(client, name, iargs);
        return client.getIdentifier().get(name);
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

    public States.State convertValueToStateClass(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<>() {
        });
    }
}
