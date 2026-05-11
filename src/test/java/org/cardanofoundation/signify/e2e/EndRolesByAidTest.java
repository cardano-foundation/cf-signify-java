package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.e2e.utils.TestUtils.Notification;
import org.cardanofoundation.signify.generated.keria.model.AidRecord;
import org.cardanofoundation.signify.generated.keria.model.EndRole;
import org.cardanofoundation.signify.generated.keria.model.GroupMember;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.OOBI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.cardanofoundation.signify.e2e.MultisigJoinTest.getOobisIndexAt0;
import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.acceptMultisigIncept;
import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.startMultisigIncept;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.unchecked;
import static org.junit.jupiter.api.Assertions.*;

public class EndRolesByAidTest extends BaseIntegrationTest {

    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    TestSteps testSteps = new TestSteps();

    @Test
    public void testEndRolesByAid() throws Exception {
        List<SignifyClient> clients = getOrCreateClientsAsync(4);
        SignifyClient client1 = clients.get(0);
        SignifyClient client2 = clients.get(1);
        SignifyClient client3 = clients.get(2);
        SignifyClient alice = clients.get(3);

        String groupName = "multisig";
        String[] memberNames = {"member1", "member2", "member3"};
        String aliceName = "alice";
        List<String> agentEids = new ArrayList<>();
        List<String> wits = env.witnessIds();

        CreateIdentifierArgs memberArgs = CreateIdentifierArgs.builder()
                .wits(wits)
                .toad(wits.size())
                .build();

        List<HabState> aids = getOrCreateAIDAsync(
                new CreateAidArgs(client1, memberNames[0], memberArgs),
                new CreateAidArgs(client2, memberNames[1], memberArgs),
                new CreateAidArgs(client3, memberNames[2], memberArgs),
                new CreateAidArgs(alice, aliceName, memberArgs)
        );
        HabState aid1 = aids.get(0);
        HabState aid2 = aids.get(1);
        HabState aid3 = aids.get(2);

        testSteps.step("Resolve member OOBIs across group", () -> {
            List<OOBI> oobis = getOobisAsync(
                    new GetOobisArgs(client1, memberNames[0], "agent"),
                    new GetOobisArgs(client2, memberNames[1], "agent"),
                    new GetOobisArgs(client3, memberNames[2], "agent")
            );
            String oobi1 = getOobisIndexAt0(oobis.get(0));
            String oobi2 = getOobisIndexAt0(oobis.get(1));
            String oobi3 = getOobisIndexAt0(oobis.get(2));
            resolveOobisAsync(
                    new ResolveOobisArgs(client1, oobi2, memberNames[1]),
                    new ResolveOobisArgs(client1, oobi3, memberNames[2]),
                    new ResolveOobisArgs(client2, oobi1, memberNames[0]),
                    new ResolveOobisArgs(client2, oobi3, memberNames[2]),
                    new ResolveOobisArgs(client3, oobi1, memberNames[0]),
                    new ResolveOobisArgs(client3, oobi2, memberNames[1])
            );
            return null;
        });

        testSteps.step("Create 3-member multisig group (3-of-3)", () -> {
            List<String> participants = List.of(aid1.getPrefix(), aid2.getPrefix(), aid3.getPrefix());

            System.out.println("Member1 starting multisig inception...");
            Object op1 = startMultisigIncept(client1, MultisigUtils.StartMultisigInceptArgs.builder()
                    .groupName(groupName)
                    .localMemberName(memberNames[0])
                    .participants(participants)
                    .isith(3)
                    .nsith(3)
                    .toad(wits.size())
                    .wits(wits)
                    .build());

            CompletableFuture<Object> acceptFuture2 = CompletableFuture.supplyAsync(unchecked(() -> {
                List<Notification> notes = TestUtils.waitForNotifications(client2, "/multisig/icp");
                for (Notification note : notes) client2.notifications().mark(note.getI());
                System.out.println("Member2 accepted");
                return acceptMultisigIncept(client2, MultisigUtils.AcceptMultisigInceptArgs.builder()
                        .groupName(groupName)
                        .localMemberName(memberNames[1])
                        .msgSaid(notes.getLast().getA().getD())
                        .build());
            }));

            CompletableFuture<Object> acceptFuture3 = CompletableFuture.supplyAsync(unchecked(() -> {
                List<Notification> notes = TestUtils.waitForNotifications(client3, "/multisig/icp");
                for (Notification note : notes) client3.notifications().mark(note.getI());
                System.out.println("Member3 accepted");
                return acceptMultisigIncept(client3, MultisigUtils.AcceptMultisigInceptArgs.builder()
                        .groupName(groupName)
                        .localMemberName(memberNames[2])
                        .msgSaid(notes.getLast().getA().getD())
                        .build());
            }));

            Object op2 = acceptFuture2.join();
            Object op3 = acceptFuture3.join();
            System.out.println("All members accepted, waiting for operations...");

            waitOperationAsync(
                    new WaitOperationArgs(client1, op1),
                    new WaitOperationArgs(client2, op2),
                    new WaitOperationArgs(client3, op3)
            );

            HabState g1 = client1.identifiers().get(groupName).get();
            HabState g2 = client2.identifiers().get(groupName).get();
            HabState g3 = client3.identifiers().get(groupName).get();
            assertEquals(g1.getPrefix(), g2.getPrefix());
            assertEquals(g1.getPrefix(), g3.getPrefix());
            System.out.println("Multisig created: " + g1.getPrefix());
            return null;
        });

        testSteps.step("Add agent end roles for group", () -> {
            HabState aid1Hab = client1.identifiers().get(memberNames[0]).get();
            HabState aid2Hab = client2.identifiers().get(memberNames[1]).get();
            HabState aid3Hab = client3.identifiers().get(memberNames[2]).get();
            HabState multisigAID = client1.identifiers().get(groupName).get();
            String stamp = TestUtils.createTimestamp();

            System.out.println("Adding agent end roles in parallel...");
            CompletableFuture<List<Object>> future1 = CompletableFuture.supplyAsync(unchecked(() ->
                    MultisigUtils.addEndRoleMultisig(client1, groupName, aid1Hab,
                            List.of(aid2Hab, aid3Hab), multisigAID, stamp, true)
            ));
            CompletableFuture<List<Object>> future2 = CompletableFuture.supplyAsync(unchecked(() ->
                    MultisigUtils.addEndRoleMultisig(client2, groupName, aid2Hab,
                            List.of(aid1Hab, aid3Hab), multisigAID, stamp, false)
            ));
            CompletableFuture<List<Object>> future3 = CompletableFuture.supplyAsync(unchecked(() ->
                    MultisigUtils.addEndRoleMultisig(client3, groupName, aid3Hab,
                            List.of(aid1Hab, aid2Hab), multisigAID, stamp, false)
            ));

            List<Object> ops1 = future1.join();
            List<Object> ops2 = future2.join();
            List<Object> ops3 = future3.join();

            List<WaitOperationArgs> waitArgs = new ArrayList<>();
            ops1.forEach(op -> waitArgs.add(new WaitOperationArgs(client1, op)));
            ops2.forEach(op -> waitArgs.add(new WaitOperationArgs(client2, op)));
            ops3.forEach(op -> waitArgs.add(new WaitOperationArgs(client3, op)));
            waitOperationAsync(waitArgs.toArray(new WaitOperationArgs[0]));

            GroupMember members = client1.identifiers().members(groupName);
            for (AidRecord signing : members.getSigning()) {
                agentEids.add(signing.getEnds().getAgent().keySet().iterator().next());
            }
            System.out.println("Agent EIDs: " + agentEids);
            return null;
        });

        testSteps.step("Alice resolves group OOBI", () -> {
            OOBI groupOobi = client1.oobis().get(groupName, "agent").get();
            String oobiUrl = getOobisIndexAt0(groupOobi).split("/agent/")[0];
            TestUtils.resolveOobi(alice, oobiUrl, groupName);
            return null;
        });

        String groupAid = client1.identifiers().get(groupName).get().getPrefix();

        testSteps.step("Alice queries /endroles/{aid}", () -> {
            List<EndRole> result = alice.oobis().endroles(groupAid, null);
            assertFalse(result.isEmpty());
            assertTrue(result.stream().allMatch(r -> groupAid.equals(r.getCid())));
            assertTrue(result.stream().allMatch(r -> "agent".equals(r.getRole())));
            assertEquals(agentEids.stream().sorted().toList(),
                    result.stream().map(EndRole::getEid).sorted().toList());
            return null;
        });

        testSteps.step("Alice queries /endroles/{aid}/agent", () -> {
            List<EndRole> result = alice.oobis().endroles(groupAid, "agent");
            assertFalse(result.isEmpty());
            assertTrue(result.stream().allMatch(r -> "agent".equals(r.getRole())));
            assertTrue(result.stream().allMatch(r -> groupAid.equals(r.getCid())));
            assertEquals(agentEids.stream().sorted().toList(),
                    result.stream().map(EndRole::getEid).sorted().toList());
            return null;
        });

        testSteps.step("Alice queries non-existent role returns empty", () -> {
            assertTrue(alice.oobis().endroles(groupAid, "mailbox").isEmpty());
            return null;
        });

        testSteps.step("Alice queries unknown AID returns empty", () -> {
            assertTrue(alice.oobis().endroles("EXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", null).isEmpty());
            return null;
        });

        testSteps.step("Alice queries her own AID returns her agent EID", () -> {
            HabState aliceAid = alice.identifiers().get(aliceName).get();
            List<EndRole> result = alice.oobis().endroles(aliceAid.getPrefix(), null);
            assertEquals(1, result.size());
            assertEquals("agent", result.getFirst().getRole());
            assertEquals(aliceAid.getPrefix(), result.getFirst().getCid());
            return null;
        });
    }
}
