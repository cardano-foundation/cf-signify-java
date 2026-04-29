package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.resolveOobi;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.waitForCompleted;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WitnessTest {
    SignifyClient client1;
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    String WITNESS_AID = "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha";
    ArrayList<String> WITNESS_URL = new ArrayList<>(Arrays.asList(
            "http://witness-demo:5642",
            "http://witness-demo:5643",
            "http://witness-demo:5644"));

    @Test
    public void testWitness() throws Exception {
        String bran1 = Coring.randomPasscode();
        client1 = new SignifyClient(
                url,
                bran1,
                Tier.LOW,
                bootUrl,
                null
        );
        client1.boot();
        client1.connect();
        client1.state();

        // Client 1 resolves witness OOBI
        resolveOobi(client1,
                WITNESS_URL.getFirst() + "/oobi/" + WITNESS_AID,
                "wit");
        System.out.println("Witness OOBI resolved");

        // Client 1 creates AID with 1 witness
        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setToad(1);
        kargs.setWits(Collections.singletonList(WITNESS_AID));

        var icpResult1 = client1.identifiers().create("aid1", kargs);
        waitForCompleted(client1, icpResult1.op());
        HabState aid1 = client1.identifiers().get("aid1").get();
        System.out.println("AID1: " + aid1.getPrefix());
        assertEquals(1, aid1.getState().getB().size());
        assertEquals(WITNESS_AID, aid1.getState().getB().getFirst());

        var rotResult1 = client1.identifiers().rotate("aid1");
        waitForCompleted(client1, rotResult1.op());
        aid1 = client1.identifiers().get("aid1").get();
        assertEquals(1, aid1.getState().getB().size());
        assertEquals(WITNESS_AID, aid1.getState().getB().getFirst());

        // Remove witness
        RotateIdentifierArgs args = RotateIdentifierArgs.builder().build();
        args.setCuts(Collections.singletonList(WITNESS_AID));

        var rotResult2 = client1.identifiers().rotate("aid1", args);
        waitForCompleted(client1, rotResult2.op());
        aid1 = client1.identifiers().get("aid1").get();
        assertEquals(0, aid1.getState().getB().size());

        // Add witness again
        args.setCuts(null);
        args.setAdds(Collections.singletonList(WITNESS_AID));
        var rotResult3 = client1.identifiers().rotate("aid1", args);
        waitForCompleted(client1, rotResult3.op());
        aid1 = client1.identifiers().get("aid1").get();
        assertEquals(1, aid1.getState().getB().size());
        assertEquals(WITNESS_AID, aid1.getState().getB().getFirst());
    }
}
