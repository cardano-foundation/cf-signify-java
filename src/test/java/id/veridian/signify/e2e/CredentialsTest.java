package id.veridian.signify.e2e;

import id.veridian.signify.app.credentialing.credentials.CredentialRecord;
import id.veridian.signify.app.Exchanging;
import id.veridian.signify.app.ExnMessages.IpexAgreeExchange;
import id.veridian.signify.app.ExnMessages.IpexApplyExchange;
import id.veridian.signify.app.ExnMessages.IpexGrantExchange;
import id.veridian.signify.app.ExnMessages.IpexOfferExchange;
import static id.veridian.signify.app.ExnMessages.IPEX_ADMIT_ROUTE;
import static id.veridian.signify.app.ExnMessages.IPEX_AGREE_ROUTE;
import static id.veridian.signify.app.ExnMessages.IPEX_APPLY_ROUTE;
import static id.veridian.signify.app.ExnMessages.IPEX_GRANT_ROUTE;
import static id.veridian.signify.app.ExnMessages.IPEX_OFFER_ROUTE;
import id.veridian.signify.app.clienting.SignifyClient;
import id.veridian.signify.app.credentialing.credentials.*;
import id.veridian.signify.app.credentialing.ipex.*;
import id.veridian.signify.app.credentialing.registries.CreateRegistryArgs;
import id.veridian.signify.app.credentialing.registries.RegistryResult;
import id.veridian.signify.cesr.Serder;
import id.veridian.signify.cesr.util.CoreUtil;
import id.veridian.signify.cesr.util.Utils;
import id.veridian.signify.e2e.utils.ResolveEnv;
import id.veridian.signify.e2e.utils.Retry;
import id.veridian.signify.e2e.utils.TestSteps;
import id.veridian.signify.e2e.utils.TestUtils;
import id.veridian.signify.generated.keria.model.Notification;
import id.veridian.signify.generated.keria.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static id.veridian.signify.e2e.utils.Retry.retry;
import static id.veridian.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class CredentialsTest extends BaseIntegrationTest {
    private ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    private String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    private String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    private String vLEIServerHostUrl = env.vleiServerUrl() + "/oobi";
    private String QVI_SCHEMA_URL = vLEIServerHostUrl + "/" + QVI_SCHEMA_SAID;
    private String LE_SCHEMA_URL = vLEIServerHostUrl + "/" + LE_SCHEMA_SAID;
    TestSteps testSteps = new TestSteps();

    private static SignifyClient issuerClient, holderClient, verifierClient, legalEntityClient;
    private TestUtils.Aid issuerAid, holderAid, verifierAid, legalEntityAid;
    private String applySaid, offerSaid, agreeSaid;

    @BeforeAll
    public static void getClients() {
        List<SignifyClient> clients = getOrCreateClientsAsync(4);
        issuerClient = clients.get(0);
        holderClient = clients.get(1);
        verifierClient = clients.get(2);
        legalEntityClient = clients.get(3);
    }

    @BeforeEach
    public void getAid() {
        List<TestUtils.Aid> aids = createAidAsync(
                new CreateAidArgs(issuerClient, "issuer"),
                new CreateAidArgs(holderClient, "holder"),
                new CreateAidArgs(verifierClient, "verifier"),
                new CreateAidArgs(legalEntityClient, "legal-entity")
        );
        issuerAid = aids.get(0);
        holderAid = aids.get(1);
        verifierAid = aids.get(2);
        legalEntityAid = aids.get(3);
    }

    @BeforeEach
    public void getContact() {
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(issuerClient, "holder", holderAid.oobi),
                new GetOrCreateContactArgs(issuerClient, "verifier", verifierAid.oobi),
                new GetOrCreateContactArgs(holderClient, "issuer", issuerAid.oobi),
                new GetOrCreateContactArgs(holderClient, "verifier", verifierAid.oobi),
                new GetOrCreateContactArgs(holderClient, "legal-entity", legalEntityAid.oobi),
                new GetOrCreateContactArgs(verifierClient, "issuer", issuerAid.oobi),
                new GetOrCreateContactArgs(verifierClient, "holder", holderAid.oobi),
                new GetOrCreateContactArgs(legalEntityClient, "holder", holderAid.oobi)
        );
        System.out.println("Created contact successfully");
    }

    @AfterAll
    public static void cleanup() {
        List<SignifyClient> clients = Arrays.asList(
                issuerClient,
                holderClient,
                verifierClient,
                legalEntityClient
        );
        assertOperations(clients);
        assertNotifications(clients);
    }

    @Test
    public void single_signature_credentials() {
        testSteps.step("Resolve schema oobis", () -> {
            resolveOobisAsync(
                    new ResolveOobisArgs(issuerClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(issuerClient, LE_SCHEMA_URL, null),
                    new ResolveOobisArgs(holderClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(holderClient, LE_SCHEMA_URL, null),
                    new ResolveOobisArgs(verifierClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(verifierClient, LE_SCHEMA_URL, null),
                    new ResolveOobisArgs(legalEntityClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(legalEntityClient, LE_SCHEMA_URL, null)
            );
        });

        Registry registrys = testSteps.step("Create registry", () -> {
            String registryName = "vLEI-test-registry";
            String updatedRegistryName = "vLEI-test-registry-1";

            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(issuerAid.name);
            registryArgs.setRegistryName(registryName);
            RegistryResult regResult = issuerClient.registries().create(registryArgs);
            waitForCompleted(issuerClient, regResult.op());

            List<Registry> registriesList = issuerClient.registries().list(issuerAid.name);
            Registry registry = registriesList.get(0);
            assertEquals(1, registriesList.size());
            assertEquals(registryName, registry.getName());

            issuerClient.registries().rename(issuerAid.name, registryName, updatedRegistryName);
            registriesList = issuerClient.registries().list(issuerAid.name);
            Registry updatedRegistry = new Registry();
            updatedRegistry.setName(registriesList.get(0).getName());
            updatedRegistry.setRegk(registriesList.get(0).getRegk());
            assertEquals(1, registriesList.size());
            assertEquals(updatedRegistryName, updatedRegistry.getName());
            return updatedRegistry;
        });

        testSteps.step("Issuer can get schemas", () -> {
            Schema issuerQviSchema = issuerClient.schemas().get(QVI_SCHEMA_SAID).get();
            assertEquals(QVI_SCHEMA_SAID, issuerQviSchema.get$Id());

            Schema issuerLeSchema = issuerClient.schemas().get(LE_SCHEMA_SAID).get();
            assertEquals(LE_SCHEMA_SAID, issuerLeSchema.get$Id());
        });

        testSteps.step("Holder can list schemas", () -> {
            List<Schema> holderSchemas = holderClient.schemas().list();
            assertEquals(2, holderSchemas.size());
        });

        String qviCredentialId = testSteps.step("create QVI credential", () -> {
            Map<String, Object> vcdata = new HashMap<>();
            vcdata.put("LEI", "5493001KJTIIGC8Y1R17");

            CredentialData.CredentialSubject a = CredentialData.CredentialSubject.builder().build();
            a.setI(holderAid.prefix);
            a.setAdditionalProperties(vcdata);

            CredentialData cData = CredentialData.builder().build();
            cData.setRi(registrys.getRegk());
            cData.setS(QVI_SCHEMA_SAID);
            cData.setA(a);

            IssueCredentialResult issResult = issuerClient.credentials().issue(issuerAid.name, cData);
            waitForCompleted(issuerClient, issResult.getOp());
            return issResult.getAcdc().getKed().get("d").toString();
        });

        testSteps.step("Issuer list credentials", () -> {
            CredentialFilter credentialFilter = CredentialFilter.builder().build();
            List<CredentialRecord> issuerCredentials = issuerClient.credentials().list(credentialFilter);
            CredentialSad sad = issuerCredentials.getFirst().value().getSad();
            CredentialState status = issuerCredentials.getFirst().value().getStatus();

            assertTrue(!issuerCredentials.isEmpty());
            assertEquals(QVI_SCHEMA_SAID, sad.getS().toString());
            assertEquals(issuerAid.prefix, sad.getI().toString());
            assertEquals("0", status.getS().toString());
        });

        testSteps.step("Issuer list credentials with filter", () -> {
            Map<String, Object> filterData = new LinkedHashMap<>();
            filterData.put("-i", issuerAid.prefix);
            CredentialFilter credentialFilter = CredentialFilter.builder().build();
            credentialFilter.setFilter(filterData);
            List<CredentialRecord> list = issuerClient.credentials().list(credentialFilter);
            assertEquals(1, list.size());

            filterData.remove("-i");
            filterData.put("-s", QVI_SCHEMA_SAID);
            list = issuerClient.credentials().list(credentialFilter);
            assertEquals(1, list.size());

            filterData.remove("-s");
            filterData.put("-a-i", holderAid.prefix);
            list = issuerClient.credentials().list(credentialFilter);
            assertEquals(1, list.size());

            filterData.remove("-a-i");
            filterData.put("-i", issuerAid.prefix);
            filterData.put("-s", QVI_SCHEMA_SAID);
            filterData.put("-a-i", holderAid.prefix);
            list = issuerClient.credentials().list(credentialFilter);
            assertEquals(1, list.size());

            filterData.put("-i", UUID.randomUUID().toString());
            filterData.put("-s", QVI_SCHEMA_SAID);
            filterData.put("-a-i", holderAid.prefix);
            list = issuerClient.credentials().list(credentialFilter);
            assertEquals(0, list.size());
        });

        testSteps.step("Issuer get credential by id", () -> {
            CredentialRecord issuerCredential = issuerClient.credentials().get(qviCredentialId).get();
            CredentialSad sad = issuerCredential.value().getSad();
            CredentialState status = issuerCredential.value().getStatus();

            assertEquals(QVI_SCHEMA_SAID, sad.getS().toString());
            assertEquals(issuerAid.prefix, sad.getI().toString());
            assertEquals("0", status.getS().toString());
        });

        testSteps.step("Issuer IPEX grant", () -> {
            String dt = createTimestamp();
            CredentialRecord issuerCredential = issuerClient.credentials().get(qviCredentialId).get();

            IpexGrantArgs gArgs = IpexGrantArgs.builder().build();
            gArgs.setSenderName(issuerAid.name);
            gArgs.setAcdc(issuerCredential.acdc());
            gArgs.setAnc(issuerCredential.anc());
            gArgs.setIss(issuerCredential.iss());
            gArgs.setAncAttachment(null);
            gArgs.setRecipient(holderAid.prefix);
            gArgs.setDatetime(dt);

            Exchanging.ExchangeMessageResult result = issuerClient.ipex().grant(gArgs);
            List<String> holderAidPrefix = Collections.singletonList(holderAid.prefix);
            ExchangeOperation op = issuerClient.ipex().submitGrant(issuerAid.name, result.exn(), result.sigs(), result.atc(), holderAidPrefix);
            waitForCompleted(issuerClient, op);
        });

        testSteps.step("Holder can get the credential status before or without holding", () -> {
            Map<String, Object> state = Utils.toMap(Retry.retry(() ->
                    holderClient.credentials().state(registrys.getRegk(), qviCredentialId).get()));

            assertEquals(qviCredentialId, state.get("i"));
            assertEquals(registrys.getRegk(), state.get("ri"));
            String et = String.valueOf(state.get("et"));
            assertTrue(CoreUtil.Ilks.ISS.getValue().equals(et) || "bis".equals(et));
        });

        testSteps.step("holder IPEX admit", () -> {
            List<Notification> holderNotifications = waitForNotifications(holderClient, "/exn" + IPEX_GRANT_ROUTE);
            Notification grantNotification = holderNotifications.getFirst();

            IpexAdmitArgs iargs = IpexAdmitArgs.builder().build();
            iargs.setSenderName(holderAid.name);
            iargs.setMessage("");
            iargs.setGrantSaid(grantNotification.getA().getD());
            iargs.setRecipient(issuerAid.prefix);
            iargs.setDatetime(createTimestamp());

            Exchanging.ExchangeMessageResult result = holderClient.ipex().admit(iargs);
            ExchangeOperation op = holderClient.ipex().submitAdmit(
                    holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(issuerAid.prefix)
            );
            waitForCompleted(holderClient, op);
            markAndRemoveNotification(holderClient, grantNotification);
        });

        testSteps.step("Issuer IPEX grant response", () -> {
            List<Notification> issuerNotifications = waitForNotifications(issuerClient, "/exn" + IPEX_ADMIT_ROUTE);
            markAndRemoveNotification(issuerClient, issuerNotifications.getFirst());
        });

        testSteps.step("Holder has credential", () -> {
            CredentialRecord holderCredential = holderClient.credentials().get(qviCredentialId).get();
            assertEquals(QVI_SCHEMA_SAID, holderCredential.value().getSad().getS());
            assertEquals(issuerAid.prefix, holderCredential.value().getSad().getI());
            assertEquals("0", holderCredential.value().getStatus().getS());
            assertNotNull(holderCredential.acdcAttachment());
        });

        testSteps.step("Verifier IPEX apply", () -> {
            IpexApplyArgs args = IpexApplyArgs.builder().build();
            args.setSenderName(verifierAid.name);
            args.setSchemaSaid(QVI_SCHEMA_SAID);
            args.setAttributes(Map.of("LEI", "5493001KJTIIGC8Y1R17"));
            args.setRecipient(holderAid.prefix);
            args.setDatetime(createTimestamp());

            Exchanging.ExchangeMessageResult result = verifierClient.ipex().apply(args);
            ExchangeOperation op = verifierClient.ipex().submitApply(
                    verifierAid.name, result.exn(), result.sigs(), Collections.singletonList(holderAid.prefix)
            );
            waitForCompleted(verifierClient, op);
        });

        testSteps.step("Holder IPEX apply receive and offer", () -> {
            List<Notification> holderNotifications = waitForNotifications(holderClient, "/exn" + IPEX_APPLY_ROUTE);
            Notification holderApplyNote = holderNotifications.getFirst();
            assertNotNull(holderApplyNote.getA().getD());

            IpexApplyExchange apply = holderClient.exchanges().get(holderApplyNote.getA().getD(), IpexApplyExchange.class).orElseThrow();
            applySaid = apply.exn().getD();

            Map<String, Object> aBody = apply.a();

            Map<String, Object> filter = new LinkedHashMap<>();
            filter.put("-s", aBody.get("s").toString());

            LinkedHashMap<String, Object> aAttributes = castObjectToLinkedHashMap(aBody.get("a"));
            for (Map.Entry<String, Object> entry : aAttributes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                filter.put("-a-" + key, value);
            }

            CredentialFilter cFilter = CredentialFilter.builder().build();
            cFilter.setFilter(filter);
            List<CredentialRecord> matchingCreds = holderClient.credentials().list(cFilter);
            assertEquals(1, matchingCreds.size());

            markAndRemoveNotification(holderClient, holderNotifications.getFirst());

            IpexOfferArgs offerArgs = IpexOfferArgs.builder().build();
            offerArgs.setSenderName(holderAid.name);
            offerArgs.setRecipient(verifierAid.prefix);
            offerArgs.setAcdc(matchingCreds.get(0).acdc());
            offerArgs.setApplySaid(applySaid);
            offerArgs.setDatetime(createTimestamp());

            Exchanging.ExchangeMessageResult result = holderClient.ipex().offer(offerArgs);
            ExchangeOperation op = holderClient.ipex().submitOffer(holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(verifierAid.prefix));
            waitForCompleted(holderClient, op);
        });

        testSteps.step("Verifier receive offer and agree", () -> {
            List<Notification> verifierNotifications = waitForNotifications(verifierClient, "/exn" + IPEX_OFFER_ROUTE);
            Notification verifierOfferNote = verifierNotifications.getFirst();
            assertNotNull(verifierOfferNote.getA().getD());

            IpexOfferExchange offer = verifierClient.exchanges().get(verifierOfferNote.getA().getD(), IpexOfferExchange.class).orElseThrow();

            offerSaid = offer.exn().getD();
            String p = offer.exn().getP();

            ACDCAttributes a = offer.e().acdc().value().getA();
            String LEI = a.getAdditionalProperties().get("LEI").toString();

            assertEquals(applySaid, p);
            assertEquals("5493001KJTIIGC8Y1R17", LEI);

            markAndRemoveNotification(verifierClient, verifierOfferNote);

            IpexAgreeArgs agreeArgs = IpexAgreeArgs.builder().build();
            agreeArgs.setSenderName(verifierAid.name);
            agreeArgs.setRecipient(holderAid.prefix);
            agreeArgs.setOfferSaid(offerSaid);
            agreeArgs.setDatetime(createTimestamp());

            Exchanging.ExchangeMessageResult result = verifierClient.ipex().agree(agreeArgs);
            ExchangeOperation op = verifierClient.ipex().submitAgree(
                    verifierAid.name, result.exn(), result.sigs(), Collections.singletonList(holderAid.prefix)
            );
            waitForCompleted(verifierClient, op);
        });

        testSteps.step("Holder IPEX receive agree and grant/present", () -> {
            List<Notification> holderNotifications = waitForNotifications(holderClient, "/exn" + IPEX_AGREE_ROUTE);
            Notification holderAgreeNote = holderNotifications.getFirst();
            assertNotNull(holderAgreeNote.getA().getD());

            IpexAgreeExchange agree = verifierClient.exchanges().get(holderAgreeNote.getA().getD(), IpexAgreeExchange.class).orElseThrow();
            agreeSaid = agree.exn().getD();
            String agreeP = agree.exn().getP();

            assertEquals(offerSaid, agreeP);

            markAndRemoveNotification(holderClient, holderAgreeNote);

            CredentialRecord holderCredential = holderClient.credentials().get(qviCredentialId).get();

            IpexGrantArgs grantArgs = IpexGrantArgs.builder().build();
            grantArgs.setSenderName(holderAid.name);
            grantArgs.setRecipient(verifierAid.prefix);
            grantArgs.setAcdc(holderCredential.acdc());
            grantArgs.setAnc(holderCredential.anc());
            grantArgs.setIss(holderCredential.iss());
            grantArgs.setAcdcAttachment(holderCredential.acdcAttachment());
            grantArgs.setAncAttachment(holderCredential.ancAttachment());
            grantArgs.setIssAttachment(holderCredential.issAttachment());
            grantArgs.setAgreeSaid(agreeSaid);
            grantArgs.setDatetime(createTimestamp());

            Exchanging.ExchangeMessageResult result = holderClient.ipex().grant(grantArgs);

            ExchangeOperation op = holderClient.ipex().submitGrant(
                    holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(verifierAid.prefix)
            );
            waitForCompleted(holderClient, op);
        });

        testSteps.step("Verifier receives IPEX grant", () -> {
            List<Notification> verifierNotifications = waitForNotifications(verifierClient, "/exn" + IPEX_GRANT_ROUTE);
            Notification verifierGrantNote = verifierNotifications.getFirst();
            assertNotNull(verifierGrantNote.getA().getD());

            IpexGrantExchange grant = holderClient.exchanges().get(verifierGrantNote.getA().getD(), IpexGrantExchange.class).orElseThrow();
            String p = grant.exn().getP();

            assertEquals(agreeSaid, p);

            IpexAdmitArgs admitArgs = IpexAdmitArgs.builder().build();
            admitArgs.setSenderName(verifierAid.name);
            admitArgs.setMessage("");
            admitArgs.setGrantSaid(verifierGrantNote.getA().getD());
            admitArgs.setRecipient(holderAid.prefix);
            admitArgs.setDatetime(createTimestamp());

            Exchanging.ExchangeMessageResult result = verifierClient.ipex().admit(admitArgs);
            ExchangeOperation op = verifierClient.ipex().submitAdmit(
                    verifierAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(holderAid.prefix)
            );
            waitForCompleted(verifierClient, op);
            markAndRemoveNotification(verifierClient, verifierGrantNote);
            CredentialRecord verifierCredential = verifierClient.credentials().get(qviCredentialId).get();

            CredentialSad sadObj = verifierCredential.value().getSad();
            CredentialState status = verifierCredential.value().getStatus();
            String s = sadObj.getS();
            String i = sadObj.getI();
            String sStatus = status.getS();

            assertEquals(QVI_SCHEMA_SAID, s);
            assertEquals(issuerAid.prefix, i);
            assertEquals("0", sStatus);
        });

        testSteps.step("Holder IPEX present response", () -> {
            List<Notification> holderNotifications = waitForNotifications(holderClient, "/exn" + IPEX_ADMIT_ROUTE);
            markAndRemoveNotification(holderClient, holderNotifications.getFirst());
        });

        Registry holderRegistry = testSteps.step("Holder create registry for LE credential", () -> {
            String registryName = "vLEI-test-registry";
            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(holderAid.name);
            registryArgs.setRegistryName(registryName);

            RegistryResult regResult = holderClient.registries().create(registryArgs);

            waitForCompleted(holderClient, regResult.op());
            List<Registry> registriesList = holderClient.registries().list(holderAid.name);

            assertTrue(!registriesList.isEmpty());
            return registriesList.getFirst();
        });

        String leCredentialId = testSteps.step("Holder create LE (chained) credential", () -> {
            CredentialRecord qviCredential = holderClient.credentials().get(qviCredentialId).get();
            CredentialSad sadBody = qviCredential.value().getSad();

            Map<String, Object> additionalProperties = new LinkedHashMap<>();
            additionalProperties.put("LEI", "5493001KJTIIGC8Y1R17");

            CredentialData.CredentialSubject cSubject = CredentialData.CredentialSubject.builder().build();
            cSubject.setI(legalEntityAid.prefix);
            cSubject.setAdditionalProperties(additionalProperties);

            Map<String, Object> usageDisclaimer = new LinkedHashMap<>();
            usageDisclaimer.put("l", StringData.USAGE_DISCLAIMER);
            Map<String, Object> issuanceDisclaimer = new LinkedHashMap<>();
            issuanceDisclaimer.put("l", StringData.ISSUANCE_DISCLAIMER);

            Map<String, Object> sad = new LinkedHashMap<>();
            sad.put("d", "");
            sad.put("usageDisclaimer", usageDisclaimer);
            sad.put("issuanceDisclaimer", issuanceDisclaimer);

            Map<String, Object> qvi = new LinkedHashMap<>();
            qvi.put("n", sadBody.getD());
            qvi.put("s", sadBody.getS());

            Map<String, Object> e = new LinkedHashMap<>();
            e.put("d", "");
            e.put("qvi", qvi);

            CredentialData cData = CredentialData.builder().build();
            cData.setA(cSubject);
            cData.setRi(holderRegistry.getRegk());
            cData.setS(LE_SCHEMA_SAID);
            cData.setR(sad);
            cData.setE(e);

            IssueCredentialResult result = holderClient.credentials().issue(holderAid.name, cData);
            waitForCompleted(holderClient, result.getOp());
            return result.getAcdc().getKed().get("d").toString();
        });

        testSteps.step("LE credential IPEX grant", () -> {
            String dt = createTimestamp();
            CredentialRecord leCredential = holderClient.credentials().get(leCredentialId)
                .orElseThrow(() -> new IllegalStateException("LE credential not found: " + leCredentialId));

            IpexGrantArgs grantArgs = IpexGrantArgs.builder().build();
            grantArgs.setSenderName(holderAid.name);
            grantArgs.setAcdc(leCredential.acdc());
            grantArgs.setAnc(leCredential.anc());
            grantArgs.setIss(leCredential.iss());
            grantArgs.setAncAttachment(null);
            grantArgs.setRecipient(legalEntityAid.prefix);
            grantArgs.setDatetime(dt);

            Exchanging.ExchangeMessageResult result = holderClient.ipex().grant(grantArgs);
            ExchangeOperation op = holderClient.ipex().submitGrant(
                    holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(legalEntityAid.prefix)
            );
            waitForCompleted(holderClient, op);
        });

        testSteps.step("Legal Entity IPEX admit", () -> {
            List<Notification> notifications = waitForNotifications(legalEntityClient, "/exn" + IPEX_GRANT_ROUTE);
            Notification grantNotification = notifications.getFirst();

            IpexAdmitArgs admitArgs = IpexAdmitArgs.builder().build();
            admitArgs.setSenderName(legalEntityAid.name);
            admitArgs.setMessage("");
            admitArgs.setGrantSaid(grantNotification.getA().getD());
            admitArgs.setRecipient(holderAid.prefix);
            admitArgs.setDatetime(createTimestamp());

            Exchanging.ExchangeMessageResult result = legalEntityClient.ipex().admit(admitArgs);
            ExchangeOperation op = legalEntityClient.ipex().submitAdmit(
                    legalEntityAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(holderAid.prefix)
            );
            waitForCompleted(legalEntityClient, op);
            markAndRemoveNotification(legalEntityClient, grantNotification);
        });

        testSteps.step("LE credential IPEX grant response", () -> {
            List<Notification> notifications = waitForNotifications(holderClient, "/exn" + IPEX_ADMIT_ROUTE);
            markAndRemoveNotification(holderClient, notifications.getFirst());
        });

        testSteps.step("Legal Entity has chained credential", () -> {
            CredentialRecord legalEntityCredential = retry(() -> {
                assertNotNull(leCredentialId);
                return legalEntityClient.credentials().get(leCredentialId)
                        .orElseThrow(() -> new IllegalStateException("LE credential not found: " + leCredentialId));
            });
            CredentialSad sad = legalEntityCredential.value().getSad();
            Map<String, Object> aMap = Utils.toMap(sad.getA());
            CredentialState status = legalEntityCredential.value().getStatus();
            List<Map<String, Object>> chains = legalEntityCredential.value().getChains();
            LinkedHashMap<String, Object> chainsBody = castObjectToLinkedHashMap(chains.getFirst());
            LinkedHashMap<String, Object> sadInChains = castObjectToLinkedHashMap(chainsBody.get("sad"));
            String atc = legalEntityCredential.acdcAttachment();

            assertEquals(LE_SCHEMA_SAID, sad.getS());
            assertEquals(holderAid.prefix, sad.getI());
            assertEquals(legalEntityAid.prefix, aMap.get("i").toString());
            assertEquals("0", status.getS());
            assertEquals(qviCredentialId, sadInChains.get("d").toString());
            assertNotNull(atc);
        });

        testSteps.step("Issuer revoke QVI credential", () -> {
            RevokeCredentialResult revokeOperation = issuerClient.credentials().revoke(issuerAid.name, qviCredentialId, null);
            waitForCompleted(issuerClient, revokeOperation.getOp());
            CredentialRecord issuerCredential = issuerClient.credentials().get(qviCredentialId).get();

            CredentialState status = issuerCredential.value().getStatus();

            assertEquals("1", status.getS());
        });
    }

    public static class StringData {
        public static final String USAGE_DISCLAIMER = "Usage of a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, does not assert that the Legal Entity is trustworthy, honest, reputable in its business dealings, safe to do business with, or compliant with any laws or that an implied or expressly intended purpose will be fulfilled.";
        public static final String ISSUANCE_DISCLAIMER = "All information in a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, is accurate as of the date the validation process was complete. The vLEI Credential has been issued to the legal entity or person named in the vLEI Credential as the subject; and the qualified vLEI Issuer exercised reasonable care to perform the validation process set forth in the vLEI Ecosystem Governance Framework.";
    }
}
