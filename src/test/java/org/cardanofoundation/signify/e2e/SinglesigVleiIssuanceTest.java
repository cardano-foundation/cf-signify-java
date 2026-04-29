package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexAdmitArgs;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexGrantArgs;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.e2e.utils.IssuerRegistry;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.Retry;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.Notification;

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

    ObjectMapper mapper = new ObjectMapper();

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

        Credential qviCred = getOrIssueCredential(
                gleifClient,
                gleifAid,
                qviAid,
                gleifRegistry,
                qviData,
                QVI_SCHEMA_SAID,
                null,
                null
        );

        CredentialSad sadQviCred = qviCred.getSad();
        Credential qviCredHolder = getReceivedCredential(qviClient, sadQviCred.getD());

        if (qviCredHolder == null) {
            sendGrantMessage(gleifClient, gleifAid, qviAid, qviCred);
            sendAdmitMessage(qviClient, qviAid, gleifAid);
        }

        qviCredHolder = retry(() -> {
            try {
                Credential cred = getReceivedCredential(qviClient, sadQviCred.getD());
                if (cred == null) throw new RuntimeException("Credential not yet available");
                return cred;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, CRED_RETRY_DEFAULTS);

        CredentialSad qviCredHolderSad = qviCredHolder.getSad();

        assertEquals(qviCredHolderSad.getD(), sadQviCred.getD());
        assertEquals(qviCredHolderSad.getS(), QVI_SCHEMA_SAID);
        assertEquals(qviCredHolderSad.getI(), gleifAid.prefix);
        assertEquals(qviCredHolderSad.getA().getI(), qviAid.prefix);
        assertEquals("0", qviCredHolder.getStatus().getS());
        assertNotNull(qviCredHolder.getAtc());

        System.out.println("Issuing LE vLEI Credential");

        Map<String, Object> qvi = new LinkedHashMap<>();
        qvi.put("n", sadQviCred.getD());
        qvi.put("s", sadQviCred.getS());

        Map<String, Object> leCredSource = new LinkedHashMap<>();
        leCredSource.put("d", "");
        leCredSource.put("qvi", qvi);

        Credential leCred = getOrIssueCredential(
                qviClient,
                qviAid,
                leAid,
                qviRegistry,
                leData,
                LE_SCHEMA_SAID,
                LE_RULES,
                leCredSource
        );
        CredentialSad sadLeCred = leCred.getSad();
        Credential leCredHolder = getReceivedCredential(leClient, sadLeCred.getD());

        if (leCredHolder == null) {
            sendGrantMessage(qviClient, qviAid, leAid, leCred);
            sendAdmitMessage(leClient, leAid, qviAid);

            leCredHolder = retry(() -> {
                try {
                    Credential cred = getReceivedCredential(leClient, sadLeCred.getD());
                    if (cred == null) throw new RuntimeException("Credential not yet available");
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        CredentialSad sadLeCredHolder = leCredHolder.getSad();
        CredentialState statusLeCredHolder = leCredHolder.getStatus();
        JsonNode leQviEdge = mapper.convertValue(sadLeCredHolder.getE(), JsonNode.class);

        assertEquals(sadLeCred.getD(), sadLeCredHolder.getD());
        assertEquals(LE_SCHEMA_SAID, sadLeCredHolder.getS());
        assertEquals(qviAid.prefix, sadLeCredHolder.getI());
        assertEquals(leAid.prefix, sadLeCredHolder.getA().getI());
        assertEquals(sadQviCred.getD(), leQviEdge.at("/qvi/n").asText());

        assertEquals("0", statusLeCredHolder.getS());
        assertNotNull(leCredHolder.getAtc());

        System.out.println("Issuing ECR vLEI Credential from LE");

        Map<String, Object> le = new LinkedHashMap<>();
        le.put("n", sadLeCred.getD());
        le.put("s", sadLeCred.getS());

        Map<String, Object> ecrCredSource = new LinkedHashMap<>();
        ecrCredSource.put("d", "");
        ecrCredSource.put("le", le);

        Credential ecrCred = getOrIssueCredential(
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

        CredentialSad sadEcrCred = ecrCred.getSad();
        Credential ecrCredHolder = getReceivedCredential(roleClient, sadEcrCred.getD());

        if (ecrCredHolder == null) {
            sendGrantMessage(leClient, leAid, roleAid, ecrCred);
            sendAdmitMessage(roleClient, roleAid, leAid);

            ecrCredHolder = retry(() -> {
                try {
                    Credential cred = getReceivedCredential(roleClient, sadEcrCred.getD());
                    if (cred == null) throw new RuntimeException("Credential not yet available");
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        CredentialSad sadEcrCredHolder = ecrCredHolder.getSad();
        ACDCAttributes aEcrCredHolder = sadEcrCredHolder.getA();
        JsonNode ecrLeEdge = mapper.convertValue(sadEcrCredHolder.getE(), JsonNode.class);
        CredentialState statusEcrCredHolder = ecrCredHolder.getStatus();

        assertEquals(sadEcrCred.getD(), sadEcrCredHolder.getD());
        assertEquals(ECR_SCHEMA_SAID, sadEcrCredHolder.getS());
        assertEquals(leAid.prefix, sadEcrCredHolder.getI());
        assertEquals(roleAid.prefix, aEcrCredHolder.getI());
        assertEquals(sadLeCred.getD(), ecrLeEdge.at("/le/n").asText());
        assertEquals("0", statusEcrCredHolder.getS());
        assertNotNull(ecrCredHolder.getAtc());

        System.out.println("Issuing ECR AUTH vLEI Credential");

        ecrAuthData.put("AID", roleAid.prefix);

        Map<String, Object> leErc = new LinkedHashMap<>();
        leErc.put("n", sadLeCred.getD());
        leErc.put("s", sadLeCred.getS());

        Map<String, Object> ecrAuthCredSource = new LinkedHashMap<>();
        ecrAuthCredSource.put("d", "");
        ecrAuthCredSource.put("le", leErc);

        Credential ecrAuthCred = getOrIssueCredential(
                leClient,
                leAid,
                qviAid,
                leRegistry,
                ecrAuthData,
                ECR_AUTH_SCHEMA_SAID,
                ECR_AUTH_RULES,
                ecrAuthCredSource
        );
        CredentialSad sadEcrAuthCred = ecrAuthCred.getSad();
        Credential ecrAuthCredHolder = getReceivedCredential(roleClient, sadEcrAuthCred.getD());

        if (ecrAuthCredHolder == null) {
            sendGrantMessage(leClient, leAid, qviAid, ecrAuthCred);
            sendAdmitMessage(qviClient, qviAid, leAid);

            ecrAuthCredHolder = retry(() -> {
                try {
                    Credential cred = getReceivedCredential(qviClient, sadEcrAuthCred.getD());
                    if (cred == null) throw new RuntimeException("Credential not yet available");
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        CredentialSad sadEcrAuthCredHolder = ecrAuthCredHolder.getSad();
        ACDCAttributes aEcrAuthCredHolder = sadEcrAuthCredHolder.getA();
        JsonNode ecrAuthLeEdge = mapper.convertValue(sadEcrAuthCredHolder.getE(), JsonNode.class);
        CredentialState statusEcrAuthCredHolder = ecrAuthCredHolder.getStatus();

        assertEquals(sadEcrAuthCred.getD(), sadEcrAuthCredHolder.getD());
        assertEquals(ECR_AUTH_SCHEMA_SAID, sadEcrAuthCredHolder.getS());
        assertEquals(leAid.prefix, sadEcrAuthCredHolder.getI());
        assertEquals(qviAid.prefix, aEcrAuthCredHolder.getI());
        assertEquals(roleAid.prefix, aEcrAuthCredHolder.getAdditionalProperties().get("AID").toString());
        assertEquals(sadLeCred.getD(), ecrAuthLeEdge.at("/le/n").asText());
        assertEquals("0", statusEcrAuthCredHolder.getS());
        assertNotNull(ecrAuthCredHolder.getAtc());

        System.out.println("Issuing ECR vLEI Credential from ECR AUTH");

        Map<String, Object> auth = new LinkedHashMap<>();
        auth.put("n", sadEcrAuthCred.getD());
        auth.put("s", sadEcrAuthCred.getS());
        auth.put("o", "I2I");

        Map<String, Object> ecrCredSource2 = new LinkedHashMap<>();
        ecrCredSource2.put("d", "");
        ecrCredSource2.put("auth", auth);

        Credential ecrCred2 = getOrIssueCredential(
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
        CredentialSad sadEcrCred2 = ecrCred2.getSad();
        Credential ecrCredHolder2 = getReceivedCredential(roleClient, sadEcrCred2.getD());

        if (ecrCredHolder2 == null) {
            sendGrantMessage(qviClient, qviAid, roleAid, ecrCred2);
            sendAdmitMessage(roleClient, roleAid, qviAid);

            ecrCredHolder2 = retry(() -> {
                try {
                    Credential cred = getReceivedCredential(roleClient, sadEcrCred2.getD());
                    if (cred == null) throw new RuntimeException("Credential not yet available");
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        CredentialSad sadEcrCredHolder2 = ecrCredHolder2.getSad();
        JsonNode ecrEcrAuthEdges2 = mapper.convertValue(sadEcrCredHolder2.getE(), JsonNode.class);
        CredentialState statusEcrCredHolder2 = ecrCredHolder2.getStatus();

        assertEquals(sadEcrCred2.getD(), sadEcrCredHolder2.getD());
        assertEquals(ECR_SCHEMA_SAID, sadEcrCredHolder2.getS());
        assertEquals(qviAid.prefix, sadEcrCredHolder2.getI());
        assertEquals(sadEcrAuthCred.getD(), ecrEcrAuthEdges2.at("/auth/n").asText());
        assertEquals("0", statusEcrCredHolder2.getS());
        assertNotNull(ecrCredHolder2.getAtc());

        System.out.println("Issuing OOR AUTH vLEI Credential");
        oorAuthData.put("AID", roleAid.prefix);

        le = new LinkedHashMap<>();
        le.put("n", sadLeCred.getD());
        le.put("s", sadLeCred.getS());

        Map<String, Object> oorAuthCredSource = new LinkedHashMap<>();
        oorAuthCredSource.put("d", "");
        oorAuthCredSource.put("le", le);

        Credential oorAuthCred = getOrIssueCredential(
                leClient,
                leAid,
                qviAid,
                leRegistry,
                oorAuthData,
                OOR_AUTH_SCHEMA_SAID,
                OOR_AUTH_RULES,
                oorAuthCredSource
        );
        CredentialSad sadOorAuthCred = oorAuthCred.getSad();
        Credential oorAuthCredHolder = getReceivedCredential(qviClient, sadOorAuthCred.getD());

        if (oorAuthCredHolder == null) {
            sendGrantMessage(leClient, leAid, qviAid, oorAuthCred);
            sendAdmitMessage(qviClient, qviAid, leAid);

            oorAuthCredHolder = retry(() -> {
                try {
                    Credential cred = getReceivedCredential(qviClient, sadOorAuthCred.getD());
                    if (cred == null) throw new RuntimeException("Credential not yet available");
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        CredentialSad sadOorAuthCredHolder = oorAuthCredHolder.getSad();
        ACDCAttributes aOorAuthCredHolder = sadOorAuthCredHolder.getA();
        JsonNode oorAuthLeEdge = mapper.convertValue(sadOorAuthCredHolder.getE(), JsonNode.class);
        CredentialState statusOorAuthCredHolder = oorAuthCredHolder.getStatus();

        assertEquals(sadOorAuthCred.getD(), sadOorAuthCredHolder.getD());
        assertEquals(OOR_AUTH_SCHEMA_SAID, sadOorAuthCredHolder.getS());
        assertEquals(leAid.prefix, sadOorAuthCredHolder.getI());
        assertEquals(qviAid.prefix, aOorAuthCredHolder.getI());
        assertEquals(roleAid.prefix, aOorAuthCredHolder.getAdditionalProperties().get("AID").toString());
        assertEquals(sadLeCred.getD(), oorAuthLeEdge.at("/le/n").asText());
        assertEquals("0", statusOorAuthCredHolder.getS());
        assertNotNull(oorAuthCredHolder.getAtc());

        System.out.println("Issuing OOR vLEI Credential from OOR AUTH");

        auth = new LinkedHashMap<>();
        auth.put("n", sadOorAuthCred.getD());
        auth.put("s", sadOorAuthCred.getS());
        auth.put("o", "I2I");

        Map<String, Object> oorCredSource = new LinkedHashMap<>();
        oorCredSource.put("d", "");
        oorCredSource.put("auth", auth);

        Credential oorCred = getOrIssueCredential(
                qviClient,
                qviAid,
                roleAid,
                qviRegistry,
                oorData,
                OOR_SCHEMA_SAID,
                OOR_RULES,
                oorCredSource
        );
        CredentialSad sadOorCred = oorCred.getSad();
        Credential oorCredHolder = getReceivedCredential(qviClient, sadOorCred.getD());

        if (oorCredHolder == null) {
            sendGrantMessage(qviClient, qviAid, roleAid, oorCred);
            sendAdmitMessage(roleClient, roleAid, qviAid);

            oorCredHolder = retry(() -> {
                try {
                    Credential cred = getReceivedCredential(roleClient, sadOorCred.getD());
                    if (cred == null) throw new RuntimeException("Credential not yet available");
                    return cred;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, CRED_RETRY_DEFAULTS);
        }

        CredentialSad sadOorCredHolder = oorCredHolder.getSad();
        JsonNode oorOorAuthEdge = mapper.convertValue(sadOorCredHolder.getE(), JsonNode.class);
        CredentialState statusOorCredHolder = oorCredHolder.getStatus();

        assertEquals(sadOorCred.getD(), sadOorCredHolder.getD());
        assertEquals(OOR_SCHEMA_SAID, sadOorCredHolder.getS());
        assertEquals(qviAid.prefix, sadOorCredHolder.getI());
        assertEquals(sadOorAuthCred.getD(), oorOorAuthEdge.at("/auth/n").asText());
        assertEquals("0", statusOorCredHolder.getS());
        assertNotNull(oorCredHolder.getAtc());

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
        List<Registry> registriesList = client.registries().list(aid.name);
        if (!registriesList.isEmpty()) {
            assertEquals(1, registriesList.size());
        } else {
            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(aid.name);
            registryArgs.setRegistryName(registryName);

            RegistryResult regResult = client.registries().create(registryArgs);
            waitForCompleted(client, regResult.op());
            registriesList = client.registries().list(aid.name);

            Registry registryBody = registriesList.get(0);
            registry.setName(registryBody.getName());
            registry.setRegk(registryBody.getRegk());
        }
        return registry;
    }

    public void sendGrantMessage(SignifyClient senderClient, Aid senderAid, Aid recipientAid, Credential credential) throws Exception {
        IpexGrantArgs grantArgs = IpexGrantArgs.builder()
                .senderName(senderAid.name)
                .acdc(new Serder(Utils.toMap(credential.getSad())))
                .anc(new Serder(Utils.toMap(credential.getAnc())))
                .iss(new Serder(Utils.toMap(credential.getIss())))
                .recipient(recipientAid.prefix)
                .datetime(createTimestamp())
                .build();

        Exchanging.ExchangeMessageResult result = senderClient.ipex().grant(grantArgs);
        ExchangeOperation op = senderClient.ipex().submitGrant(
                senderAid.name,
                result.exn(),
                result.sigs(),
                result.atc(),
                Collections.singletonList(recipientAid.prefix)
        );
        waitForCompleted(senderClient, op);
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

        ExchangeOperation op = senderClient.ipex().submitAdmit(
                senderAid.name,
                result.exn(),
                result.sigs(),
                result.atc(),
                Collections.singletonList(recipientAid.prefix)
        );
        waitForCompleted(senderClient, op);
        markAndRemoveNotification(senderClient, grantNotification);
    }

    public static class DataString {
        public static final String USAGE_DISCLAIMER = "Usage of a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, does not assert that the Legal Entity is trustworthy, honest, reputable in its business dealings, safe to do business with, or compliant with any laws or that an implied or expressly intended purpose will be fulfilled.";
        public static final String ISSUANCE_DISCLAIMER = "All information in a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, is accurate as of the date the validation process was complete. The vLEI Credential has been issued to the legal entity or person named in the vLEI Credential as the subject; and the qualified vLEI Issuer exercised reasonable care to perform the validation process set forth in the vLEI Ecosystem Governance Framework.";
        public static final String PRIVACY_DISCLAIMER = "It is the sole responsibility of Holders as Issuees of an ECR vLEI Credential to present that Credential in a privacy-preserving manner using the mechanisms provided in the Issuance and Presentation Exchange (IPEX) protocol specification and the Authentic Chained Data Container (ACDC) specification. https://github.com/WebOfTrust/IETF-IPEX and https://github.com/trustoverip/tswg-acdc-specification.";
        public static final String PRIVACY_DISCLAIMER_1 = "Privacy Considerations are applicable to QVI ECR AUTH vLEI Credentials.  It is the sole responsibility of QVIs as Issuees of QVI ECR AUTH vLEI Credentials to present these Credentials in a privacy-preserving manner using the mechanisms provided in the Issuance and Presentation Exchange (IPEX) protocol specification and the Authentic Chained Data Container (ACDC) specification.  https://github.com/WebOfTrust/IETF-IPEX and https://github.com/trustoverip/tswg-acdc-specification.";
    }
}
