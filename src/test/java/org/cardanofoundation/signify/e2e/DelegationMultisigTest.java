package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.Oobis;
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
import java.util.concurrent.CompletableFuture;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DelegationMultisigTest {

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
        List<CompletableFuture<SignifyClient>> bootFutures = new ArrayList<>();
        bootFutures.add(bootClient());
        bootFutures.add(bootClient());
        bootFutures.add(bootClient());
        bootFutures.add(bootClient());

        CompletableFuture.allOf(bootFutures.toArray(new CompletableFuture[0])).join();
        SignifyClient delegator1Client = bootFutures.get(0).get();
        SignifyClient delegator2Client = bootFutures.get(1).get();
        SignifyClient delegatee1Client = bootFutures.get(2).get();
        SignifyClient delegatee2Client = bootFutures.get(3).get();

        // Create delegator and delegatee identifiers clients
        List<States.HabState> aids = testSteps.step("Creating single sig aids", () -> {
            List<CompletableFuture<States.HabState>> aidFutures = new ArrayList<>();
            aidFutures.add(createAid(delegator1Client, delegator1Name));
            aidFutures.add(createAid(delegator2Client, delegator2Name));
            aidFutures.add(createAid(delegatee1Client, delegatee1Name));
            aidFutures.add(createAid(delegatee2Client, delegatee2Name));

            return aidFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        });

        States.HabState delegator1Aid = aids.get(0);
        States.HabState delegator2Aid = aids.get(1);
        States.HabState delegatee1Aid = aids.get(2);
        States.HabState delegatee2Aid = aids.get(3);

        // Exchange OOBIs
        List<Object> oobis = testSteps.step("Exchanging OOBIs", () -> {
            List<CompletableFuture<Object>> oobiFutures = new ArrayList<>();
            oobiFutures.add(getOobis(delegator1Client.getOobis(), delegator1Name, "agent"));
            oobiFutures.add(getOobis(delegator2Client.getOobis(), delegator2Name, "agent"));
            oobiFutures.add(getOobis(delegatee1Client.getOobis(), delegatee1Name, "agent"));
            oobiFutures.add(getOobis(delegatee2Client.getOobis(), delegatee2Name, "agent"));

            return oobiFutures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        });

        Map<String, Object> delegator1Oobi = (Map<String, Object>) oobis.get(0);
        Map<String, Object> delegator2Oobi = (Map<String, Object>) oobis.get(1);
        Map<String, Object> delegatee1Oobi = (Map<String, Object>) oobis.get(2);
        Map<String, Object> delegatee2Oobi = (Map<String, Object>) oobis.get(3);

        // Resolve OOBIs
        testSteps.step("Resolving OOBIs", () -> {
            List<CompletableFuture<Void>> resolveOobiFutures = new ArrayList<>();
            resolveOobiFutures.add(resolveOobis(delegator1Client, ((List<String>) delegator2Oobi.get("oobis")).get(0), delegator2Name));
            resolveOobiFutures.add(resolveOobis(delegator2Client, ((List<String>) delegator1Oobi.get("oobis")).get(0), delegator1Name));
            resolveOobiFutures.add(resolveOobis(delegatee1Client, ((List<String>) delegatee2Oobi.get("oobis")).get(0), delegatee2Name));
            resolveOobiFutures.add(resolveOobis(delegatee2Client, ((List<String>) delegatee1Oobi.get("oobis")).get(0), delegatee1Name));

            CompletableFuture.allOf(resolveOobiFutures.toArray(new CompletableFuture[0])).join();

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

        TestUtils.waitOperation(delegator1Client, otor1);
        TestUtils.waitOperation(delegator2Client, otor2);

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

                List<CompletableFuture<Object>> waitOperationFutures = new ArrayList<>();
                opList1.forEach(op -> waitOperationFutures.add(waitOperation(delegator1Client, op)));
                opList2.forEach(op -> waitOperationFutures.add(waitOperation(delegator2Client, op)));
                CompletableFuture.allOf(waitOperationFutures.toArray(new CompletableFuture[0])).join();

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
        getOrCreateContact(delegatee1Client, adelegatorGroupName.getName(), oobiGtor);
        getOrCreateContact(delegatee2Client, adelegatorGroupName.getName(), oobiGtor);

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

                Operation dresult1 = TestUtils.waitOperation(delegator1Client, delApprOp1);
                Operation dresult2 = TestUtils.waitOperation(delegator2Client, delApprOp2);

                assertEquals(dresult1.getResponse(), dresult2.getResponse());

                waitAndMarkNotification(delegator1Client, "/multisig/ixn");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Object queryOp1 = delegator1Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);
        Object queryOp2 = delegator2Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);

        TestUtils.waitOperation(delegator1Client, queryOp1);
        TestUtils.waitOperation(delegator2Client, queryOp2);

        // QARs query the GEDA's key state
        Object ksteetor1 = delegatee1Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);
        Object ksteetor2 = delegatee2Client.getKeyStates().query(adelegatorGroupName.getPrefix(), "1", null);

        TestUtils.waitOperation(delegatee1Client, ksteetor1);
        TestUtils.waitOperation(delegatee2Client, ksteetor2);

        TestUtils.waitOperation(delegatee1Client, opDelegatee1);
        TestUtils.waitOperation(delegatee2Client, opDelegatee2);
        System.out.println("Delegated multisig created!");

        States.HabState agtee = delegatee1Client.getIdentifier().get(delegateeGroupName);
        assertEquals(agtee.getPrefix(), teepre);

        // TODO check operations and notifications failures
        // assertOperations(List.of(delegator1Client, delegator2Client, delegatee1Client, delegatee2Client));
        // assertNotifications(List.of(delegator1Client, delegator2Client, delegatee1Client, delegatee2Client));
    }


    CompletableFuture<SignifyClient> bootClient() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getOrCreateClient();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    CompletableFuture<States.HabState> createAid(SignifyClient client, String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TestUtils.createAidAndGetHabState(client, name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    CompletableFuture<Object> getOobis(Oobis oobis, String name, String role) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return oobis.get(name, role);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    CompletableFuture<Void> resolveOobis(SignifyClient signifyClient, String oobi, String alias) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TestUtils.resolveOobi(signifyClient, oobi, alias);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    CompletableFuture<Object> waitOperation(SignifyClient client, Object op) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TestUtils.waitOperation(client, op);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
