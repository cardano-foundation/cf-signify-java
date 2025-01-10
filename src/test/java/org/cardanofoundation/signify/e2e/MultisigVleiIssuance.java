package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.createAIDMultisig;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.waitAndMarkNotification;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultisigVleiIssuance extends BaseIntegrationTest {
    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    String vleiServerUrl = env.vleiServerUrl();
    List<String> witnessIds = env.witnessIds();

    String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    String ECR_SCHEMA_SAID = "EEy9PkikFcANV1l7EHukCeXqrzT1hNZjGlUk7wuMO5jw";

    String vLEIServerHostUrl = vleiServerUrl + "/oobi";
    String QVI_SCHEMA_URL = vLEIServerHostUrl + "/" + QVI_SCHEMA_SAID;
    String LE_SCHEMA_URL = vLEIServerHostUrl + "/" + LE_SCHEMA_SAID;
    String ECR_SCHEMA_URL = vLEIServerHostUrl + "/" + ECR_SCHEMA_SAID;

    Map<String, Object> qviData = new HashMap<>();
    Map<String, Object> leData = new HashMap<>();
    Map<String, Object> ecrData = new LinkedHashMap<>();
    Map<String, Object> LE_RULES = new LinkedHashMap<>();
    Map<String, Object> ECR_RULES = new LinkedHashMap<>();

    static TestUtils testUtils = new TestUtils();


    @Test
    public void multisig_vlei_issuance() throws Exception {
        qviData.put("LEI", "254900OPPU84GM83MG36");

        leData.put("LEI", "875500ELOZEL05BVXV37");

        ecrData.put("LEI", leData.get("LEI"));
        ecrData.put("personLegalName", "John Doe");
        ecrData.put("engagementContextRole", "EBA Submitter");

        LE_RULES.put("d", "");
        LE_RULES.put("usageDisclaimer", DataString.USAGE_DISCLAIMER);
        LE_RULES.put("issuanceDisclaimer", DataString.ISSUANCE_DISCLAIMER);

        ECR_RULES.put("d", "");
        ECR_RULES.put("usageDisclaimer", DataString.USAGE_DISCLAIMER);
        ECR_RULES.put("issuanceDisclaimer", DataString.ISSUANCE_DISCLAIMER);
        ECR_RULES.put("privacyDisclaimer", DataString.PRIVACY_DISCLAIMER);

        // Boot nine clients
        List<SignifyClient> signifyClients = getOrCreateClientsAsync(9);
        SignifyClient clientGAR1 = signifyClients.get(0);
        SignifyClient clientGAR2 = signifyClients.get(1);
        SignifyClient clientQAR1 = signifyClients.get(2);
        SignifyClient clientQAR2 = signifyClients.get(3);
        SignifyClient clientQAR3 = signifyClients.get(4);
        SignifyClient clientLAR1 = signifyClients.get(5);
        SignifyClient clientLAR2 = signifyClients.get(6);
        SignifyClient clientLAR3 = signifyClients.get(7);
        SignifyClient clientECR = signifyClients.get(8);

        CreateIdentifierArgs kargsAID = new CreateIdentifierArgs();
        kargsAID.setToad(witnessIds.size());
        kargsAID.setWits(witnessIds);

        States.HabState aidGAR1 = testUtils.getOrCreateAID(clientGAR1, "GAR1", kargsAID);
        States.HabState aidGAR2 = testUtils.getOrCreateAID(clientGAR2, "GAR2", kargsAID);
        States.HabState aidQAR1 = testUtils.getOrCreateAID(clientQAR1, "QAR1", kargsAID);
        States.HabState aidQAR2 = testUtils.getOrCreateAID(clientQAR2, "QAR2", kargsAID);
        States.HabState aidQAR3 = testUtils.getOrCreateAID(clientQAR3, "QAR3", kargsAID);
        States.HabState aidLAR1 = testUtils.getOrCreateAID(clientLAR1, "LAR1", kargsAID);
        States.HabState aidLAR2 = testUtils.getOrCreateAID(clientLAR2, "LAR2", kargsAID);
        States.HabState aidLAR3 = testUtils.getOrCreateAID(clientLAR3, "LAR3", kargsAID);
        States.HabState aidECR = testUtils.getOrCreateAID(clientECR, "ECR", kargsAID);


        List<Object> oobis = getOobisAsync(
                new GetOobisArgs(clientGAR1, "GAR1", "agent"),
                new GetOobisArgs(clientGAR2, "GAR2", "agent"),
                new GetOobisArgs(clientQAR1, "QAR1", "agent"),
                new GetOobisArgs(clientQAR2, "QAR2", "agent"),
                new GetOobisArgs(clientQAR3, "QAR3", "agent"),
                new GetOobisArgs(clientLAR1, "LAR1", "agent"),
                new GetOobisArgs(clientLAR2, "LAR2", "agent"),
                new GetOobisArgs(clientLAR3, "LAR3", "agent"),
                new GetOobisArgs(clientECR, "ECR", "agent")
        );

        Map<String, Object> oobiGAR1 = (Map<String, Object>) oobis.get(0);
        Map<String, Object> oobiGAR2 = (Map<String, Object>) oobis.get(1);
        Map<String, Object> oobiQAR1 = (Map<String, Object>) oobis.get(2);
        Map<String, Object> oobiQAR2 = (Map<String, Object>) oobis.get(3);
        Map<String, Object> oobiQAR3 = (Map<String, Object>) oobis.get(4);
        Map<String, Object> oobiLAR1 = (Map<String, Object>) oobis.get(5);
        Map<String, Object> oobiLAR2 = (Map<String, Object>) oobis.get(6);
        Map<String, Object> oobiLAR3 = (Map<String, Object>) oobis.get(7);
        Map<String, Object> oobiECR = (Map<String, Object>) oobis.get(8);

        getOrCreateContact(clientGAR1, "GAR2", oobiGAR2.get("oobis").toString());
        getOrCreateContact(clientGAR2, "GAR1", oobiGAR1.get("oobis").toString());
        getOrCreateContact(clientQAR1, "QAR2", oobiQAR2.get("oobis").toString());
        getOrCreateContact(clientQAR1, "QAR3", oobiQAR3.get("oobis").toString());
        getOrCreateContact(clientQAR2, "QAR1", oobiQAR1.get("oobis").toString());
        getOrCreateContact(clientQAR2, "QAR3", oobiQAR3.get("oobis").toString());
        getOrCreateContact(clientQAR3, "QAR1", oobiQAR1.get("oobis").toString());
        getOrCreateContact(clientQAR3, "QAR2", oobiQAR2.get("oobis").toString());
        getOrCreateContact(clientLAR1, "LAR2", oobiLAR2.get("oobis").toString());
        getOrCreateContact(clientLAR1, "LAR3", oobiLAR3.get("oobis").toString());
        getOrCreateContact(clientLAR2, "LAR1", oobiLAR1.get("oobis").toString());
        getOrCreateContact(clientLAR2, "LAR3", oobiLAR3.get("oobis").toString());
        getOrCreateContact(clientLAR3, "LAR1", oobiLAR1.get("oobis").toString());
        getOrCreateContact(clientLAR3, "LAR2", oobiLAR2.get("oobis").toString());
        getOrCreateContact(clientLAR1, "ECR", oobiECR.get("oobis").toString());
        getOrCreateContact(clientLAR2, "ECR", oobiECR.get("oobis").toString());
        getOrCreateContact(clientLAR3, "ECR", oobiECR.get("oobis").toString());

        resolveOobisAsync(
                new ResolveOobisArgs(clientGAR1, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientGAR2, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR1, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR1, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR2, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR2, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR3, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR3, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR1, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR1, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR1, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR2, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR2, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR2, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR3, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR3, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR3, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(clientECR, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientECR, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientECR, ECR_SCHEMA_URL, null)

        );

        States.HabState aidGEDAbyGAR1, aidGEDAbyGAR2;
        CreateIdentifierArgs kargsMultisigAID = new CreateIdentifierArgs();
        try {
            aidGEDAbyGAR1 = clientGAR1.getIdentifier().get("GEDA");
            aidGEDAbyGAR2 = clientGAR2.getIdentifier().get("GEDA");
        } catch (Exception e) {
            List<States.State> rstates = Arrays.asList(aidGAR1.getState(), aidGAR2.getState());
            List<States.State> states = rstates;

            kargsMultisigAID.setAlgo(Manager.Algos.group);
            kargsMultisigAID.setIsith(Arrays.asList("1/2", "1/2"));
            kargsMultisigAID.setNsith(Arrays.asList("1/2", "1/2"));
            kargsMultisigAID.setToad(kargsAID.getToad());
            kargsMultisigAID.setWits(kargsAID.getWits());
            kargsMultisigAID.setStates(Collections.singletonList(states));
            kargsMultisigAID.setRstates(Collections.singletonList(rstates));
        }

        kargsMultisigAID.setMhab(aidGAR1);
        Object multisigAIDOp1 = createAIDMultisig(
                clientGAR1,
                aidGAR1,
                Collections.singletonList(aidGAR2),
                "GEDA",
                kargsMultisigAID,
                true
        );

        kargsMultisigAID.setMhab(aidGAR2);
        Object multisigAIDOp2 = createAIDMultisig(
                clientGAR2,
                aidGAR2,
                Collections.singletonList(aidGAR1),
                "GEDA",
                kargsMultisigAID,
                false
        );

        waitOperationAsync(
                new WaitOperationArgs(clientGAR1, multisigAIDOp1),
                new WaitOperationArgs(clientGAR2, multisigAIDOp2)
        );

        waitAndMarkNotification(clientGAR1, "/multisig/icp");
        aidGEDAbyGAR1 = clientGAR1.getIdentifier().get("GEDA");
        aidGEDAbyGAR2 = clientGAR2.getIdentifier().get("GEDA");

        assertEquals(aidGEDAbyGAR1.getPrefix(), aidGEDAbyGAR2.getPrefix());
        assertEquals(aidGEDAbyGAR1.getName(), aidGEDAbyGAR2.getName());
    }

    public class DataString {
        public static final String USAGE_DISCLAIMER = "Usage of a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, does not assert that the Legal Entity is trustworthy, honest, reputable in its business dealings, safe to do business with, or compliant with any laws or that an implied or expressly intended purpose will be fulfilled.";
        public static final String ISSUANCE_DISCLAIMER = "All information in a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, is accurate as of the date the validation process was complete. The vLEI Credential has been issued to the legal entity or person named in the vLEI Credential as the subject; and the qualified vLEI Issuer exercised reasonable care to perform the validation process set forth in the vLEI Ecosystem Governance Framework.";
        public static final String PRIVACY_DISCLAIMER = "It is the sole responsibility of Holders as Issuees of an ECR vLEI Credential to present that Credential in a privacy-preserving manner using the mechanisms provided in the Issuance and Presentation Exchange (IPEX) protocol specification and the Authentic Chained Data Container (ACDC) specification. https://github.com/WebOfTrust/IETF-IPEX and https://github.com/trustoverip/tswg-acdc-specification.";
    }
}
