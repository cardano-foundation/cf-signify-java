package org.cardanofoundation.signify.e2e;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.e2e.utils.TestUtils.Notification;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.acceptMultisigIncept;
import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.startMultisigIncept;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MultisigInceptionTest extends BaseIntegrationTest {
    SignifyClient client1, client2;
    String aid1, aid2;
    Object oobi1, oobi2;
    TestSteps testSteps = new TestSteps();

    @Test
    public void testMultisigInception() throws Exception {
        List<SignifyClient> clients = getOrCreateClientsAsync(2);
        client1 = clients.get(0);
        client2 = clients.get(1);

        aid1 = TestUtils.getOrCreateIdentifier(client1, "member1", null)[0];
        aid2 = TestUtils.getOrCreateIdentifier(client2, "member2", null)[0];

        testSteps.step("Resolve oobis", () -> {
            try {
                oobi1 = client1.getOobis().get("member1", "agent");
                oobi2 = client2.getOobis().get("member2", "agent");

                TestUtils.resolveOobi(client1, Utils.toList(Utils.toMap(oobi2).get("oobis")).getFirst(), "member2");
                TestUtils.resolveOobi(client2, Utils.toList(Utils.toMap(oobi1).get("oobis")).getFirst(), "member1");
            } catch (SodiumException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Create multisig group", () -> {
            try {
                String groupName = "multisig";
                Object op1 = startMultisigIncept(client1, MultisigUtils.StartMultisigInceptArgs.builder()
                    .groupName(groupName)
                    .localMemberName("member1")
                    .participants(Arrays.asList(aid1, aid2))
                    .toad(2)
                    .isith(2)
                    .nsith(2)
                    .wits(Arrays.asList(
                        "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                        "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                        "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
                    ))
                    .build());
                System.out.println("Member1 initiated multisig, waiting for others to join...");

                // Second member check notifications and join the multisig
                List<Notification> notifications = TestUtils.waitForNotifications(client2, "/multisig/icp");
                for (Notification note : notifications) {
                    client2.getNotifications().mark(note.getI());
                }

                String msgSaid = notifications.getLast().getA().getD();
                assertNotNull(msgSaid, "msgSaid not defined");
                Object op2 = acceptMultisigIncept(client2, MultisigUtils.AcceptMultisigInceptArgs.builder()
                    .localMemberName("member2")
                    .groupName(groupName)
                    .msgSaid(msgSaid)
                    .build());
                System.out.println("Member2 joined multisig, waiting for others...");

                // Check for completion
                op1 = waitOperation(client1, op1);
                op2 = waitOperation(client2, op2);
                System.out.println("Multisig created!");

                States.HabState multisig1 = client1.getIdentifier().get(groupName);
                States.HabState multisig2 = client2.getIdentifier().get(groupName);
                assertEquals(multisig1.getPrefix(), multisig2.getPrefix());
                Object members = client1.getIdentifier().members(groupName);
                Map<String, Object> membersMap = Utils.toMap(members);
                List<?> signing = (List<?>) membersMap.get("signing");
                List<?> rotation = (List<?>) membersMap.get("rotation");

                assertEquals(2, signing.size());
                assertEquals(2, rotation.size());
                assertEquals(aid1, Utils.toMap(signing.get(0)).get("aid"));
                assertEquals(aid2, Utils.toMap(signing.get(1)).get("aid"));
                assertEquals(aid1, Utils.toMap(rotation.get(0)).get("aid"));
                assertEquals(aid2, Utils.toMap(rotation.get(1)).get("aid"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Test creating another group", () -> {
            try {
                String groupName = "multisig2";
                Object op1 = startMultisigIncept(client1, MultisigUtils.StartMultisigInceptArgs.builder()
                    .groupName(groupName)
                    .localMemberName("member1")
                    .participants(List.of(aid1, aid2))
                    .toad(0)
                    .isith(2)
                    .nsith(2)
                    .wits(new ArrayList<>())
                    .build()
                );
                System.out.println("Member1 initiated multisig, waiting for others to join...");

                // Second member check notifications and join the multisig
                List<Notification> notifications = TestUtils.waitForNotifications(client2, "/multisig/icp");
                for (Notification note : notifications) {
                    client2.getNotifications().mark(note.getI());
                }

                String msgSaid = notifications.getLast().getA().getD();
                assertNotNull(msgSaid, "msgSaid not defined");
                Object op2 = acceptMultisigIncept(client2, MultisigUtils.AcceptMultisigInceptArgs.builder()
                    .localMemberName("member2")
                    .groupName(groupName)
                    .msgSaid(msgSaid)
                    .build()
                );

                op1 = waitOperation(client1, op1);
                op2 = waitOperation(client2, op2);

                // TODO: https://github.com/WebOfTrust/keria/issues/189
                // const members = await client1.identifiers().members(groupName);
                // assert.strictEqual(members.signing.length, 2);
                // assert.strictEqual(members.rotating.length, 2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
