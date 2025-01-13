package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.getOrCreateClients;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.getOrCreateIdentifier;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSetupSingleClient {
    private static SignifyClient client;
    private static String name1_id;
    private static String name1_oobi;
    private static List<String> brans = Collections.singletonList("0ADF2TpptgqcDE5IQUF1HeTp");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> clients = getOrCreateClients(1, brans);
        client = clients.getFirst();
    }

    @BeforeEach
    public void getIdentifier() throws Exception {
        String[] clients1 = getOrCreateIdentifier(client, "name1", null);
        name1_id = clients1[0];
        name1_oobi = clients1[1];
    }

    @Test
    public void test_setup_single_client_step1() {
        assertEquals("EB3UGWwIMq7ppzcQ697ImQIuXlBG5jzh-baSx-YG3-tY", client.getController().getPre());
        System.out.println("Test Setup Single Client: Step 1 is Passed");
    }

    @Test
    public void test_setup_single_client_step2() throws SodiumException, IOException, InterruptedException {
        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
        Map<String, Object> oobi = (Map<String, Object>) client.getOobis().get("name1", "witness");
        ArrayList<String> oobis = (ArrayList<String>) oobi.get("oobis");
        assertEquals(3, oobis.size());
        switch (env.preset()) {
            case LOCAL:
                assertEquals(name1_oobi, "http://127.0.0.1:3902/oobi/" + name1_id + "/agent/" + client.getAgent().getPre());
                assertEquals(oobis.get(0), "http://127.0.0.1:5642/oobi/" + name1_id + "/witness/BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha");
                assertEquals(oobis.get(1), "http://127.0.0.1:5643/oobi/" + name1_id + "/witness/BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM");
                assertEquals(oobis.get(2), "http://127.0.0.1:5644/oobi/" + name1_id + "/witness/BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX");
                break;
            case DOCKER:
                assertEquals(name1_oobi, "http://keria:3902/oobi/" + name1_id + "/agent/" + client.getAgent().getPre());
                assertEquals(oobis.get(0), "http://witness-demo:5642/oobi/" + name1_id + "/witness/BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha");
                assertEquals(oobis.get(1), "http://witness-demo:5643/oobi/" + name1_id + "/witness/BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM");
                assertEquals(oobis.get(2), "http://witness-demo:5644/oobi/" + name1_id + "/witness/BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX");
                break;
        }
        System.out.println("Test Setup Single Client: Step 2 is Passed");
    }
}