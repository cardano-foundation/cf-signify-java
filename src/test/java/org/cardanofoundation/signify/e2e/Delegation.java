package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.Retry;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.SinglesigDRT.delegator;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Delegation extends TestUtils {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private String opResponseName, oobisResponse;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void delegationTest() throws Exception {
        String bran1 = Coring.randomPasscode();
        String bran2 = Coring.randomPasscode();
        SignifyClient client1 = new SignifyClient(
                url,
                bran1,
                Salter.Tier.low,
                bootUrl,
                null
        );
        SignifyClient client2 = new SignifyClient(
                url,
                bran2,
                Salter.Tier.low,
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
        kargs.setWits(List.of(
                "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
                "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
                "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
        ));
        EventResult icpResult1 = client1.getIdentifier().create("delegator", kargs);

        waitOperation(client1, icpResult1.op());
        States.HabState ator = client1.getIdentifier().get("delegator");
        EventResult rpyResult1 = client1.getIdentifier().addEndRole(
                "delegator",
                "agent",
                client1.getAgent().getPre(),
                null
        );
        waitOperation(client1, rpyResult1.op());

        // Client 2 resolves delegator OOBI
        Map<String, Object> oobi1 = (Map<String, Object>) client1.getOobis().get("delegator", "agent");
        ArrayList<String> listOobi1 = (ArrayList<String>) oobi1.get("oobis");
        resolveOobi(client2, listOobi1.getFirst(), "delegator");
        System.out.println("OOBI resolved");

        // Client 2 creates delegate AID
        CreateIdentifierArgs delpre = new CreateIdentifierArgs();
        delpre.setDelpre(ator.getPrefix());
        EventResult icpResult2 = client2.getIdentifier().create("delegate", delpre);
        Object op2 = icpResult2.op();
        if (op2 instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op2, new TypeReference<>() {
                });
                opResponseName = opMap.get("name").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String delegatePrefix = opResponseName.split("\\.")[1];
        System.out.println("Delegate's prefix: " + delegatePrefix);

        Map<String, String> anchor = new HashMap<>();
        anchor.put("i", delegatePrefix);
        anchor.put("s", "0");
        anchor.put("d", delegatePrefix);

        // TO DO Approve delegation

        Object op3 = client2.getKeyStates().query(ator.getPrefix(), 1, null);
        if (op3 instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op3, new TypeReference<>() {});
                waitOperation(client2, opMap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Client 2 check approval
        waitOperation(client2, op2);

        States.HabState aid2 = client2.getIdentifier().get("delegate");
        assertEquals(delegatePrefix, aid2.getPrefix());
        System.out.println("Delegation approved for aid: " + aid2.getPrefix());

        assertOperations(Collections.singletonList(client1));
        assertOperations(Collections.singletonList(client2));

        EventResult rpyResult2 = client2.getIdentifier().addEndRole(
                "delegator",
                "agent",
                client2.getAgent().getPre(),
                null
        );
        waitOperation(client1, rpyResult2.op());
        Object oobis =  client2.getOobis().get("delegator", "agent");
        try {
            HashMap<String, Object> opMap = objectMapper.readValue(oobis.toString(), new TypeReference<>() {
            });
            oobisResponse = (String) opMap.get("oobis");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String contactId = getOrCreateContact(
                client1,
                "delegate",
                oobisResponse.split("/agent/")[1]
        );

        assertEquals(aid2.getPrefix(), contactId);
    }
}
