package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils;
import org.cardanofoundation.signify.e2e.utils.Retry;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DelegationMultisigTest extends BaseIntegrationTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
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
        List<States.HabState> aids = testSteps.step("Creating single sig aids", () ->
                createAidAndGetHabStateAsync(
                        new CreateAidArgs(delegator1Client, delegator1Name),
                        new CreateAidArgs(delegator2Client, delegator2Name),
                        new CreateAidArgs(delegatee1Client, delegatee1Name),
                        new CreateAidArgs(delegatee2Client, delegatee2Name))
        );

        States.HabState delegator1Aid = aids.get(0);
        States.HabState delegator2Aid = aids.get(1);
        States.HabState delegatee1Aid = aids.get(2);
        States.HabState delegatee2Aid = aids.get(3);

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
                delegator1Name + "(" + delegator1Aid.getPrefix() + ") and " +
                        delegatee1Name + "(" + delegatee1Aid.getPrefix() + ") resolved " +
                        delegator2Name + "(" + delegator2Aid.getPrefix() + ") and " +
                        delegatee2Name + "(" + delegatee2Aid.getPrefix() + ") OOBIs and vice versa"
        );

        // First member start the creation of a multisig identifier
        // Create a multisig AID for the GEDA.
        // Skip if a GEDA AID has already been incepted.
        Object otor1Object = testSteps.step(String.format("%s(%s) initiated delegator multisig, waiting for %s(%s) to join...",
                delegator1Name, delegator1Aid.getPrefix(), delegator2Name, delegator2Aid.getPrefix()), () -> {

            MultisigUtils.StartMultisigInceptArgs startMultisigInceptArgs = MultisigUtils.StartMultisigInceptArgs
                    .builder()
                    .groupName(delegatorGroupName)
                    .localMemberName(delegator1Aid.getName())
                    .participants(List.of(delegator1Aid.getPrefix(), delegator2Aid.getPrefix()))
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

        Operation otor1 = Operation.fromObject(otor1Object);

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
                        .localMemberName(delegator2Aid.getName())
                        .groupName(delegatorGroupName)
                        .msgSaid(ntor.getA().getD())
                        .build();

        Object otor2Object = MultisigUtils.acceptMultisigIncept(delegator2Client, acceptMultisigInceptArgs);
        Operation otor2 = Operation.fromObject(otor2Object);

        String torpre = otor1.getName().split("\\.")[1];

        waitOperationAsync(
                new WaitOperationArgs(delegator1Client, otor1),
                new WaitOperationArgs(delegator2Client, otor2)
        );

        States.HabState adelegatorGroupName1 = delegator1Client.getIdentifier().get(delegatorGroupName);
        States.HabState adelegatorGroupName2 = delegator2Client.getIdentifier().get(delegatorGroupName);

        assertEquals(adelegatorGroupName1.getPrefix(), adelegatorGroupName2.getPrefix());
        assertEquals(adelegatorGroupName1.getName(), adelegatorGroupName2.getName());

        States.HabState adelegatorGroupName = adelegatorGroupName1;

        //Resolve delegator OOBI
        String delegatorGroupNameOobi = testSteps.step(String.format("Add and resolve delegator OOBI %s(%s)", delegatorGroupName, adelegatorGroupName.getPrefix()), () -> {
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

                Map<String, Object> odelegatorGroupName1 = (Map<String, Object>) delegator1Client.getOobis().get(adelegatorGroupName.getName(), "agent");
                Map<String, Object> odelegatorGroupName2 = (Map<String, Object>) delegator2Client.getOobis().get(adelegatorGroupName.getName(), "agent");

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

        Object opDelegatee1 = testSteps.step(delegatee1Name + "(" + delegatee1Aid.getPrefix() + ") initiated delegatee multisig, waiting for "
                + delegatee2Name + "(" + delegatee2Aid.getPrefix() + ") to join...", () -> {
            MultisigUtils.StartMultisigInceptArgs startMultisigInceptArgs = MultisigUtils.StartMultisigInceptArgs
                    .builder()
                    .groupName(delegateeGroupName)
                    .localMemberName(delegatee1Aid.getName())
                    .participants(List.of(delegatee1Aid.getPrefix(), delegatee2Aid.getPrefix()))
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
                        .localMemberName(delegatee2Aid.getName())
                        .groupName(delegateeGroupName)
                        .msgSaid(ntee.getA().getD())
                        .build();
        Object opDelegatee2 = MultisigUtils.acceptMultisigIncept(delegatee2Client, acceptMultisigInceptArgs);
        System.out.println(delegatee2Name + " joined multisig, waiting for delegator...");

        States.HabState agtee1 = delegatee1Client.getIdentifier().get(delegateeGroupName);
        States.HabState agtee2 = delegatee2Client.getIdentifier().get(delegateeGroupName);

        assertEquals(agtee1.getPrefix(), agtee2.getPrefix());
        assertEquals(agtee1.getName(), agtee2.getName());

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

                Operation<Object> dresult1 = waitOperations(delegator1Client, delApprOp1);
                Object responseDresult1 = dresult1.getResponse();

                Operation<Object> dresult2 = waitOperations(delegator2Client, delApprOp2);
                Object responseDresult2 = dresult2.getResponse();

                assertEquals(responseDresult1, responseDresult2);
                waitAndMarkNotification(delegator1Client, "/multisig/ixn");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Object queryOp1 = delegator1Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);
        Object queryOp2 = delegator2Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);

        waitOperationAsync(
                new WaitOperationArgs(delegator1Client, queryOp1),
                new WaitOperationArgs(delegator2Client, queryOp2)
        );

        // QARs query the GEDA's key state
        Object ksteetor1 = delegatee1Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);
        Object ksteetor2 = delegatee2Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);

        waitOperationAsync(
                new WaitOperationArgs(delegatee1Client, ksteetor1),
                new WaitOperationArgs(delegatee2Client, ksteetor2),
                new WaitOperationArgs(delegatee1Client, opDelegatee1),
                new WaitOperationArgs(delegatee2Client, opDelegatee2)
        );
        System.out.println("Delegated multisig created!");

        States.HabState agtee = delegatee1Client.getIdentifier().get(delegateeGroupName);
        assertEquals(agtee.getPrefix(), teepre);

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
