package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Siger;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.AidRecord;
import org.cardanofoundation.signify.generated.keria.model.GroupMember;
import org.cardanofoundation.signify.generated.keria.model.Identifier;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultisigJoinTest extends BaseIntegrationTest {
    private static SignifyClient client1, client2, client3;

    Identifier aid1, aid2, aid3;
    static String nameMember1 = "member1";
    static String nameMember2 = "member2";
    static String nameMember3 = "member3";
    static String nameMultisig = "multisigGroup";
    static String oobi1, oobi2, oobi3, oobiMultisig;
    private static Map<String, Object> oobiGetMultisig;

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> signifyClients = getOrCreateClientsAsync(3);
        client1 = signifyClients.get(0);
        client2 = signifyClients.get(1);
        client3 = signifyClients.get(2);

        createAID(client1, nameMember1, new ArrayList<>());
        createAID(client2, nameMember2, new ArrayList<>());

        List<Object> oobis = getOobisAsync(
            new GetOobisArgs(client1, nameMember1, "agent"),
            new GetOobisArgs(client2, nameMember2, "agent")
        );
        oobi1 = getOobisIndexAt0(oobis.get(0));
        oobi2 = getOobisIndexAt0(oobis.get(1));

        resolveOobisAsync(
            new ResolveOobisArgs(client1, oobi2, nameMember2),
            new ResolveOobisArgs(client2, oobi1, nameMember1)
        );
    }

    @Test
    @Order(1)
    public void multisigJoinTest() throws Exception {
        List<Identifier> aids = createAidAndGetHabStateAsync(
            new CreateAidArgs(client1, nameMember1),
            new CreateAidArgs(client2, nameMember2)
        );
        aid1 = aids.get(0);
        aid2 = aids.get(1);

        List<KeyStateRecord> states = Arrays.asList(aid1.getState(), aid2.getState());
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.group);
        kargs.setMhab(aid1);
        kargs.setIsith(1);
        kargs.setNsith(1);
        kargs.setToad(aid1.getState().getB().size());
        kargs.setWits(aid1.getState().getB());
        kargs.setStates(states);
        kargs.setRstates(states);

        EventResult icpResult = client1.identifiers().create(nameMultisig, kargs);

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

        client1.exchanges().send(
            nameMember1,
            nameMultisig,
            aid1,
            "/multisig/icp",
            payload,
            embeds,
            recipients
        );

        String msgSaid = TestUtils.waitAndMarkNotification(client2, "/multisig/icp");
        Object response = client2.groups().getRequest(msgSaid).get();
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
        iargs2.setStates(states);
        iargs2.setRstates(states);

        EventResult icpResult2 = client2.identifiers().create(nameMultisig, iargs2);

        Object createMultisig2 = icpResult2.op();

        List<Operation> op = waitOperationAsync(
            new WaitOperationArgs(client1, createMultisig1),
            new WaitOperationArgs(client2, createMultisig2)
        );

        createMultisig1 = op.get(0);
        createMultisig2 = op.get(1);

        Map<String, Object> multisigRes1 = castObjectToLinkedHashMap(
            Utils.toMap(createMultisig1).get("response"));
        Map<String, Object> multisigRes2 = castObjectToLinkedHashMap(
            Utils.toMap(createMultisig2).get("response"));

        assertEquals(aid1.getState().getK().getFirst(), Utils.toList(multisigRes1.get("k")).getFirst());
        assertEquals(aid2.getState().getK().getFirst(), Utils.toList(multisigRes1.get("k")).get(1));
        assertEquals(aid1.getState().getK().getFirst(), Utils.toList(multisigRes2.get("k")).getFirst());
        assertEquals(aid2.getState().getK().getFirst(), Utils.toList(multisigRes2.get("k")).get(1));

        GroupMember membersAgent1 = client1.identifiers().members(nameMultisig);
        GroupMember membersAgent2 = client2.identifiers().members(nameMultisig);

        List<AidRecord> signing1 = membersAgent1.getSigning();
        String eid1 = Utils.toList(signing1.getFirst().getEnds().getAgent().keySet()).getFirst();

        List<AidRecord> signing2 = membersAgent2.getSigning();
        String eid2 = Utils.toList(signing2.getFirst().getEnds().getAgent().keySet()).getFirst();

        EventResult endRoleOperation1 = client1.identifiers().addEndRole(nameMultisig, "agent", eid1, null);
        EventResult endRoleOperation2 = client2.identifiers().addEndRole(nameMultisig, "agent", eid2, null);

        oobiGetMultisig = new LinkedHashMap<>();
        oobiGetMultisig = (Map<String, Object>) client1.oobis().get(nameMultisig, "agent").get();

        waitOperationAsync(
            new WaitOperationArgs(client1, endRoleOperation1.op()),
            new WaitOperationArgs(client2, endRoleOperation2.op())
        );
    }

    @Test
    @Order(2)
    public void multisigJoinTestAddMember3() throws Exception {
        client3 = getOrCreateClient();

        aid3 = createAID(client3, nameMember3, new ArrayList<>());

        List<Object> oobis = getOobisAsync(
            new GetOobisArgs(client1, nameMember1, "agent"),
            new GetOobisArgs(client2, nameMember2, "agent"),
            new GetOobisArgs(client3, nameMember3, "agent")
        );

        oobi3 = getOobisIndexAt0(oobis.get(2));
        oobiMultisig = getOobisIndexAt0(oobiGetMultisig);

        resolveOobisAsync(
            new ResolveOobisArgs(client1, oobi3, nameMember3),
            new ResolveOobisArgs(client2, oobi3, nameMember3),
            new ResolveOobisArgs(client3, oobi1, nameMember1),
            new ResolveOobisArgs(client3, oobi2, nameMember2),
            new ResolveOobisArgs(client3, oobiMultisig, nameMultisig)
        );

        EventResult rotateResult1 = client1.identifiers().rotate(nameMember1);
        EventResult rotateResult2 = client2.identifiers().rotate(nameMember2);

        waitOperationAsync(
            new WaitOperationArgs(client1, rotateResult1.op()),
            new WaitOperationArgs(client2, rotateResult2.op())
        );

        aid1 = client1.identifiers().get(nameMember1).get();
        aid2 = client2.identifiers().get(nameMember2).get();

        List<Object> updates = getKeyStateQuerAsync(
            new GetKeyStateQueryArgs(client1, aid2.getPrefix(), "1"),
            new GetKeyStateQueryArgs(client1, aid3.getPrefix(), "0"),
            new GetKeyStateQueryArgs(client2, aid1.getPrefix(), "1"),
            new GetKeyStateQueryArgs(client2, aid3.getPrefix(), "0"),
            new GetKeyStateQueryArgs(client3, aid1.getPrefix(), "1"),
            new GetKeyStateQueryArgs(client3, aid2.getPrefix(), "1")

        );

        List<Operation> statesUpdate = waitOperationAsync(
            new WaitOperationArgs(client1, updates.get(0)),
            new WaitOperationArgs(client1, updates.get(1)),
            new WaitOperationArgs(client2, updates.get(2)),
            new WaitOperationArgs(client2, updates.get(3)),
            new WaitOperationArgs(client3, updates.get(4)),
            new WaitOperationArgs(client3, updates.get(5))
        );
        Object aid2States = statesUpdate.get(0);
        Object aid1States = statesUpdate.get(2);
        Object aid3States = statesUpdate.get(1);

        KeyStateRecord aid2State = Utils.fromJson(Utils.jsonStringify(Operation.fromObject(aid2States).getResponse()), KeyStateRecord.class);
        KeyStateRecord aid1State = Utils.fromJson(Utils.jsonStringify(Operation.fromObject(aid1States).getResponse()), KeyStateRecord.class);
        KeyStateRecord aid3State = Utils.fromJson(Utils.jsonStringify(Operation.fromObject(aid3States).getResponse()), KeyStateRecord.class);

        List<KeyStateRecord> states = Arrays.asList(aid1State, aid2State);
        List<KeyStateRecord> rstates = new ArrayList<>(states);
        rstates.add(aid3State);

        EventResult rotateOperation1 = client1.identifiers().rotate(nameMultisig, RotateIdentifierArgs.builder()
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
            .map(KeyStateRecord::getI)
            .collect(Collectors.toList());

        Map<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("gid", serder1.getPre());
        payload1.put("smids", smids);
        payload1.put("rmids", rmids);

        client1.exchanges().send(
            nameMember1,
            nameMultisig,
            aid1,
            "/multisig/rot",
            payload1,
            rembeds,
            recp
        );

        TestUtils.waitAndMarkNotification(client2, "/multisig/rot");
        TestUtils.waitAndMarkNotification(client3, "/multisig/rot");

        Identifier multiSigAid = client1.identifiers().get(nameMultisig).get();

        assertEquals(2, multiSigAid.getState().getK().size());
        assertEquals(aid1.getState().getK().getFirst(), multiSigAid.getState().getK().getFirst());
        assertEquals(aid2.getState().getK().getFirst(), multiSigAid.getState().getK().get(1));

        assertEquals(3, multiSigAid.getState().getN().size());
        assertEquals(aid1.getState().getN().getFirst(), multiSigAid.getState().getN().getFirst());
        assertEquals(aid2.getState().getN().getFirst(), multiSigAid.getState().getN().get(1));
        assertEquals(aid3.getState().getN().getFirst(), multiSigAid.getState().getN().get(2));
    }

    @Test
    @Order(3)
    public void signingKeysAndJoinTest() throws Exception {
        EventResult rotateResult1 = client1.identifiers().rotate(nameMember1);
        EventResult rotateResult2 = client2.identifiers().rotate(nameMember2);
        EventResult rotateResult3 = client3.identifiers().rotate(nameMember3);

        waitOperationAsync(
            new WaitOperationArgs(client1, rotateResult1.op()),
            new WaitOperationArgs(client2, rotateResult2.op()),
            new WaitOperationArgs(client3, rotateResult3.op())
        );

        aid1 = client1.identifiers().get(nameMember1).get();
        aid2 = client2.identifiers().get(nameMember2).get();
        aid3 = client3.identifiers().get(nameMember3).get();

        List<Object> updates = getKeyStateQuerAsync(
            new GetKeyStateQueryArgs(client1, aid2.getPrefix(), "2"),
            new GetKeyStateQueryArgs(client1, aid3.getPrefix(), "1"),
            new GetKeyStateQueryArgs(client2, aid1.getPrefix(), "2"),
            new GetKeyStateQueryArgs(client2, aid3.getPrefix(), "1"),
            new GetKeyStateQueryArgs(client3, aid1.getPrefix(), "2"),
            new GetKeyStateQueryArgs(client3, aid2.getPrefix(), "2")

        );

        List<Operation> statesUpdate = waitOperationAsync(
            new WaitOperationArgs(client1, updates.get(0)),
            new WaitOperationArgs(client1, updates.get(1)),
            new WaitOperationArgs(client2, updates.get(2)),
            new WaitOperationArgs(client2, updates.get(3)),
            new WaitOperationArgs(client3, updates.get(4)),
            new WaitOperationArgs(client3, updates.get(5))
        );
        Object aid2States = statesUpdate.get(0);
        Object aid1States = statesUpdate.get(2);
        Object aid3States = statesUpdate.get(1);

        KeyStateRecord aid2State = Utils.fromJson(Utils.jsonStringify(Operation.fromObject(aid2States).getResponse()), KeyStateRecord.class);
        KeyStateRecord aid1State = Utils.fromJson(Utils.jsonStringify(Operation.fromObject(aid1States).getResponse()), KeyStateRecord.class);
        KeyStateRecord aid3State = Utils.fromJson(Utils.jsonStringify(Operation.fromObject(aid3States).getResponse()), KeyStateRecord.class);

        List<KeyStateRecord> states = Arrays.asList(aid1State, aid2State, aid3State);

        EventResult rotateOperation1 = client1.identifiers().rotate(nameMultisig, RotateIdentifierArgs.builder()
            .states(states)
            .rstates(states)
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

        List<String> rmids = states.stream()
            .map(state -> Utils.toMap(state).get("i").toString())
            .collect(Collectors.toList());

        List<String> recp = Stream.of(aid2.getState(), aid3.getState())
            .map(KeyStateRecord::getI)
            .collect(Collectors.toList());

        Map<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("gid", serder1.getPre());
        payload1.put("smids", smids);
        payload1.put("rmids", rmids);

        client1.exchanges().send(
            nameMember1,
            nameMultisig,
            aid1,
            "/multisig/rot",
            payload1,
            rembeds,
            recp
        );

        String rotationNotification3 = TestUtils.waitAndMarkNotification(client3, "/multisig/rot");
        Object response = client3.groups().getRequest(rotationNotification3).get();

        Map<String, Object> exn3 = castObjectToLinkedHashMap(
            castObjectToListMap(response).getFirst().get("exn")
        );
        Map<String, Object> op1Response = Utils.toMap(exn3.get("e"));
        Map<String, Object> exnValue = Utils.toMap(op1Response.get("rot"));
        Serder serder3 = new Serder(exnValue);

        Keeping.Keeper<?> keeper3 = client3.getManager().get(aid3);
        List<String> sig3 = keeper3.sign(serder3.getRaw().getBytes()).signatures();

        Object joinOperation = client3.groups()
            .join(
                nameMultisig,
                serder3,
                sig3,
                Utils.toMap(exn3.get("a")).get("gid").toString(),
                smids,
                rmids
            );

        waitOperation(client3, joinOperation);

        Identifier multiSigAid = client3.identifiers().get(nameMultisig).get();

        assertEquals(3, multiSigAid.getState().getK().size());
        assertEquals(aid1.getState().getK().getFirst(), multiSigAid.getState().getK().getFirst());
        assertEquals(aid2.getState().getK().getFirst(), multiSigAid.getState().getK().get(1));
        assertEquals(aid3.getState().getK().getFirst(), multiSigAid.getState().getK().get(2));

        assertEquals(3, multiSigAid.getState().getN().size());
        assertEquals(aid1.getState().getN().getFirst(), multiSigAid.getState().getN().getFirst());
        assertEquals(aid2.getState().getN().getFirst(), multiSigAid.getState().getN().get(1));
        assertEquals(aid3.getState().getN().getFirst(), multiSigAid.getState().getN().get(2));

        GroupMember members = client3.identifiers().members(nameMultisig);

        List<AidRecord> signing3 = members.getSigning();
        String eid = Utils.toList(signing3.get(2).getEnds().getAgent().keySet()).getFirst();

        EventResult endRoleOperation = client3.identifiers().addEndRole(nameMultisig, "agent", eid, null);
        Object endRoleResult = waitOperation(client3, endRoleOperation.op());

        assertEquals("true", Utils.toMap(endRoleResult).get("done").toString());
        assertNull(Utils.toMap(endRoleResult).get("error"));
    }

    public static Identifier createAID(SignifyClient client, String name, List<String> wits) throws Exception {
        CreateIdentifierArgs iargs = new CreateIdentifierArgs();
        iargs.setWits(wits);
        iargs.setToad(wits.size());
        TestUtils.getOrCreateIdentifier(client, name, iargs);
        return client.identifiers().get(name).get();
    }

    public static String getOobisIndexAt0(Object oobi) {
        Map<String, Object> oobiBody = Utils.toMap(oobi);
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }
}