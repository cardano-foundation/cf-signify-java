package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.app.util.HabStateUtil;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.Retry.retry;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("unchecked")
public class DelegationTest {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private static SignifyClient client1, client2;
    private String opResponseName;
    private TestSteps testSteps = new TestSteps();
    String oobi, contactId;

    @Test
    void delegationTest() throws Exception {
        String bran1 = Coring.randomPasscode();
        String bran2 = Coring.randomPasscode();
        client1 = new SignifyClient(
                url,
                bran1,
                Tier.LOW,
                bootUrl,
                null
        );
        client2 = new SignifyClient(
                url,
                bran2,
                Tier.LOW,
                bootUrl,
                null
        );
        client1.boot();
        client2.boot();
        client1.connect();
        client2.connect();
        client1.state();
        client2.state();

        // Client 1 create delegator AID
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setToad(3);
        List<String> wits = new ArrayList<>(Arrays.asList(
                "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"));
        kargs.setWits(wits);
        EventResult icpResult1 = client1.identifiers().create("delegator", kargs);
        waitOperation(client1, icpResult1.op());

        HabState ator = client1.identifiers().get("delegator").get();
        EventResult rpyResult1 = client1.identifiers().addEndRole(
                "delegator",
                "agent",
                client1.getAgent().getPre(),
                null
        );
        waitOperation(client1, rpyResult1.op());

        // Client 2 resolves delegator OOBI
        Map<String, Object> oobi1 = (Map<String, Object>) client1.oobis().get("delegator", "agent").get();
        ArrayList<String> listOobi1 = (ArrayList<String>) oobi1.get("oobis");
        resolveOobi(client2, listOobi1.getFirst(), "delegator");
        System.out.println("OOBI resolved");

        // Client 2 creates delegate AID
        CreateIdentifierArgs delpre = new CreateIdentifierArgs();
        delpre.setDelpre(HabStateUtil.getHabPrefix(ator));
        EventResult icpResult2 = client2.identifiers().create("delegate", delpre);
        Operation<?> op2 = Operation.fromObject(icpResult2.op());
        opResponseName = op2.getName();
        String delegatePrefix = opResponseName.split("\\.")[1];
        System.out.println("Delegate's prefix: " + delegatePrefix);
        System.out.println("Delegate waiting for approval...");

        // Client 1 approves delegation
        LinkedHashMap<String, Object> anchor = new LinkedHashMap<>();
        anchor.put("i", delegatePrefix);
        anchor.put("s", "0");
        anchor.put("d", delegatePrefix);

        testSteps.step("delegator approves delegation", () -> {
            EventResult result = retry(unchecked(() -> {
                EventResult apprDelRes = client1.delegations().approve("delegator", anchor);
                waitOperation(client1, apprDelRes.op());
                return apprDelRes;
            }));
            List<LinkedHashMap<String, Object>> approDelResList = (List<LinkedHashMap<String, Object>>) result.serder().getKed().get("a");
            assertEquals(approDelResList.getFirst(), anchor);
        });

        Object op3 = client2.keyStates().query(HabStateUtil.getHabPrefix(ator), "1", null);
        waitOperation(client2, op3);

        // Client 2 check approval
        waitOperation(client2, op2);
        HabState aid2 = client2.identifiers().get("delegate").get();
        assertEquals(delegatePrefix, HabStateUtil.getHabPrefix(aid2));
        System.out.println("Delegation approved for aid: " + HabStateUtil.getHabPrefix(aid2));

        List<SignifyClient> clientList = new ArrayList<>(Arrays.asList(client1, client2));
        assertOperations(clientList);

        EventResult rpyResult2 = client2.identifiers().addEndRole(
                "delegate",
                "agent",
                client2.getAgent().getPre(),
                null
        );
        waitOperation(client2, rpyResult2.op());
        Object oobis = client2.oobis().get("delegate", null).get();
        Map<String, Object> oobiBody = (Map<String, Object>) oobis;
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");

        oobi = oobisResponse.getFirst().split("/agent/")[0];
        assertNotNull(oobi);

        contactId = getOrCreateContact(client1, "delegate", oobi);
        assertEquals(HabStateUtil.getHabPrefix(aid2), contactId);
    }
}