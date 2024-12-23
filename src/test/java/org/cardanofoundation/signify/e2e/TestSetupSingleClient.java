package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSetupSingleClient extends TestUtils {
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
        String[] clients1 = getOrCreateIdentifier(client, "name1");
        name1_id = clients1[0];
        name1_oobi = clients1[1];
    }

    @Test
    public void test_setup_single_client_step1() {
        assertEquals("EB3UGWwIMq7ppzcQ697ImQIuXlBG5jzh-baSx-YG3-tY", client.getController().getPre());
        System.out.println("Step 1 is Passed");
    }

    @Test
    public void test_setup_single_client_step2() throws SodiumException, IOException, InterruptedException {
        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
        Map<String, Object> oobi = (Map<String, Object>) client.getOobis().get("name1", "witness");
        ArrayList<String> oobis = (ArrayList<String>) oobi.get("oobis");
        assertEquals(3, oobis.size());

    }
}
