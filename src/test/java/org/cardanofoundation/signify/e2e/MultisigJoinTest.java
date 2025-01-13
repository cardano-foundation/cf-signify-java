package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Siger;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultisigJoinTest extends TestUtils {
    private static SignifyClient client1, client2, client3;

    States.HabState aid1, aid2;
    static String nameMember1 = "member1";
    static String nameMember2 = "member2";
    static String nameMember3 = "member3";
    static String nameMultisig = "multisigGroup";
    static Object oobi1, oobi2, oobi3, oobi4;
    static Object opOobi1, opOobi2, opOobi3, opOobi4, opOobi5;
    static String oobis1, oobis2, oobis3, oobiMultisig;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void getClients() throws Exception {
        client1 = getOrCreateClient(null);
        client2 = getOrCreateClient(null);
        client3 = getOrCreateClient(null);

        createAID(client1, nameMember1, new ArrayList<>());
        createAID(client2, nameMember2, new ArrayList<>());

        oobi1 = client1.getOobis().get(nameMember1, "agent");
        oobi2 = client2.getOobis().get(nameMember2, "agent");

        oobis1 = getOobisIndexAt0(oobi1);
        oobis2 = getOobisIndexAt0(oobi2);

        opOobi1 = client1.getOobis().resolve(oobis2, nameMember2);
        opOobi2 = client2.getOobis().resolve(oobis1, nameMember1);

        waitOperation(client1, opOobi1);
        waitOperation(client2, opOobi2);
    }

    @Disabled
    @Test
    public void multisigJoinTest() throws Exception {
        aid1 = client1.getIdentifier().get(nameMember1);
        aid2 = client2.getIdentifier().get(nameMember2);

        List<States.State> states = Arrays.asList(aid1.getState(), aid2.getState());
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.group);
        kargs.setMhab(aid1);
        kargs.setIsith(1);
        kargs.setNsith(1);
        kargs.setToad(aid1.getState().getB().size());
        kargs.setWits(aid1.getState().getB());
        kargs.setStates(Collections.singletonList(states));
        kargs.setRstates(Collections.singletonList(states));

        EventResult icpResult = client1.getIdentifier().create(nameMultisig, kargs);

        Object createMultisig1 = icpResult.op();
        Serder serder = icpResult.serder();
        List<String> sigs = icpResult.sigs();
        List<Siger> sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        String ims = new String(Eventing.messagize(serder, sigers));
        String atc = ims.substring(serder.getSize());
        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("icp", Arrays.asList(serder, atc));

        List<String> smids = Collections.singletonList(aid2.getState().getI());

        List<String> recipients = Collections.singletonList(aid2.getState().getI());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", serder.getPre());
        payload.put("smids", smids);
        payload.put("rmids", smids);

        client1.getExchanges().send(
                nameMember1,
                nameMultisig,
                aid1,
                "/multisig/icp",
                payload,
                embeds,
                recipients
        );

        String msgSaid = TestUtils.waitAndMarkNotification(client2, "/multisig/icp");
        Object response = client2.getGroups().getRequest(msgSaid);
        Map<String, Object> exn = castObjectToLinkedHashMap(
                castObjectToListMap(response).getFirst().get("exn")
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
        iargs2.setRstates(Collections.singletonList(states));

        EventResult icpResult2 = client2.getIdentifier().create(nameMultisig, iargs2);

        Object createMultisig2 = icpResult2.op();

        createMultisig1 = waitOperation(client1, createMultisig1);
        createMultisig2 = waitOperation(client2, createMultisig2);

        Map<String, Object> multisigRes1 = castObjectToLinkedHashMap(
                Utils.toMap(createMultisig1).get("response"));
        Map<String, Object> multisigRes2 = castObjectToLinkedHashMap(
                Utils.toMap(createMultisig2).get("response"));

        assertEquals(aid1.getState().getK().getFirst(), Utils.toList(multisigRes1.get("k")).getFirst());
        assertEquals(aid2.getState().getK().getFirst(), Utils.toList(multisigRes1.get("k")).get(1));
        assertEquals(aid1.getState().getK().getFirst(), Utils.toList(multisigRes2.get("k")).getFirst());
        assertEquals(aid2.getState().getK().getFirst(), Utils.toList(multisigRes2.get("k")).get(1));

        Map<String, Object> membersAgent1 = (Map<String, Object>) client1.getIdentifier().members(nameMultisig);
        Map<String, Object> membersAgent2 = (Map<String, Object>) client2.getIdentifier().members(nameMultisig);

        List<Map<String, Object>> signing1 = (List<Map<String, Object>>) Utils.toMap(membersAgent1).get("signing");
        String eid1 = Utils.toList(Utils.toMap(Utils.toMap(signing1.getFirst().get("ends")).get("agent")).keySet()).getFirst();

        List<Map<String, Object>> signing2 = (List<Map<String, Object>>) Utils.toMap(membersAgent2).get("signing");
        String eid2 = Utils.toList(Utils.toMap(Utils.toMap(signing2.getFirst().get("ends")).get("agent")).keySet()).getFirst();

        EventResult endRoleOperation1 = client1.getIdentifier().addEndRole(nameMultisig, "agent", eid1, null);
        EventResult endRoleOperation2 = client2.getIdentifier().addEndRole(nameMultisig, "agent", eid2, null);

        waitOperation(client1, endRoleOperation1.op());
        waitOperation(client2, endRoleOperation2.op());
    }

    @Disabled
    @Test
    public void multisigJoinTestAddMember3() throws Exception {
        client3 = getOrCreateClient();

        States.HabState aid3 = createAID(client3, nameMember3, new ArrayList<>());

        oobi1 = client1.getOobis().get(nameMember1, "agent");
        oobi2 = client2.getOobis().get(nameMember2, "agent");
        oobi3 = client3.getOobis().get(nameMember3, "agent");
        oobi4 = client1.getOobis().get(nameMultisig, "agent");

        oobis3 = getOobisIndexAt0(oobi3);
        oobiMultisig = getOobisIndexAt0(oobi4);

        opOobi1 = client1.getOobis().resolve(oobis3, nameMember3);
        opOobi2 = client2.getOobis().resolve(oobis3, nameMember3);
        opOobi3 = client3.getOobis().resolve(oobis1, nameMember1);
        opOobi4 = client3.getOobis().resolve(oobis2, nameMember2);
        opOobi5 = client3.getOobis().resolve(oobiMultisig, nameMultisig);

        waitOperation(client1, opOobi1);
        waitOperation(client2, opOobi2);
        waitOperation(client3, opOobi3);
        waitOperation(client3, opOobi4);
        waitOperation(client3, opOobi5);

        EventResult rotateResult1 = client1.getIdentifier().rotate(nameMember1);
        EventResult rotateResult2 = client2.getIdentifier().rotate(nameMember2);

        waitOperation(client1, rotateResult1.op());
        waitOperation(client2, rotateResult2.op());

        aid1 = client1.getIdentifier().get(nameMember1);
        aid2 = client2.getIdentifier().get(nameMember2);

        List<Object> updates = Arrays.asList(
                client1.getKeyStates().query(aid2.getPrefix(), "1"),
                client1.getKeyStates().query(aid3.getPrefix(), "0"),
                client2.getKeyStates().query(aid1.getPrefix(), "1"),
                client2.getKeyStates().query(aid3.getPrefix(), "0"),
                client3.getKeyStates().query(aid1.getPrefix(), "1"),
                client3.getKeyStates().query(aid2.getPrefix(), "1")
        );

        Object aid2States = waitOperation(client1, updates.get(0));
        States.State aid2State = convertValueToStateClass(Utils.toMap(aid2States).get("response"));
        waitOperation(client1, updates.get(1));
        Object aid1States = waitOperation(client2, updates.get(2));
        States.State aid1State = convertValueToStateClass(Utils.toMap(aid1States).get("response"));
        waitOperation(client2, updates.get(3));
        Object aid3States = waitOperation(client3, updates.get(4));
        States.State aid3State = convertValueToStateClass(Utils.toMap(aid3States).get("response"));
        waitOperation(client3, updates.get(5));

        List<States.State> states = Arrays.asList(aid1State, aid2State);
        List<States.State> rstates = new ArrayList<>(states);
        rstates.add(aid3State);

        EventResult rotateOperation1 = client1.getIdentifier().rotate(nameMultisig, RotateIdentifierArgs.builder()
                .states(states)
                .rstates(rstates)
                .build());

        Serder serder1 = rotateOperation1.serder();
        List<String> sigs = rotateOperation1.sigs();
        List<Siger> sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        String ims = new String(Eventing.messagize(serder1, sigers));
        String atc = ims.substring(serder1.getSize());
        Map<String, List<Object>> rembeds = new LinkedHashMap<>();
        rembeds.put("rot", Arrays.asList(serder1, atc));

        List<String> smids = states.stream()
                .map(state -> Utils.toMap(state).get("i").toString())
                .collect(Collectors.toList());

        List<String> rmids = rstates.stream()
                .map(state -> Utils.toMap(state).get("i").toString())
                .collect(Collectors.toList());

        List<String> recp = Stream.of(aid2.getState(), aid3.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        Map<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("gid", serder1.getPre());
        payload1.put("smids", smids);
        payload1.put("rmids", rmids);

        client1.getExchanges().send(
                nameMember1,
                nameMultisig,
                aid1,
                "/multisig/rot",
                payload1,
                rembeds,
                recp
        );

        waitAndMarkNotification(client2, "/multisig/rot");
        waitAndMarkNotification(client3, "/multisig/rot");

        States.HabState multiSigAid = client1.getIdentifier().get(nameMultisig);

        assertEquals(2, multiSigAid.getState().getK().size());
        assertEquals(aid1.getState().getK().getFirst(), multiSigAid.getState().getK().getFirst());
        assertEquals(aid2.getState().getK().getFirst(), multiSigAid.getState().getK().get(1));

        assertEquals(3, multiSigAid.getState().getN().size());
        assertEquals(aid1.getState().getN().getFirst(), multiSigAid.getState().getN().getFirst());
        assertEquals(aid2.getState().getN().getFirst(), multiSigAid.getState().getN().get(1));
        assertEquals(aid3.getState().getN().getFirst(), multiSigAid.getState().getN().get(2));
    }

    public static States.HabState createAID(SignifyClient client, String name, List<String> wits) throws Exception {
        CreateIdentifierArgs iargs = new CreateIdentifierArgs();
        iargs.setWits(wits);
        iargs.setToad(wits.size());
        TestUtils.getOrCreateIdentifier(client, name, iargs);
        return client.getIdentifier().get(name);
    }

    public static String getOobisIndexAt0(Object oobi) {
        Map<String, Object> oobiBody = Utils.toMap(oobi);
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }

    public States.State convertValueToStateClass(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<>() {
        });
    }
}
