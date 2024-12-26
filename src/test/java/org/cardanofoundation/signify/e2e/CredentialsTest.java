package org.cardanofoundation.signify.e2e;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CredentialsTest extends TestUtils {
    private ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    private String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    private String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    private String vLEIServerHostUrl = env.vleiServerUrl() + "/oobi";
    private String QVI_SCHEMA_URL = vLEIServerHostUrl + QVI_SCHEMA_SAID;
    private String LE_SCHEMA_URL = vLEIServerHostUrl + LE_SCHEMA_SAID;
    TestSteps testSteps = new TestSteps();

    private static SignifyClient issuerClient, holderClient, verifierClient, legalEntityClient;
    private Aid issuerAid, holderAid, verifierAid, legalEntityAid;
    private String applySaid, offerSaid, agreeSaid;

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> clients = getOrCreateClients(4, null);
        issuerClient = clients.get(0);
        holderClient = clients.get(1);
        verifierClient = clients.get(2);
        legalEntityClient = clients.get(3);
    }

    @BeforeEach
    public void getContact() throws SodiumException, IOException, InterruptedException {
        getOrCreateContact(issuerClient, "holder", holderAid.oobi);
        getOrCreateContact(issuerClient, "verifier", verifierAid.oobi);
        getOrCreateContact(holderClient, "issuer", issuerAid.oobi);
        getOrCreateContact(holderClient,"verifier", verifierAid.oobi);
        getOrCreateContact(holderClient, "legal-entity" ,legalEntityAid.oobi);
        getOrCreateContact(verifierClient, "issuer" ,issuerAid.oobi);
        getOrCreateContact(verifierClient, "holder", holderAid.oobi);
        getOrCreateContact(legalEntityClient, "holder", holderAid.oobi);
    }

    @AfterAll
    public static void cleanup() throws SodiumException, IOException, InterruptedException {
        assertOperations(Collections.singletonList(issuerClient));
        assertOperations(Collections.singletonList(holderClient));
        assertOperations(Collections.singletonList(verifierClient));
        assertOperations(Collections.singletonList(legalEntityClient));

        // TO-DO: assertNotifications miss func in TestUtils
        assertNotifications(Collections.singletonList(issuerClient));
        assertNotifications(Collections.singletonList(holderClient));
        assertNotifications(Collections.singletonList(verifierClient));
        assertNotifications(Collections.singletonList(legalEntityClient));
    }

    @Test
    public void single_signature_credentials() throws Exception {
//        testSteps.step("Resolve schema oobis", );
    }
}
