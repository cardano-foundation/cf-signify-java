package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexAdmitArgs;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexGrantArgs;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.e2e.utils.IssuerRegistry;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.Retry;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;

import static org.cardanofoundation.signify.e2e.utils.Retry.retry;
import static org.junit.jupiter.api.Assertions.*;

public class SinglesigVleiIssuanceTest extends BaseIntegrationTest {
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
        privacyDisclaimer1.put("l", DataString.PRIVACY_DISCLAIMER_1);

        Map<String, Object> LE_RULES = new LinkedHashMap<>();
        LE_RULES.put("d", "");
        LE_RULES.put("usageDisclaimer", usageDisclaimer);
        LE_RULES.put("issuanceDisclaimer", issuanceDisclaimer);

        Map<String, Object> ECR_RULES = new LinkedHashMap<>();
        ECR_RULES.put("d", "");
        ECR_RULES.put("usageDisclaimer", usageDisclaimer);
        ECR_RULES.put("issuanceDisclaimer", issuanceDisclaimer);
        ECR_RULES.put("privacyDisclaimer", privacyDisclaimer);

        Map<String, Object> ECR_AUTH_RULES = new LinkedHashMap<>();
        ECR_AUTH_RULES.put("d", "");
        ECR_AUTH_RULES.put("usageDisclaimer", usageDisclaimer);
        ECR_AUTH_RULES.put("issuanceDisclaimer", issuanceDisclaimer);
        ECR_AUTH_RULES.put("privacyDisclaimer", privacyDisclaimer1);

        Map<String, Object> OOR_RULES = LE_RULES;
        Map<String, Object> OOR_AUTH_RULES = LE_RULES;

        Retry.RetryOptions CRED_RETRY_DEFAULTS = Retry.RetryOptions.builder()
                .maxSleep(10000)
                .minSleep(1000)
                .maxRetries(null)
                .timeout(30000)
                .build();

        System.out.println("Created data successfully");

        List<SignifyClient> clients = getOrCreateClientsAsync(4);
        gleifClient = clients.get(0);
        qviClient = clients.get(1);
        leClient = clients.get(2);
        roleClient = clients.get(3);

        List<TestUtils.Aid> aids = createAidAsync(
                new CreateAidArgs(gleifClient, "gleif"),
                new CreateAidArgs(qviClient, "qvi"),
                new CreateAidArgs(leClient, "le"),
                new CreateAidArgs(roleClient, "role")
        );
        gleifAid = aids.get(0);
        qviAid = aids.get(1);
        leAid = aids.get(2);
        roleAid = aids.get(3);

        getOrCreateContactAsync(
                new GetOrCreateContactArgs(gleifClient, "qvi", qviAid.oobi),
                new GetOrCreateContactArgs(qviClient, "gleif", gleifAid.oobi),
                new GetOrCreateContactArgs(qviClient, "le", leAid.oobi),
                new GetOrCreateContactArgs(qviClient, "role", roleAid.oobi),
                new GetOrCreateContactArgs(leClient, "gleif", gleifAid.oobi),
                new GetOrCreateContactArgs(leClient, "qvi", qviAid.oobi),
                new GetOrCreateContactArgs(leClient, "role", roleAid.oobi),
                new GetOrCreateContactArgs(roleClient, "gleif", gleifAid.oobi),
                new GetOrCreateContactArgs(roleClient, "qvi", qviAid.oobi),
                new GetOrCreateContactArgs(roleClient, "le", leAid.oobi)
        );

        resolveOobisAsync(
                new ResolveOobisArgs(gleifClient, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(qviClient, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(qviClient, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(qviClient, ECR_AUTH_SCHEMA_URL, null),
                new ResolveOobisArgs(qviClient, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(qviClient, OOR_AUTH_SCHEMA_URL, null),
                new ResolveOobisArgs(qviClient, OOR_SCHEMA_URL, null),
                new ResolveOobisArgs(leClient, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(leClient, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(leClient, ECR_AUTH_SCHEMA_URL, null),
                new ResolveOobisArgs(leClient, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(leClient, OOR_AUTH_SCHEMA_URL, null),
                new ResolveOobisArgs(leClient, OOR_SCHEMA_URL, null),
                new ResolveOobisArgs(roleClient, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(roleClient, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(roleClient, ECR_AUTH_SCHEMA_URL, null),
                new ResolveOobisArgs(roleClient, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(roleClient, OOR_AUTH_SCHEMA_URL, null),
                new ResolveOobisArgs(roleClient, OOR_SCHEMA_URL, null)
        );

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

        Map<String, Object> qviCredBody = castObjectToLinkedHashMap(qviCred);
        Map<String, Object> sadQviCred = castObjectToLinkedHashMap(qviCredBody.get("sad"));
        Object qviCredHolder = getReceivedCredential(qviClient, sadQviCred.get("d").toString());

        if (qviCredHolder == null) {
            sendGrantMessage(gleifClient, gleifAid, qviAid, qviCredBody);
            sendAdmitMessage(qviClient, qviAid, gleifAid);
        }

        qviCredHolder = retry(() -> {
            try {
                Object cred = getReceivedCredential(qviClient, sadQviCred.get("d").toString());
                assert (cred != null);
                return cred;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CRED_RETRY_DEFAULTS);

        Map<String, Object> qviCredHolderBody = castObjectToLinkedHashMap(qviCredHolder);
        Map<String, Object> sadQviCredHolder = castObjectToLinkedHashMap(qviCredHolderBody.get("sad"));
        Map<String, Object> a = castObjectToLinkedHashMap(sadQviCredHolder.get("a"));
        Map<String, Object> statusBody = castObjectToLinkedHashMap(qviCredHolderBody.get("status"));

        assertEquals(sadQviCredHolder.get("d").toString(), sadQviCred.get("d").toString());
        assertEquals(sadQviCredHolder.get("s").toString(), QVI_SCHEMA_SAID);
        assertEquals(sadQviCredHolder.get("i").toString(), gleifAid.prefix);
        assertEquals(a.get("i").toString(), qviAid.prefix);
        assertEquals("0", statusBody.get("s").toString());
        assertNotNull(qviCredHolderBody.get("atc"));

        System.out.println("Issuing LE vLEI Credential");

        Map<String, Object> qvi = new LinkedHashMap<>();
        qvi.put("n", sadQviCred.get("d").toString());
        qvi.put("s", sadQviCred.get("s").toString());

        Map<String, Object> leCredSource = new LinkedHashMap<>();
        leCredSource.put("d", "");
        leCredSource.put("qvi", qvi);

        Object leCred = getOrIssueCredential(
                qviClient,
                qviAid,
                leAid,
                qviRegistry,
                leData,
                LE_SCHEMA_SAID,
                LE_RULES,
                leCredSource
        );
        Map<String, Object> leCredSourceBody = castObjectToLinkedHashMap(leCred);
        Map<String, Object> sadLeCred = castObjectToLinkedHashMap(leCredSourceBody.get("sad"));
        Object leCredHolder = getReceivedCredential(leClient, sadLeCred.get("d").toString());

        if (leCredHolder == null) {
            sendGrantMessage(qviClient, qviAid, leAid, leCredSourceBody);
            sendAdmitMessage(leClient, leAid, qviAid);

            leCredHolder = retry(() -> {
                try {
                    Object cred = getReceivedCredential(leClient, sadLeCred.get("d").toString());
                    assert (cred != null);
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        Map<String, Object> leCredHolderBody = castObjectToLinkedHashMap(leCredHolder);

        Map<String, Object> sadLeCredHolder = castObjectToLinkedHashMap(leCredHolderBody.get("sad"));
        Map<String, Object> aLeCredHolder = castObjectToLinkedHashMap(sadLeCredHolder.get("a"));
        Map<String, Object> eLeCredHolder = castObjectToLinkedHashMap(sadLeCredHolder.get("e"));
        Map<String, Object> qviLeCredHolder = castObjectToLinkedHashMap(eLeCredHolder.get("qvi"));
        Map<String, Object> statusLeCredHolder = castObjectToLinkedHashMap(leCredHolderBody.get("status"));

        assertEquals(sadLeCred.get("d").toString(), sadLeCredHolder.get("d").toString());
        assertEquals(LE_SCHEMA_SAID, sadLeCredHolder.get("s").toString());
        assertEquals(qviAid.prefix, sadLeCredHolder.get("i").toString());
        assertEquals(leAid.prefix, aLeCredHolder.get("i").toString());
        assertEquals(sadQviCred.get("d").toString(), qviLeCredHolder.get("n").toString());
        assertEquals("0", statusLeCredHolder.get("s").toString());
        assertNotNull(leCredHolderBody.get("atc"));

        System.out.println("Issuing ECR vLEI Credential from LE");

        Map<String, Object> le = new LinkedHashMap<>();
        le.put("n", sadLeCred.get("d").toString());
        le.put("s", sadLeCred.get("s").toString());

        Map<String, Object> ecrCredSource = new LinkedHashMap<>();
        ecrCredSource.put("d", "");
        ecrCredSource.put("le", le);

        Object ecrCred = getOrIssueCredential(
                leClient,
                leAid,
                roleAid,
                leRegistry,
                ecrData,
                ECR_SCHEMA_SAID,
                ECR_RULES,
                ecrCredSource,
                true
        );

        Map<String, Object> ecrCredBody = castObjectToLinkedHashMap(ecrCred);
        Map<String, Object> sadEcrCred = castObjectToLinkedHashMap(ecrCredBody.get("sad"));
        Object ecrCredHolder = getReceivedCredential(roleClient, sadEcrCred.get("d").toString());

        if (ecrCredHolder == null) {
            sendGrantMessage(leClient, leAid, roleAid, ecrCredBody);
            sendAdmitMessage(roleClient, roleAid, leAid);

            ecrCredHolder = retry(() -> {
                try {
                    assertNotNull(sadEcrCred.get("d").toString());
                    Object cred = getReceivedCredential(roleClient, sadEcrCred.get("d").toString());
                    assert (cred != null);
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        Map<String, Object> ecrCredHolderBody = castObjectToLinkedHashMap(ecrCredHolder);

        Map<String, Object> sadEcrCredHolder = castObjectToLinkedHashMap(ecrCredHolderBody.get("sad"));
        Map<String, Object> aEcrCredHolder = castObjectToLinkedHashMap(sadEcrCredHolder.get("a"));
        Map<String, Object> eEcrCredHolder = castObjectToLinkedHashMap(sadEcrCredHolder.get("e"));
        Map<String, Object> leEcrCredHolder = castObjectToLinkedHashMap(eEcrCredHolder.get("le"));
        Map<String, Object> statusEcrCredHolder = castObjectToLinkedHashMap(ecrCredHolderBody.get("status"));

        assertEquals(sadEcrCred.get("d").toString(), sadEcrCredHolder.get("d").toString());
        assertEquals(ECR_SCHEMA_SAID, sadEcrCredHolder.get("s").toString());
        assertEquals(leAid.prefix, sadEcrCredHolder.get("i").toString());
        assertEquals(roleAid.prefix, aEcrCredHolder.get("i").toString());
        assertEquals(sadLeCred.get("d").toString(), leEcrCredHolder.get("n").toString());
        assertEquals("0", statusEcrCredHolder.get("s").toString());
        assertNotNull(ecrCredHolderBody.get("atc"));

        System.out.println("Issuing ECR AUTH vLEI Credential");

        ecrAuthData.put("AID", roleAid.prefix);

        Map<String, Object> leErc = new LinkedHashMap<>();
        leErc.put("n", sadLeCred.get("d").toString());
        leErc.put("s", sadLeCred.get("s").toString());

        Map<String, Object> ecrAuthCredSource = new LinkedHashMap<>();
        ecrAuthCredSource.put("d", "");
        ecrAuthCredSource.put("le", leErc);

        Object ecrAuthCred = getOrIssueCredential(
                leClient,
                leAid,
                qviAid,
                leRegistry,
                ecrAuthData,
                ECR_AUTH_SCHEMA_SAID,
                ECR_AUTH_RULES,
                ecrAuthCredSource
        );
        Map<String, Object> ecrAuthCredBody = castObjectToLinkedHashMap(ecrAuthCred);
        Map<String, Object> sadEcrAuthCred = castObjectToLinkedHashMap(ecrAuthCredBody.get("sad"));
        Object ecrAuthCredHolder = getReceivedCredential(roleClient, sadEcrAuthCred.get("d").toString());

        if (ecrAuthCredHolder == null) {
            sendGrantMessage(leClient, leAid, qviAid, ecrAuthCredBody);
            sendAdmitMessage(qviClient, qviAid, leAid);

            ecrAuthCredHolder = retry(() -> {
                try {
                    Object cred = getReceivedCredential(qviClient, sadEcrAuthCred.get("d").toString());
                    assert (cred != null);
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }
        Map<String, Object> ecrAuthCredHolderBody = castObjectToLinkedHashMap(ecrAuthCredHolder);

        Map<String, Object> sadEcrAuthCredHolder = castObjectToLinkedHashMap(ecrAuthCredHolderBody.get("sad"));
        Map<String, Object> aEcrAuthCredHolder = castObjectToLinkedHashMap(sadEcrAuthCredHolder.get("a"));
        Map<String, Object> eEcrAuthCredHolder = castObjectToLinkedHashMap(sadEcrAuthCredHolder.get("e"));
        Map<String, Object> leEcrAuthCredHolder = castObjectToLinkedHashMap(eEcrAuthCredHolder.get("le"));
        Map<String, Object> statusEcrAuthCredHolder = castObjectToLinkedHashMap(ecrAuthCredHolderBody.get("status"));

        assertEquals(sadEcrAuthCred.get("d").toString(), sadEcrAuthCredHolder.get("d").toString());
        assertEquals(ECR_AUTH_SCHEMA_SAID, sadEcrAuthCredHolder.get("s").toString());
        assertEquals(leAid.prefix, sadEcrAuthCredHolder.get("i").toString());
        assertEquals(qviAid.prefix, aEcrAuthCredHolder.get("i").toString());
        assertEquals(roleAid.prefix, aEcrAuthCredHolder.get("AID").toString());
        assertEquals(sadLeCred.get("d").toString(), leEcrAuthCredHolder.get("n").toString());
        assertEquals("0", statusEcrAuthCredHolder.get("s").toString());
        assertNotNull(ecrAuthCredHolderBody.get("atc"));

        System.out.println("Issuing ECR vLEI Credential from ECR AUTH");

        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("n", sadEcrAuthCred.get("d").toString());
        auth.put("s", sadEcrAuthCred.get("s").toString());
        auth.put("o", "I2I");

        Map<String, Object> ecrCredSource2 = new LinkedHashMap<>();
        ecrCredSource2.put("d", "");
        ecrCredSource2.put("auth", auth);

        Object ecrCred2 = getOrIssueCredential(
                qviClient,
                qviAid,
                roleAid,
                qviRegistry,
                ecrData,
                ECR_SCHEMA_SAID,
                ECR_RULES,
                ecrCredSource2,
                true
        );
        Map<String, Object> ecrCred2Body = castObjectToLinkedHashMap(ecrCred2);
        Map<String, Object> sadEcrCred2 = castObjectToLinkedHashMap(ecrCred2Body.get("sad"));
        Object ecrCredHolder2 = getReceivedCredential(roleClient, sadEcrCred2.get("d").toString());

        if (ecrCredHolder2 == null) {
            sendGrantMessage(qviClient, qviAid, roleAid, ecrCred2Body);
            sendAdmitMessage(roleClient, roleAid, qviAid);

            ecrCredHolder2 = retry(() -> {
                try {
                    Object cred = getReceivedCredential(roleClient, sadEcrCred2.get("d").toString());
                    assert (cred != null);
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }
        Map<String, Object> ecrCredHolder2Body = castObjectToLinkedHashMap(ecrCredHolder2);

        Map<String, Object> sadEcrCredHolder2 = castObjectToLinkedHashMap(ecrCredHolder2Body.get("sad"));
        Map<String, Object> eEcrCredHolder2 = castObjectToLinkedHashMap(sadEcrCredHolder2.get("e"));
        Map<String, Object> authEcrCredHolder2 = castObjectToLinkedHashMap(eEcrCredHolder2.get("auth"));
        Map<String, Object> statusEcrCredHolder2 = castObjectToLinkedHashMap(ecrCredHolder2Body.get("status"));

        assertEquals(sadEcrCred2.get("d").toString(), sadEcrCredHolder2.get("d").toString());
        assertEquals(ECR_SCHEMA_SAID, sadEcrCredHolder2.get("s").toString());
        assertEquals(qviAid.prefix, sadEcrCredHolder2.get("i").toString());
        assertEquals(sadEcrAuthCred.get("d").toString(), authEcrCredHolder2.get("n").toString());
        assertEquals("0", statusEcrCredHolder2.get("s").toString());
        assertNotNull(ecrCredHolder2Body.get("atc"));

        System.out.println("Issuing OOR AUTH vLEI Credential");
        oorAuthData.put("AID", roleAid.prefix);

        le = new LinkedHashMap<>();
        le.put("n", sadLeCred.get("d").toString());
        le.put("s", sadLeCred.get("s").toString());

        Map<String, Object> oorAuthCredSource = new LinkedHashMap<>();
        oorAuthCredSource.put("d", "");
        oorAuthCredSource.put("le", le);

        Object oorAuthCred = getOrIssueCredential(
                leClient,
                leAid,
                qviAid,
                leRegistry,
                oorAuthData,
                OOR_AUTH_SCHEMA_SAID,
                OOR_AUTH_RULES,
                oorAuthCredSource
        );
        Map<String, Object> oorAuthCredBody = castObjectToLinkedHashMap(oorAuthCred);
        Map<String, Object> sadOorAuthCred = castObjectToLinkedHashMap(oorAuthCredBody.get("sad"));
        Object oorAuthCredHolder = getReceivedCredential(qviClient, sadOorAuthCred.get("d").toString());

        if (oorAuthCredHolder == null) {
            sendGrantMessage(leClient, leAid, qviAid, oorAuthCredBody);
            sendAdmitMessage(qviClient, qviAid, leAid);

            oorAuthCredHolder = retry(() -> {
                try {
                    Object cred = getReceivedCredential(qviClient, sadOorAuthCred.get("d").toString());
                    assert (cred != null);
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }
        Map<String, Object> oorAuthCredHolderBody = castObjectToLinkedHashMap(oorAuthCredHolder);

        Map<String, Object> sadOorAuthCredHolder = castObjectToLinkedHashMap(oorAuthCredHolderBody.get("sad"));
        Map<String, Object> aOorAuthCredHolder = castObjectToLinkedHashMap(sadOorAuthCredHolder.get("a"));
        Map<String, Object> eOorAuthCredHolder = castObjectToLinkedHashMap(sadOorAuthCredHolder.get("e"));
        Map<String, Object> leOorAuthCredHolder = castObjectToLinkedHashMap(eOorAuthCredHolder.get("le"));
        Map<String, Object> statusOorAuthCredHolder = castObjectToLinkedHashMap(oorAuthCredHolderBody.get("status"));

        assertEquals(sadOorAuthCred.get("d").toString(), sadOorAuthCredHolder.get("d").toString());
        assertEquals(OOR_AUTH_SCHEMA_SAID, sadOorAuthCredHolder.get("s").toString());
        assertEquals(leAid.prefix, sadOorAuthCredHolder.get("i").toString());
        assertEquals(qviAid.prefix, aOorAuthCredHolder.get("i").toString());
        assertEquals(roleAid.prefix, aOorAuthCredHolder.get("AID").toString());
        assertEquals(sadLeCred.get("d").toString(), leOorAuthCredHolder.get("n").toString());
        assertEquals("0", statusOorAuthCredHolder.get("s").toString());
        assertNotNull(oorAuthCredHolderBody.get("atc"));

        System.out.println("Issuing OOR vLEI Credential from OOR AUTH");

        auth = new LinkedHashMap<>();
        auth.put("n", sadOorAuthCred.get("d").toString());
        auth.put("s", sadOorAuthCred.get("s").toString());
        auth.put("o", "I2I");

        Map<String, Object> oorCredSource = new LinkedHashMap<>();
        oorCredSource.put("d", "");
        oorCredSource.put("auth", auth);

        Object oorCred = getOrIssueCredential(
                qviClient,
                qviAid,
                roleAid,
                qviRegistry,
                oorData,
                OOR_SCHEMA_SAID,
                OOR_RULES,
                oorCredSource
        );
        Map<String, Object> oorCredBody = castObjectToLinkedHashMap(oorCred);
        Map<String, Object> sadOorCred = castObjectToLinkedHashMap(oorCredBody.get("sad"));
        Object oorCredHolder = getReceivedCredential(qviClient, sadOorCred.get("d").toString());

        if (oorCredHolder == null) {
            sendGrantMessage(qviClient, qviAid, roleAid, oorCredBody);
            sendAdmitMessage(roleClient, roleAid, qviAid);

            oorCredHolder = retry(() -> {
                try {
                    Object cred = getReceivedCredential(roleClient, sadOorCred.get("d").toString());
                    assert (cred != null);
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }
        Map<String, Object> oorCredHolderBody = castObjectToLinkedHashMap(oorCredHolder);

        Map<String, Object> sadOorCredHolder = castObjectToLinkedHashMap(oorCredHolderBody.get("sad"));
        Map<String, Object> eOorCredHolder = castObjectToLinkedHashMap(sadOorCredHolder.get("e"));
        Map<String, Object> authOorCredHolder = castObjectToLinkedHashMap(eOorCredHolder.get("auth"));
        Map<String, Object> statusOorCredHolder = castObjectToLinkedHashMap(oorCredHolderBody.get("status"));

        assertEquals(sadOorCred.get("d").toString(), sadOorCredHolder.get("d").toString());
        assertEquals(OOR_SCHEMA_SAID, sadOorCredHolder.get("s").toString());
        assertEquals(qviAid.prefix, sadOorCredHolder.get("i").toString());
        assertEquals(sadOorAuthCred.get("d").toString(), authOorCredHolder.get("n").toString());
        assertEquals("0", statusOorCredHolder.get("s").toString());
        assertNotNull(oorCredHolderBody.get("atc"));

        List<SignifyClient> clientList = Arrays.asList(
                gleifClient,
                qviClient,
                leClient,
                roleClient
        );
        assertOperations(clientList);
        warnNotifications(clientList);
    }

    public IssuerRegistry getOrCreateRegistry(SignifyClient client, Aid aid, String registryName) throws Exception {
        IssuerRegistry registry = IssuerRegistry.builder().build();
        Object registries = client.registries().list(aid.name);
        ArrayList<String> registriesBody = (ArrayList<String>) registries;
        if (!registriesBody.isEmpty()) {
            assertEquals(1, registriesBody.size());
        } else {
            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(aid.name);
            registryArgs.setRegistryName(registryName);

            RegistryResult regResult = client.registries().create(registryArgs);
            waitOperation(client, regResult.op());
            registries = client.registries().list(aid.name);

            registriesBody = (ArrayList<String>) registries;
            LinkedHashMap<String, Object> registryBody = castObjectToLinkedHashMap(registriesBody.getFirst());
            registry.setName(registryBody.get("name").toString());
            registry.setRegk(registryBody.get("regk").toString());
        }
        return registry;
    }

    public void sendGrantMessage(SignifyClient senderClient, Aid senderAid, Aid recipientAid, Map<String, Object> credential) throws Exception {
        Map<String, Object> sad = castObjectToLinkedHashMap(credential.get("sad"));
        Map<String, Object> anc = castObjectToLinkedHashMap(credential.get("anc"));
        Map<String, Object> iss = castObjectToLinkedHashMap(credential.get("iss"));

        IpexGrantArgs grantArgs = IpexGrantArgs.builder()
                .senderName(senderAid.name)
                .acdc(new Serder(sad))
                .anc(new Serder(anc))
                .iss(new Serder(iss))
                .ancAttachment(null)
                .recipient(recipientAid.prefix)
                .datetime(createTimestamp())
                .build();

        Exchanging.ExchangeMessageResult result = senderClient.ipex().grant(grantArgs);
        Object op = senderClient.ipex().submitGrant(
                senderAid.name,
                result.exn(),
                result.sigs(),
                result.atc(),
                Collections.singletonList(recipientAid.prefix)
        );
        waitOperation(senderClient, op);
    }

    public void sendAdmitMessage(SignifyClient senderClient, Aid senderAid, Aid recipientAid) throws Exception {
        Thread.sleep(2000);
        List<Notification> notifications = waitForNotifications(senderClient, "/exn/ipex/grant");
        assertEquals(1, notifications.size());
        Notification grantNotification = notifications.getFirst();

        IpexAdmitArgs admitArgs = IpexAdmitArgs.builder()
                .senderName(senderAid.name)
                .message("")
                .grantSaid(grantNotification.a.d)
                .recipient(recipientAid.prefix)
                .datetime(createTimestamp())
                .build();
        Exchanging.ExchangeMessageResult result = senderClient.ipex().admit(admitArgs);

        Object op = senderClient.ipex().submitAdmit(
                senderAid.name,
                result.exn(),
                result.sigs(),
                result.atc(),
                Collections.singletonList(recipientAid.prefix)
        );
        waitOperation(senderClient, op);
        markAndRemoveNotification(senderClient, grantNotification);
    }

    public static class DataString {
        public static final String USAGE_DISCLAIMER = "Usage of a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, does not assert that the Legal Entity is trustworthy, honest, reputable in its business dealings, safe to do business with, or compliant with any laws or that an implied or expressly intended purpose will be fulfilled.";
        public static final String ISSUANCE_DISCLAIMER = "All information in a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, is accurate as of the date the validation process was complete. The vLEI Credential has been issued to the legal entity or person named in the vLEI Credential as the subject; and the qualified vLEI Issuer exercised reasonable care to perform the validation process set forth in the vLEI Ecosystem Governance Framework.";
        public static final String PRIVACY_DISCLAIMER = "It is the sole responsibility of Holders as Issuees of an ECR vLEI Credential to present that Credential in a privacy-preserving manner using the mechanisms provided in the Issuance and Presentation Exchange (IPEX) protocol specification and the Authentic Chained Data Container (ACDC) specification. https://github.com/WebOfTrust/IETF-IPEX and https://github.com/trustoverip/tswg-acdc-specification.";
        public static final String PRIVACY_DISCLAIMER_1 = "Privacy Considerations are applicable to QVI ECR AUTH vLEI Credentials.  It is the sole responsibility of QVIs as Issuees of QVI ECR AUTH vLEI Credentials to present these Credentials in a privacy-preserving manner using the mechanisms provided in the Issuance and Presentation Exchange (IPEX) protocol specification and the Authentic Chained Data Container (ACDC) specification.  https://github.com/WebOfTrust/IETF-IPEX and https://github.com/trustoverip/tswg-acdc-specification.";
    }
}
