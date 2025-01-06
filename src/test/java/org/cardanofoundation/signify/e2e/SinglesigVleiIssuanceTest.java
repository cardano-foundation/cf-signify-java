package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Saider;
import org.cardanofoundation.signify.e2e.modules.IssuerRegistry;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SinglesigVleiIssuanceTest extends TestUtils {
    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    String vleiServerUrl = env.vleiServerUrl();

    String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    String ECR_AUTH_SCHEMA_SAID = "EH6ekLjSr8V32WyFbGe1zXjTzFs9PkTYmupJ9H65O14g";
    String ECR_SCHEMA_SAID = "EEy9PkikFcANV1l7EHukCeXqrzT1hNZjGlUk7wuMO5jw";
    String OOR_AUTH_SCHEMA_SAID = "EKA57bKBKxr_kN7iN5i7lMUxpMG-s19dRcmov1iDxz-E";
    String OOR_SCHEMA_SAID = "EBNaNu-M9P5cgrnfl2Fvymy4E_jvxxyjb70PRtiANlJy";

    String vLEIServerHostUrl = vleiServerUrl + "/oobi";
    String QVI_SCHEMA_URL = vLEIServerHostUrl + "/" + QVI_SCHEMA_SAID;
    String LE_SCHEMA_URL = vLEIServerHostUrl + "/" + LE_SCHEMA_SAID;
    String ECR_AUTH_SCHEMA_URL = vLEIServerHostUrl + "/" + ECR_AUTH_SCHEMA_SAID;
    String ECR_SCHEMA_URL = vLEIServerHostUrl + "/" + ECR_SCHEMA_SAID;
    String OOR_AUTH_SCHEMA_URL = vLEIServerHostUrl + "/" + OOR_AUTH_SCHEMA_SAID;
    String OOR_SCHEMA_URL = vLEIServerHostUrl + "/" + OOR_SCHEMA_SAID;

    SignifyClient gleifClient, qviClient, leClient, roleClient;
    Aid gleifAid, qviAid, leAid, roleAid;
    IssuerRegistry gleifRegistry, qviRegistry, leRegistry;

    @Test
    public void singlesig_vlei_issuance() throws Exception {
        Map<String, Object> qviData = new HashMap<>();
        qviData.put("LEI", "254900OPPU84GM83MG36");

        Map<String, Object> leData = new HashMap<>();
        leData.put("LEI", "875500ELOZEL05BVXV37");

        Map<String, Object> ecrData = new LinkedHashMap<>();
        ecrData.put("LEI", leData.get("LEI"));
        ecrData.put("personLegalName", "John Doe");
        ecrData.put("engagementContextRole", "EBA Data Submitter");

        Map<String, Object> ecrAuthData = new LinkedHashMap<>();
        ecrAuthData.put("AID", "");
        ecrAuthData.put("LEI", ecrData.get("LEI"));
        ecrAuthData.put("personLegalName", ecrData.get("personLegalName"));
        ecrAuthData.put("engagementContextRole", ecrData.get("engagementContextRole"));

        Map<String, Object> oorData = new LinkedHashMap<>();
        oorData.put("LEI", leData.get("LEI"));
        oorData.put("personLegalName", "John Doe");
        oorData.put("officialRole", "HR Manager");

        Map<String, Object> oorAuthData = new LinkedHashMap<>();
        oorAuthData.put("AID", "");
        oorAuthData.put("LEI", oorData.get("LEI"));
        oorAuthData.put("personLegalName", oorData.get("personLegalName"));
        oorAuthData.put("officialRole", oorData.get("officialRole"));

        Map<String, Object> usageDisclaimer = new HashMap<>();
        usageDisclaimer.put("l", DataString.USAGE_DISCLAIMER);

        Map<String, Object> issuanceDisclaimer = new HashMap<>();
        issuanceDisclaimer.put("l", DataString.ISSUANCE_DISCLAIMER);

        Map<String, Object> privacyDisclaimer = new HashMap<>();
        privacyDisclaimer.put("l", DataString.PRIVACY_DISCLAIMER);

        Map<String, Object> privacyDisclaimer1 = new HashMap<>();
        privacyDisclaimer.put("l", DataString.PRIVACY_DISCLAIMER_1);

        Map<String, Object> LE_RULES_BODY = new LinkedHashMap<>();
        LE_RULES_BODY.put("d", "");
        LE_RULES_BODY.put("usageDisclaimer", usageDisclaimer);
        LE_RULES_BODY.put("issuanceDisclaimer", issuanceDisclaimer);

        Map<String, Object> ECR_RULES_BODY = new LinkedHashMap<>();
        ECR_RULES_BODY.put("d", "");
        ECR_RULES_BODY.put("usageDisclaimer", usageDisclaimer);
        ECR_RULES_BODY.put("issuanceDisclaimer", issuanceDisclaimer);
        ECR_RULES_BODY.put("privacyDisclaimer", privacyDisclaimer);

        Map<String, Object> ECR_AUTH_RULES_BODY = new LinkedHashMap<>();
        ECR_AUTH_RULES_BODY.put("d", "");
        ECR_AUTH_RULES_BODY.put("usageDisclaimer", usageDisclaimer);
        ECR_AUTH_RULES_BODY.put("issuanceDisclaimer", issuanceDisclaimer);
        ECR_AUTH_RULES_BODY.put("privacyDisclaimer", privacyDisclaimer1);

        Saider.SaidifyResult LE_RULES = Saider.saidify(LE_RULES_BODY);
        Saider.SaidifyResult ECR_RULES = Saider.saidify(ECR_RULES_BODY);
        Saider.SaidifyResult ECR_AUTH_RULES = Saider.saidify(ECR_AUTH_RULES_BODY);
        Saider.SaidifyResult OOR_RULES = LE_RULES;
        Saider.SaidifyResult OOR_AUTH_RULES = LE_RULES;

        Map<String, Object> CRED_RETRY_DEFAULTS = new LinkedHashMap<>();
        CRED_RETRY_DEFAULTS.put("maxSleep", "10000");
        CRED_RETRY_DEFAULTS.put("minSleep", "1000");
        CRED_RETRY_DEFAULTS.put("maxRetries", null);
        CRED_RETRY_DEFAULTS.put("timeout", "30000");

        System.out.println("Created data successfully");

        List<SignifyClient> clients = getOrCreateClients(4, null);
        gleifClient = clients.get(0);
        qviClient = clients.get(1);
        leClient = clients.get(2);
        roleClient = clients.get(3);

        gleifAid = createAid(gleifClient, "gleif");
        qviAid = createAid(qviClient, "qvi");
        leAid = createAid(leClient, "le");
        roleAid = createAid(roleClient, "role");

        getOrCreateContact(gleifClient, "qvi", qviAid.oobi);
        getOrCreateContact(qviClient, "gleif", gleifAid.oobi);
        getOrCreateContact(qviClient, "le", leAid.oobi);
        getOrCreateContact(qviClient, "role", roleAid.oobi);
        getOrCreateContact(leClient, "gleif", gleifAid.oobi);
        getOrCreateContact(leClient, "qvi", qviAid.oobi);
        getOrCreateContact(leClient, "role", roleAid.oobi);
        getOrCreateContact(roleClient, "gleif", gleifAid.oobi);
        getOrCreateContact(roleClient, "qvi", qviAid.oobi);
        getOrCreateContact(roleClient, "le", leAid.oobi);

        resolveOobi(gleifClient, QVI_SCHEMA_URL,null);
        resolveOobi(qviClient, QVI_SCHEMA_URL,null);
        resolveOobi(qviClient, LE_SCHEMA_URL,null);
        resolveOobi(qviClient, ECR_AUTH_SCHEMA_URL,null);
        resolveOobi(qviClient, ECR_SCHEMA_URL,null);
        resolveOobi(qviClient, OOR_AUTH_SCHEMA_URL,null);
        resolveOobi(qviClient, OOR_SCHEMA_URL,null);
        resolveOobi(leClient, QVI_SCHEMA_URL,null);
        resolveOobi(leClient, LE_SCHEMA_URL,null);
        resolveOobi(leClient, ECR_AUTH_SCHEMA_URL,null);
        resolveOobi(leClient, ECR_SCHEMA_URL,null);
        resolveOobi(leClient, OOR_AUTH_SCHEMA_URL,null);
        resolveOobi(leClient, OOR_SCHEMA_URL,null);
        resolveOobi(roleClient, QVI_SCHEMA_URL,null);
        resolveOobi(roleClient, LE_SCHEMA_URL,null);
        resolveOobi(roleClient, ECR_AUTH_SCHEMA_URL,null);
        resolveOobi(roleClient, ECR_SCHEMA_URL,null);
        resolveOobi(roleClient, OOR_AUTH_SCHEMA_URL,null);
        resolveOobi(roleClient, OOR_SCHEMA_URL,null);

        gleifRegistry = getOrCreateRegistry(gleifClient, gleifAid, "gleifRegistry");
        qviRegistry = getOrCreateRegistry(qviClient, qviAid, "qviRegistry");
        leRegistry = getOrCreateRegistry(leClient, leAid, "leRegistry");

        System.out.println("Issuing QVI vLEI Credential");

        Object qviCred = getOrIssueCredential(
                gleifClient,
                gleifAid,
                qviAid,
                gleifRegistry,
                qviData,
                QVI_SCHEMA_SAID,
                null,
                null
        );
    }

    public IssuerRegistry getOrCreateRegistry(SignifyClient client, Aid aid, String registryName) throws Exception {
        IssuerRegistry registry = IssuerRegistry.builder().build();
        Object registries = client.getRegistries().list(aid.name);
        ArrayList<String> registriesBody = (ArrayList<String>) registries;
        if (!registriesBody.isEmpty()) {
            assertEquals(1, registriesBody.size());
        } else {
            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(aid.name);
            registryArgs.setRegistryName(registryName);

            RegistryResult regResult = client.getRegistries().create(registryArgs);
            waitOperations(client, regResult.op());
            registries = client.getRegistries().list(aid.name);

            registriesBody = (ArrayList<String>) registries;
            LinkedHashMap<String, Object> registryBody = castObjectToLinkedHashMap(registriesBody.getFirst());
            registry.setName(registryBody.get("name").toString());
            registry.setRegk(registryBody.get("regk").toString());
        }
        return registry;
    }

    public static class DataString {
        public static final String USAGE_DISCLAIMER = "Usage of a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, does not assert that the Legal Entity is trustworthy, honest, reputable in its business dealings, safe to do business with, or compliant with any laws or that an implied or expressly intended purpose will be fulfilled.";
        public static final String ISSUANCE_DISCLAIMER = "All information in a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, is accurate as of the date the validation process was complete. The vLEI Credential has been issued to the legal entity or person named in the vLEI Credential as the subject; and the qualified vLEI Issuer exercised reasonable care to perform the validation process set forth in the vLEI Ecosystem Governance Framework.";
        public static final String PRIVACY_DISCLAIMER = "It is the sole responsibility of Holders as Issuees of an ECR vLEI Credential to present that Credential in a privacy-preserving manner using the mechanisms provided in the Issuance and Presentation Exchange (IPEX) protocol specification and the Authentic Chained Data Container (ACDC) specification. https://github.com/WebOfTrust/IETF-IPEX and https://github.com/trustoverip/tswg-acdc-specification.";
        public static final String PRIVACY_DISCLAIMER_1 = "Privacy Considerations are applicable to QVI ECR AUTH vLEI Credentials.  It is the sole responsibility of QVIs as Issuees of QVI ECR AUTH vLEI Credentials to present these Credentials in a privacy-preserving manner using the mechanisms provided in the Issuance and Presentation Exchange (IPEX) protocol specification and the Authentic Chained Data Container (ACDC) specification.  https://github.com/WebOfTrust/IETF-IPEX and https://github.com/trustoverip/tswg-acdc-specification.";
    }
}
