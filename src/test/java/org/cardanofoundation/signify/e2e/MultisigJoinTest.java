package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Siger;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultisigJoinTest extends BaseIntegrationTest {
    private static SignifyClient client1, client2, client3;

    States.HabState aid1, aid2;
    static String nameMember1 = "member1";
    static String nameMember2 = "member2";
    static String nameMember3 = "member3";
    String nameMultisig = "multisigGroup";
    static Object oobi1, oobi2, opOobi1, opOobi2;
    private static String oobiRes;

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> clients = getOrCreateClientsAsync(3);
        client1 = clients.get(0);
        client2 = clients.get(1);
        client3 = clients.get(2);

        createAID(client1, nameMember1, new ArrayList<>());
        createAID(client2, nameMember2, new ArrayList<>());

        oobi1 = client1.getOobis().get(nameMember1, "agent");
        oobi2 = client2.getOobis().get(nameMember2, "agent");

        opOobi1 = client1.getOobis().resolve(getOobisIndexAt0(oobi2), nameMember2);
        opOobi2 = client2.getOobis().resolve(getOobisIndexAt0(oobi1), nameMember1);

        waitOperationAsync(
                new WaitOperationArgs(client1, opOobi1),
                new WaitOperationArgs(client2, opOobi2)
        );
    }

    @Test
    @DisplayName("Multisig Join Test")
    void multisigJoinTest() throws Exception {
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
        EventResult icpResult = client1.getIdentifier().create("multisigGroup", kargs);

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

        List<String> smids = Stream.of(aid2.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

        List<String> recipients = Stream.of(aid2.getState())
                .map(States.State::getI)
                .collect(Collectors.toList());

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
        List<HashMap<String, Object>> listRes = (List<HashMap<String, Object>>) response;
        Map<String, Object> resMap = listRes.getFirst();
        Map<String, Object> exn = (Map<String, Object>) resMap.get("exn");

        Map<String, Object> icp = (Map<String, Object>) Utils.toMap(exn.get("e")).get("icp");

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

        Operation<Object> operation1 = waitAndResolve(createMultisig1);
        Operation<Object> operation2 = waitAndResolve(createMultisig2);

        List<String> multisigKeys1 = extractMultisigKeys(operation1);
        List<String> multisigKeys2 = extractMultisigKeys(operation2);

        assertEquals(aid1.getState().getK().getFirst(), multisigKeys1.getFirst());
        assertEquals(aid2.getState().getK().getFirst(), multisigKeys1.get(1));
        assertEquals(aid1.getState().getK().getFirst(), multisigKeys2.getFirst());
        assertEquals(aid2.getState().getK().getFirst(), multisigKeys1.get(1));

        Map<String, Object> membersAgent1 = extractSigningAgent(client1.getIdentifier().members(nameMultisig));
        Map<String, Object> membersAgent2 = extractSigningAgent(client2.getIdentifier().members(nameMultisig));

        String eid1 = membersAgent1.keySet().iterator().next();
        String eid2 = membersAgent2.keySet().iterator().next();

        EventResult endRoleOperation1 = client1.getIdentifier().addEndRole(nameMultisig, "agent", eid1, null);
        EventResult endRoleOperation2 = client2.getIdentifier().addEndRole(nameMultisig, "agent", eid2, null);

        waitOperation(client1, endRoleOperation1.op());
        waitOperation(client2, endRoleOperation2.op());
    }

    @Test
    @DisplayName("Should Add Member3 To Multisig")
    void multisigJoinTestAddMember3() throws Exception {
//        client3 = getOrCreateClient();

        States.HabState aid3 = createAID(client3, nameMember3, new ArrayList<>());
        List<Object> oobis = getOobisAsync(
                new GetOobisArgs(client1, nameMember1, "agent"),
                new GetOobisArgs(client2, nameMember2, "agent"),
                new GetOobisArgs(client3, nameMember3, "agent"),
                new GetOobisArgs(client1, nameMultisig, "agent")
        );

        Map<String, Object> ooobi1 = (Map<String, Object>) oobis.getFirst();
        Map<String, Object> ooobi2 = (Map<String, Object>) oobis.get(1);
        Map<String, Object> ooobi3 = (Map<String, Object>) oobis.get(2);
        Map<String, Object> ooobi4 = (Map<String, Object>) oobis.get(3);

        ArrayList<String> oobiMultisig = (ArrayList<String>) ooobi4.get("oobis");
        oobiRes = oobiMultisig.getFirst().split("/agent/")[0];
        // TO DO

    }

    public static States.HabState createAID(SignifyClient client, String name, List<String> wits) throws Exception {
        CreateIdentifierArgs iargs = new CreateIdentifierArgs();
        iargs.setWits(wits);
        iargs.setToad(wits.size());
        TestUtils.getOrCreateIdentifier(client, name, iargs);
        return client.getIdentifier().get(name);
    }

    public List<String> extractMultisigKeys(Operation<?> operation) {
        LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) operation.getResponse();
        return (List<String>) response.get("k");
    }

    public Map<String, Object> extractSigningAgent(Object members) {
        LinkedHashMap<String, Object> memberList = TestUtils.castObjectToLinkedHashMap(members);
        List<Map<String, Object>> signingList = TestUtils.castObjectToListMap(memberList.get("signing"));
        LinkedHashMap<String, Object> ends = TestUtils.castObjectToLinkedHashMap(signingList.getFirst().get("ends"));
        return (Map<String, Object>) ends.get("agent");
    }

    public Operation<Object> waitAndResolve(Object future) {
        return ((CompletableFuture<Operation<Object>>) future).join();
    }

    public static String getOobisIndexAt0(Object oobi) {
        Map<String, Object> oobiBody = Utils.toMap(oobi);
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }
}
