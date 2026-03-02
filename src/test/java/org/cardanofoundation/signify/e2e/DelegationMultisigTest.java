package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils;
import org.cardanofoundation.signify.e2e.utils.Retry;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.app.util.HabStateUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("unchecked")
public class DelegationMultisigTest extends BaseIntegrationTest {
    // private static final ObjectMapper objectMapper = new ObjectMapper();
    TestSteps testSteps = new TestSteps();
    String delegatorGroupName = "delegator_group";
    String delegateeGroupName = "delegatee_group";
    String delegator1Name = "delegator1";
    String delegator2Name = "delegator2";
    String delegatee1Name = "delegatee1";
    String delegatee2Name = "delegatee2";

    @Test
    @DisplayName("Delegation Multisig Test")
    void delegationMultisigTest() throws Exception {

        // Boot four clients
        List<SignifyClient> signifyClients = getOrCreateClientsAsync(4);
        SignifyClient delegator1Client = signifyClients.get(0);
        SignifyClient delegator2Client = signifyClients.get(1);
        SignifyClient delegatee1Client = signifyClients.get(2);
        SignifyClient delegatee2Client = signifyClients.get(3);

        // Create delegator and delegatee identifiers clients
        List<HabState> aids = testSteps.step("Creating single sig aids", () ->
                createAidAndGetHabStateAsync(
                        new CreateAidArgs(delegator1Client, delegator1Name),
                        new CreateAidArgs(delegator2Client, delegator2Name),
                        new CreateAidArgs(delegatee1Client, delegatee1Name),
                        new CreateAidArgs(delegatee2Client, delegatee2Name))
        );

        HabState delegator1Aid = aids.get(0);
        HabState delegator2Aid = aids.get(1);
        HabState delegatee1Aid = aids.get(2);
        HabState delegatee2Aid = aids.get(3);

        // Exchange OOBIs
        List<Object> oobis = testSteps.step("Exchanging OOBIs", () ->
                getOobisAsync(
                        new GetOobisArgs(delegator1Client, delegator1Name, "agent"),
                        new GetOobisArgs(delegator2Client, delegator2Name, "agent"),
                        new GetOobisArgs(delegatee1Client, delegatee1Name, "agent"),
                        new GetOobisArgs(delegatee2Client, delegatee2Name, "agent")
                ));

        Map<String, Object> delegator1Oobi = (Map<String, Object>) oobis.get(0);
        Map<String, Object> delegator2Oobi = (Map<String, Object>) oobis.get(1);
        Map<String, Object> delegatee1Oobi = (Map<String, Object>) oobis.get(2);
        Map<String, Object> delegatee2Oobi = (Map<String, Object>) oobis.get(3);

        // Resolve OOBIs
        testSteps.step("Resolving OOBIs", () -> {
            resolveOobisAsync(
                    new ResolveOobisArgs(delegator1Client, ((List<String>) delegator2Oobi.get("oobis")).get(0), delegator2Name),
                    new ResolveOobisArgs(delegator2Client, ((List<String>) delegator1Oobi.get("oobis")).get(0), delegator1Name),
                    new ResolveOobisArgs(delegatee1Client, ((List<String>) delegatee2Oobi.get("oobis")).get(0), delegatee2Name),
                    new ResolveOobisArgs(delegatee2Client, ((List<String>) delegatee1Oobi.get("oobis")).get(0), delegatee1Name)
            );
        });
        System.out.println(
                delegator1Name + "(" + HabStateUtil.getHabPrefix(delegator1Aid) + ") and " +
                        delegatee1Name + "(" + HabStateUtil.getHabPrefix(delegatee1Aid) + ") resolved " +
                        delegator2Name + "(" + HabStateUtil.getHabPrefix(delegator2Aid) + ") and " +
                        delegatee2Name + "(" + HabStateUtil.getHabPrefix(delegatee2Aid) + ") OOBIs and vice versa"
        );

        // First member start the creation of a multisig identifier
        // Create a multisig AID for the GEDA.
        // Skip if a GEDA AID has already been incepted.
        Object otor1Object = testSteps.step(String.format("%s(%s) initiated delegator multisig, waiting for %s(%s) to join...",
                delegator1Name, HabStateUtil.getHabPrefix(delegator1Aid), delegator2Name, HabStateUtil.getHabPrefix(delegator2Aid)), () -> {

            MultisigUtils.StartMultisigInceptArgs startMultisigInceptArgs = MultisigUtils.StartMultisigInceptArgs
                    .builder()
                    .groupName(delegatorGroupName)
                    .localMemberName(HabStateUtil.getHabName(delegator1Aid))
                    .participants(List.of(HabStateUtil.getHabPrefix(delegator1Aid), HabStateUtil.getHabPrefix(delegator2Aid)))
                    .isith(2)
                    .nsith(2)
                    .toad(2)
                    .wits(List.of("BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                            "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                            "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"))
                    .build();

            try {
                return MultisigUtils.startMultisigIncept(delegator1Client, startMultisigInceptArgs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Operation<?> otor1 = Operation.fromObject(otor1Object);

        TestUtils.Notification ntor;
        Retry.RetryOptions options = Retry.RetryOptions.builder()
                .maxSleep(10000)
                .minSleep(1000)
                .timeout(30000)
                .build();
        ntor = TestUtils.waitForNotifications(delegator2Client, "/multisig/icp", options).getFirst();
        markAndRemoveNotification(delegator2Client, ntor);

        assertNotNull(ntor.getA().getD());

        MultisigUtils.AcceptMultisigInceptArgs acceptMultisigInceptArgs =
                MultisigUtils.AcceptMultisigInceptArgs
                        .builder()
                        .localMemberName(HabStateUtil.getHabName(delegator2Aid))
                        .groupName(delegatorGroupName)
                        .msgSaid(ntor.getA().getD())
                        .build();

        Object otor2Object = MultisigUtils.acceptMultisigIncept(delegator2Client, acceptMultisigInceptArgs);
        Operation<?> otor2 = Operation.fromObject(otor2Object);

        String torpre = otor1.getName().split("\\.")[1];

        waitOperationAsync(
                new WaitOperationArgs(delegator1Client, otor1),
                new WaitOperationArgs(delegator2Client, otor2)
        );

        HabState adelegatorGroupName1 = delegator1Client.identifiers().get(delegatorGroupName).get();
        HabState adelegatorGroupName2 = delegator2Client.identifiers().get(delegatorGroupName).get();

        assertEquals(HabStateUtil.getHabPrefix(adelegatorGroupName1), HabStateUtil.getHabPrefix(adelegatorGroupName2));
        assertEquals(HabStateUtil.getHabName(adelegatorGroupName1), HabStateUtil.getHabName(adelegatorGroupName2));

        HabState adelegatorGroupName = adelegatorGroupName1;

        //Resolve delegator OOBI
                String delegatorGroupNameOobi = testSteps.step(String.format("Add and resolve delegator OOBI %s(%s)", delegatorGroupName, HabStateUtil.getHabPrefix(adelegatorGroupName)), () -> {
            String timestamp = createTimestamp();
            try {
                List<Object> opList1 = MultisigUtils.addEndRoleMultisig(delegator1Client,
                        delegatorGroupName,
                        delegator1Aid,
                        List.of(delegator2Aid),
                        adelegatorGroupName,
                        timestamp,
                        true);

                List<Object> opList2 = MultisigUtils.addEndRoleMultisig(delegator2Client,
                        delegatorGroupName,
                        delegator2Aid,
                        List.of(delegator1Aid),
                        adelegatorGroupName,
                        timestamp,
                        false);

                List<WaitOperationArgs> waitOperationArgsList = new ArrayList<>();
                opList1.forEach(op -> waitOperationArgsList.add(new WaitOperationArgs(delegator1Client, op)));
                opList2.forEach(op -> waitOperationArgsList.add(new WaitOperationArgs(delegator2Client, op)));
                waitOperationAsync(waitOperationArgsList.toArray(new WaitOperationArgs[0]));

                TestUtils.waitAndMarkNotification(delegator1Client, "/multisig/rpy");
                TestUtils.waitAndMarkNotification(delegator2Client, "/multisig/rpy");

                Map<String, Object> odelegatorGroupName1 = (Map<String, Object>) delegator1Client.oobis().get(HabStateUtil.getHabName(adelegatorGroupName), "agent").get();
                Map<String, Object> odelegatorGroupName2 = (Map<String, Object>) delegator2Client.oobis().get(HabStateUtil.getHabName(adelegatorGroupName), "agent").get();

                assertEquals(odelegatorGroupName1.get("role"), odelegatorGroupName2.get("role"));

                String stringOobis1 = ((List<String>) odelegatorGroupName1.get("oobis")).get(0);
                String stringOobis2 = ((List<String>) odelegatorGroupName2.get("oobis")).get(0);

                assertEquals(stringOobis1, stringOobis2);
                return stringOobis1;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        String oobiGtor = delegatorGroupNameOobi.split("/agent/")[0];

        getOrCreateContactAsync(
                new GetOrCreateContactArgs(delegatee1Client, delegateeGroupName, oobiGtor),
                new GetOrCreateContactArgs(delegatee2Client, delegateeGroupName, oobiGtor)
        );

        Object opDelegatee1 = testSteps.step(delegatee1Name + "(" + HabStateUtil.getHabPrefix(delegatee1Aid) + ") initiated delegatee multisig, waiting for "
                + delegatee2Name + "(" + HabStateUtil.getHabPrefix(delegatee2Aid) + ") to join...", () -> {
            MultisigUtils.StartMultisigInceptArgs startMultisigInceptArgs = MultisigUtils.StartMultisigInceptArgs
                    .builder()
                    .groupName(delegateeGroupName)
                    .localMemberName(HabStateUtil.getHabName(delegatee1Aid))
                    .participants(List.of(HabStateUtil.getHabPrefix(delegatee1Aid), HabStateUtil.getHabPrefix(delegatee2Aid)))
                    .isith(2)
                    .nsith(2)
                    .toad(2)
                    .delpre(torpre)
                    .wits(List.of("BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                            "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                            "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"))
                    .build();
            try {
                return MultisigUtils.startMultisigIncept(delegatee1Client, startMultisigInceptArgs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Second member of delegatee check notifications and join the multisig
        Notification ntee = TestUtils.waitForNotifications(delegatee2Client, "/multisig/icp").get(0);
        markAndRemoveNotification(delegatee2Client, ntee);

        assertNotNull(ntee.getA().getD());
        acceptMultisigInceptArgs =
                MultisigUtils.AcceptMultisigInceptArgs
                        .builder()
                        .localMemberName(HabStateUtil.getHabName(delegatee2Aid))
                        .groupName(delegateeGroupName)
                        .msgSaid(ntee.getA().getD())
                        .build();
        Object opDelegatee2 = MultisigUtils.acceptMultisigIncept(delegatee2Client, acceptMultisigInceptArgs);
        System.out.println(delegatee2Name + " joined multisig, waiting for delegator...");

        HabState agtee1 = delegatee1Client.identifiers().get(delegateeGroupName).get();
        HabState agtee2 = delegatee2Client.identifiers().get(delegateeGroupName).get();

        assertEquals(HabStateUtil.getHabPrefix(agtee1), HabStateUtil.getHabPrefix(agtee2));
        assertEquals(HabStateUtil.getHabName(agtee1), HabStateUtil.getHabName(agtee2));

        String teepre = Operation.fromObject(opDelegatee1).getName().split("\\.")[1];
        assertEquals(teepre, Operation.fromObject(opDelegatee2).getName().split("\\.")[1]);

        testSteps.step("delegator anchors/approves delegation", () -> {
            // GEDA anchors delegation with an interaction event.
            Map<String, String> anchor = new LinkedHashMap<>() {{
                put("i", teepre);
                put("s", "0");
                put("d", teepre);
            }};

            try {
                Object delApprOp1 = MultisigUtils.delegateMultisig(
                        delegator1Client,
                        delegator1Aid,
                        Collections.singletonList(delegator2Aid),
                        adelegatorGroupName,
                        anchor,
                        true);

                Object delApprOp2 = MultisigUtils.delegateMultisig(
                        delegator2Client,
                        delegator2Aid,
                        Collections.singletonList(delegator1Aid),
                        adelegatorGroupName,
                        anchor,
                        false);

                Operation<Object> dresult1 = waitOperation(delegator1Client, delApprOp1);
                Object responseDresult1 = dresult1.getResponse();

                Operation<Object> dresult2 = waitOperation(delegator2Client, delApprOp2);
                Object responseDresult2 = dresult2.getResponse();

                assertEquals(responseDresult1, responseDresult2);
                waitAndMarkNotification(delegator1Client, "/multisig/ixn");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Object queryOp1 = delegator1Client.keyStates().query(HabStateUtil.getHabPrefix(adelegatorGroupName), "1", null);
        Object queryOp2 = delegator2Client.keyStates().query(HabStateUtil.getHabPrefix(adelegatorGroupName), "1", null);

        waitOperationAsync(
                new WaitOperationArgs(delegator1Client, queryOp1),
                new WaitOperationArgs(delegator2Client, queryOp2)
        );

        // QARs query the GEDA's key state
        Object ksteetor1 = delegatee1Client.keyStates().query(HabStateUtil.getHabPrefix(adelegatorGroupName), "1", null);
        Object ksteetor2 = delegatee2Client.keyStates().query(HabStateUtil.getHabPrefix(adelegatorGroupName), "1", null);

        waitOperationAsync(
                new WaitOperationArgs(delegatee1Client, ksteetor1),
                new WaitOperationArgs(delegatee2Client, ksteetor2),
                new WaitOperationArgs(delegatee1Client, opDelegatee1),
                new WaitOperationArgs(delegatee2Client, opDelegatee2)
        );
        System.out.println("Delegated multisig created!");

        HabState agtee = delegatee1Client.identifiers().get(delegateeGroupName).get();
        assertEquals(HabStateUtil.getHabPrefix(agtee), teepre);

        List<SignifyClient> clients = Arrays.asList(
                delegator1Client,
                delegator2Client,
                delegatee1Client,
                delegatee2Client
        );
        assertOperations(clients);
        assertNotifications(clients);
    }
}
