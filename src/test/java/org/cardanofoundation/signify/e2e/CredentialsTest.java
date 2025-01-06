package org.cardanofoundation.signify.e2e;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.credentials.*;
import org.cardanofoundation.signify.app.credentialing.ipex.*;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.DigestException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class CredentialsTest extends TestUtils {
    private ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    private String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    private String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    private String vLEIServerHostUrl = env.vleiServerUrl() + "/oobi";
    private String QVI_SCHEMA_URL = vLEIServerHostUrl + "/" + QVI_SCHEMA_SAID;
    private String LE_SCHEMA_URL = vLEIServerHostUrl + "/" + LE_SCHEMA_SAID;
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
    public void getAid() throws Exception {
        issuerAid = createAid(issuerClient, "issuer");
        holderAid = createAid(holderClient, "holder");
        verifierAid = createAid(verifierClient, "verifier");
        legalEntityAid = createAid(legalEntityClient, "legal-entity");
    }

    @BeforeEach
    public void getContact() throws SodiumException, IOException, InterruptedException {
        getOrCreateContact(issuerClient, "holder", holderAid.oobi);
        getOrCreateContact(issuerClient, "verifier", verifierAid.oobi);
        getOrCreateContact(holderClient, "issuer", issuerAid.oobi);
        getOrCreateContact(holderClient, "verifier", verifierAid.oobi);
        getOrCreateContact(holderClient, "legal-entity", legalEntityAid.oobi);
        getOrCreateContact(verifierClient, "issuer", issuerAid.oobi);
        getOrCreateContact(verifierClient, "holder", holderAid.oobi);
        getOrCreateContact(legalEntityClient, "holder", holderAid.oobi);
        System.out.println("Created contact successfully");
    }

    @AfterAll
    public static void cleanup() throws Exception {
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
    public void single_signature_credentials() throws Exception {
        testSteps.step("Resolve schema oobis", () -> {
            try {
                resolveOobi(issuerClient, QVI_SCHEMA_URL, null);
                resolveOobi(issuerClient, LE_SCHEMA_URL, null);
                resolveOobi(holderClient, QVI_SCHEMA_URL, null);
                resolveOobi(holderClient, LE_SCHEMA_URL, null);
                resolveOobi(verifierClient, QVI_SCHEMA_URL, null);
                resolveOobi(verifierClient, LE_SCHEMA_URL, null);
                resolveOobi(legalEntityClient, QVI_SCHEMA_URL, null);
                resolveOobi(legalEntityClient, LE_SCHEMA_URL, null);
            } catch (SodiumException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        HashMap<String, Object> registrys = testSteps.step("Create registry", () -> {
            String registryName = "vLEI-test-registry";
            String updatedRegistryName = "vLEI-test-registry-1";
            HashMap<String, Object> updateRegistry = new HashMap<>();

            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(issuerAid.name);
            registryArgs.setRegistryName(registryName);
            try {
                RegistryResult regResult = issuerClient.getRegistries().create(registryArgs);
                waitOperations(issuerClient, regResult.op());
            } catch (IOException | InterruptedException | SodiumException | DigestException e) {
                throw new RuntimeException(e);
            }
            try {
                Object registries = issuerClient.getRegistries().list(issuerAid.name);
                List<Map<String, Object>> registriesList = castObjectToListMap(registries);
                HashMap<String, String> registry = new HashMap<>();
                registry.put("name", registriesList.getFirst().get("name").toString());
                registry.put("regk", registriesList.getFirst().get("regk").toString());
                assertEquals(1, registriesList.size());
                assertEquals(registryName, registry.get("name"));
            } catch (IOException | InterruptedException | SodiumException e) {
                throw new RuntimeException(e);
            }

            try {
                issuerClient.getRegistries().rename(issuerAid.name, registryName, updatedRegistryName);
                Object registries = issuerClient.getRegistries().list(issuerAid.name);
                List<Map<String, Object>> registriesList = castObjectToListMap(registries);
                updateRegistry.put("name", registriesList.getFirst().get("name").toString());
                updateRegistry.put("regk", registriesList.getFirst().get("regk").toString());
                assertEquals(1, registriesList.size());
                assertEquals(updatedRegistryName, updateRegistry.get("name"));
            } catch (IOException | InterruptedException | SodiumException e) {
                throw new RuntimeException(e);
            }
            return updateRegistry;
        });

        testSteps.step("Issuer can get schemas", () -> {
            try {
                Object issuerQviSchema = issuerClient.getSchemas().get(QVI_SCHEMA_SAID);
                LinkedHashMap<String, Object> issuerQviSchemaList = castObjectToLinkedHashMap(issuerQviSchema);
                String issuerQviSchemaID = issuerQviSchemaList.get("$id").toString();
                assertEquals(issuerQviSchemaID, QVI_SCHEMA_SAID);

                Object issuerLeSchema = issuerClient.getSchemas().get(LE_SCHEMA_SAID);
                LinkedHashMap<String, Object> issuerLeSchemaList = castObjectToLinkedHashMap(issuerLeSchema);
                String issuerLeSchemaID = issuerLeSchemaList.get("$id").toString();
                assertEquals(issuerLeSchemaID, LE_SCHEMA_SAID);
            } catch (IOException | InterruptedException | SodiumException e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Holder can list schemas", () -> {
            try {
                Object holderSchemas = holderClient.getSchemas().list();
                List<Map<String, Object>> holderSchemasList = castObjectToListMap(holderSchemas);
                assertEquals(2, holderSchemasList.size());
            } catch (IOException | InterruptedException | SodiumException e) {
                throw new RuntimeException(e);
            }
        });

        String qviCredentialId = testSteps.step("create QVI credential", () -> {
            Map<String, Object> vcdata = new HashMap<>();
            vcdata.put("LEI", "5493001KJTIIGC8Y1R17");

            CredentialData.CredentialSubject a = CredentialData.CredentialSubject.builder().build();
            a.setI(holderAid.prefix);
            a.setAdditionalProperties(vcdata);

            CredentialData cData = CredentialData.builder().build();
            cData.setRi(registrys.get("regk").toString());
            cData.setS(QVI_SCHEMA_SAID);
            cData.setA(a);

            IssueCredentialResult issResult = issuerClient.getCredentials().issue(issuerAid.name, cData);
            waitOperations(issuerClient, issResult.getOp());
            return issResult.getAcdc().getKed().get("d").toString();
        });

        testSteps.step("Issuer list credentials", () -> {
            CredentialFilter credentialFilter = CredentialFilter.builder().build();
            try {
                Object issuerCredentials = issuerClient.getCredentials().list(credentialFilter);
                List<Map<String, Object>> issuerCredentialsList = castObjectToListMap(issuerCredentials);
                Object credentialsMap = issuerCredentialsList.getFirst().get("sad");
                LinkedHashMap<String, Object> sad = castObjectToLinkedHashMap(credentialsMap);
                credentialsMap = issuerCredentialsList.getFirst().get("status");
                LinkedHashMap<String, Object> status = castObjectToLinkedHashMap(credentialsMap);

                assertTrue(!issuerCredentialsList.isEmpty());
                assertEquals(QVI_SCHEMA_SAID, sad.get("s").toString());
                assertEquals("0", status.get("s").toString());
            } catch (SodiumException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Issuer list credentials with filter", () -> {
            Map<String, Object> filterData = new LinkedHashMap<>();
            filterData.put("-i", issuerAid.prefix);
            CredentialFilter credentialFilter = CredentialFilter.builder().build();
            credentialFilter.setFilter(filterData);
            try {
                List<Map<String, Object>> list = castObjectToListMap(issuerClient.getCredentials().list(credentialFilter));
                assertEquals(1, list.size());

                filterData.remove("-i");
                filterData.put("-s", QVI_SCHEMA_SAID);
                list = castObjectToListMap(issuerClient.getCredentials().list(credentialFilter));
                assertEquals(1, list.size());

                filterData.remove("-s");
                filterData.put("-a-i", holderAid.prefix);
                list = castObjectToListMap(issuerClient.getCredentials().list(credentialFilter));
                assertEquals(1, list.size());

                filterData.remove("-a-i");
                filterData.put("-i", issuerAid.prefix);
                filterData.put("-s", QVI_SCHEMA_SAID);
                filterData.put("-a-i", holderAid.prefix);
                list = castObjectToListMap(issuerClient.getCredentials().list(credentialFilter));
                assertEquals(1, list.size());

                filterData.put("-i", UUID.randomUUID().toString());
                filterData.put("-s", QVI_SCHEMA_SAID);
                filterData.put("-a-i", holderAid.prefix);
                list = castObjectToListMap(issuerClient.getCredentials().list(credentialFilter));
                assertEquals(0, list.size());

            } catch (SodiumException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // TO-DO

        testSteps.step("Issuer get credential by id", () -> {
            try {
                Object issuerCredential = issuerClient.getCredentials().get(qviCredentialId);
                LinkedHashMap<String, Object> issuerCredentialsList = castObjectToLinkedHashMap(issuerCredential);
                Object credentialsMap = issuerCredentialsList.get("sad");
                LinkedHashMap<String, Object> sad = castObjectToLinkedHashMap(credentialsMap);
                credentialsMap = issuerCredentialsList.get("status");
                LinkedHashMap<String, Object> status = castObjectToLinkedHashMap(credentialsMap);

                assertEquals(QVI_SCHEMA_SAID, sad.get("s").toString());
                assertEquals(issuerAid.prefix, sad.get("i").toString());
                assertEquals("0", status.get("s").toString());
            } catch (SodiumException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Issuer IPEX grant", () -> {
            String dt = createTimestamp();
            try {
                Object issuerCredential = issuerClient.getCredentials().get(qviCredentialId);
                LinkedHashMap<String, Object> issuerCredentialList = castObjectToLinkedHashMap(issuerCredential);
                Map<String, Object> getSAD = (Map<String, Object>) issuerCredentialList.get("sad");
                Map<String, Object> getANC = (Map<String, Object>) issuerCredentialList.get("anc");
                Map<String, Object> getISS = (Map<String, Object>) issuerCredentialList.get("iss");
                assert issuerCredential != null;

                IpexGrantArgs gArgs = IpexGrantArgs.builder().build();
                gArgs.setSenderName(issuerAid.name);
                gArgs.setAcdc(new Serder(getSAD));
                gArgs.setAnc(new Serder(getANC));
                gArgs.setIss(new Serder(getISS));
                gArgs.setAncAttachment(null);
                gArgs.setRecipient(holderAid.prefix);
                gArgs.setDatetime(dt);

                Exchanging.ExchangeMessageResult result = issuerClient.getIpex().grant(gArgs);
                List<String> holderAidPrefix = Collections.singletonList(holderAid.prefix);
                Object op = issuerClient.getIpex().submitGrant(issuerAid.name, result.exn(), result.sigs(), result.atc(), holderAidPrefix);
                waitOperations(issuerClient, op);
            } catch (SodiumException | IOException | InterruptedException | DigestException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Holder can get the credential status before or without holding", () -> {
            CredentialState state = CredentialState.builder().build();
            try {
                Thread.sleep(2000);
                Object result = holderClient.getCredentials().state(registrys.get("regk").toString(), qviCredentialId);
                LinkedHashMap<String, Object> stateMap = castObjectToLinkedHashMap(result);

                ArrayList<Integer> list = (ArrayList<Integer>) stateMap.get("vn");
                int[] vn = list.stream().mapToInt(i -> i).toArray();

                LinkedHashMap<String, Object> aValue = castObjectToLinkedHashMap(stateMap.get("a"));
                CredentialState.A a = new CredentialState.A(parseInteger(aValue.get("s").toString()), aValue.get("d").toString());

                state.setVn(vn);
                state.setI(stateMap.get("i").toString());
                state.setS(stateMap.get("s").toString());
                state.setD(stateMap.get("d").toString());
                state.setRi(stateMap.get("ri").toString());
                state.setA(a);
                state.setDt(stateMap.get("dt").toString());
                state.setEt(stateMap.get("et").toString());
            } catch (SodiumException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertEquals(qviCredentialId, state.getI());
            assertEquals(registrys.get("regk").toString(), state.getRi());
            assertEquals(CoreUtil.Ilks.ISS.toString(), state.getEt().toUpperCase(Locale.ROOT));
        });

        testSteps.step("holder IPEX admit", () -> {
            try {
                List<Notification> holderNotifications = waitForNotifications(holderClient, "/exn/ipex/grant");
                Notification grantNotification = holderNotifications.getFirst();

                IpexAdmitArgs iargs = IpexAdmitArgs.builder().build();
                iargs.setSenderName(holderAid.name);
                iargs.setMessage("");
                iargs.setGrantSaid(grantNotification.a.d);
                iargs.setRecipient(issuerAid.prefix);
                iargs.setRecipient(createTimestamp());

                Exchanging.ExchangeMessageResult result = holderClient.getIpex().admit(iargs);
                Object op = holderClient.getIpex().submitAdmit(
                        holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(issuerAid.prefix)
                );
                waitOperations(holderClient, op);
                markAndRemoveNotification(holderClient, grantNotification);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Issuer IPEX grant response", () -> {
            List<Notification> issuerNotifications;
            try {
                issuerNotifications = waitForNotifications(issuerClient, "/exn/ipex/admit");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            markAndRemoveNotification(issuerClient, issuerNotifications.getFirst());
        });

        testSteps.step("Holder has credential", () -> {
            Map<String, Object> sad, status;
            String atc;
            try {
                Object holderCredential = holderClient.getCredentials().get(qviCredentialId);
                LinkedHashMap<String, Object> holderCredentialList = castObjectToLinkedHashMap(holderCredential);

                Object credentialsMap = holderCredentialList.get("sad");
                sad = castObjectToLinkedHashMap(credentialsMap);

                credentialsMap = holderCredentialList.get("status");
                status = castObjectToLinkedHashMap(credentialsMap);

                atc = holderCredentialList.get("atc").toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertEquals(QVI_SCHEMA_SAID, sad.get("s"));
            assertEquals(issuerAid.prefix, sad.get("i"));
            assertEquals("0", status.get("s"));
            assertNotNull(atc);
        });

        testSteps.step("Verifier IPEX apply", () -> {
            IpexApplyArgs args = IpexApplyArgs.builder().build();
            args.setSenderName(verifierAid.name);
            args.setSchemaSaid(QVI_SCHEMA_SAID);
            args.setAttributes(Map.of("LEI", "5493001KJTIIGC8Y1R17"));
            args.setRecipient(holderAid.prefix);
            args.setDatetime(createTimestamp());

            try {
                Exchanging.ExchangeMessageResult result = verifierClient.getIpex().apply(args);
                Object op = verifierClient.getIpex().submitApply(
                        verifierAid.name, result.exn(), result.sigs(), Collections.singletonList(holderAid.prefix)
                );
                waitOperations(verifierClient, op);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Holder IPEX apply receive and offer", () -> {
            List<Notification> holderNotifications;
            try {
                Thread.sleep(2000);
                holderNotifications = waitForNotifications(holderClient, "/exn/ipex/apply");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Notification holderApplyNote = holderNotifications.getFirst();
            assertNotNull(holderApplyNote.a.d);

            try {
                Object apply = holderClient.getExchanges().get(holderApplyNote.a.d);
                LinkedHashMap<String, Object> applyMap = castObjectToLinkedHashMap(apply);
                LinkedHashMap<String, Object> exn = castObjectToLinkedHashMap(applyMap.get("exn"));
                applySaid = exn.get("d").toString();

                LinkedHashMap<String, Object> aBody = castObjectToLinkedHashMap(exn.get("a"));

                Map<String, Object> filter = new LinkedHashMap<>();
                filter.put("-s", aBody.get("s").toString());

                LinkedHashMap<String, Object> a = castObjectToLinkedHashMap(aBody.get("a"));
                for (Map.Entry<String, Object> entry : a.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    filter.put("-a-" + key, value);
                }

                CredentialFilter cFilter = CredentialFilter.builder().build();
                cFilter.setFilter(filter);
                Object matchingCreds = holderClient.getCredentials().list(cFilter);
                ArrayList<String> matchingCredsMap = (ArrayList<String>) matchingCreds;
                assertEquals(1, matchingCredsMap.size());

                LinkedHashMap<String, Object> matchingCredsBody = castObjectToLinkedHashMap(matchingCredsMap.getFirst());
                Map<String, Object> sad = castObjectToLinkedHashMap(matchingCredsBody.get("sad"));

                markAndRemoveNotification(holderClient, holderNotifications.getFirst());

                IpexOfferArgs offerArgs = IpexOfferArgs.builder().build();
                offerArgs.setSenderName(holderAid.name);
                offerArgs.setRecipient(verifierAid.prefix);
                offerArgs.setAcdc(new Serder(sad));
                offerArgs.setApplySaid(applySaid);
                offerArgs.setDatetime(createTimestamp());

                Exchanging.ExchangeMessageResult result = holderClient.getIpex().offer(offerArgs);
                Object op = holderClient.getIpex().submitOffer(holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(verifierAid.prefix));
                waitOperations(holderClient, op);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Verifier receive offer and agree", () -> {
            List<Notification> verifierNotifications;
            try {
                Thread.sleep(2000);
                verifierNotifications = waitForNotifications(verifierClient, "/exn/ipex/offer");
                Notification verifierOfferNote = verifierNotifications.getFirst();
                assertNotNull(verifierOfferNote.a.d);

                Object offer = verifierClient.getExchanges().get(verifierOfferNote.a.d);
                LinkedHashMap<String, Object> offerBody = castObjectToLinkedHashMap(offer);
                LinkedHashMap<String, Object> exn = castObjectToLinkedHashMap(offerBody.get("exn"));

                offerSaid = exn.get("d").toString();
                String p = exn.get("p").toString();

                LinkedHashMap<String, Object> e = castObjectToLinkedHashMap(exn.get("e"));
                LinkedHashMap<String, Object> acdc = castObjectToLinkedHashMap(e.get("acdc"));
                LinkedHashMap<String, Object> a = castObjectToLinkedHashMap(acdc.get("a"));
                String LEI = a.get("LEI").toString();

                assertEquals(applySaid, p);
                assertEquals("5493001KJTIIGC8Y1R17", LEI);

                markAndRemoveNotification(verifierClient, verifierOfferNote);

                IpexAgreeArgs agreeArgs = IpexAgreeArgs.builder().build();
                agreeArgs.setSenderName(verifierAid.name);
                agreeArgs.setRecipient(holderAid.prefix);
                agreeArgs.setOfferSaid(offerSaid);
                agreeArgs.setDatetime(createTimestamp());

                Exchanging.ExchangeMessageResult result = verifierClient.getIpex().agree(agreeArgs);
                Object op = verifierClient.getIpex().submitAgree(
                        verifierAid.name, result.exn(), result.sigs(), Collections.singletonList(holderAid.prefix)
                );
                waitOperations(verifierClient, op);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Holder IPEX receive agree and grant/present", () -> {
            List<Notification> holderNotifications;
            try {
                Thread.sleep(2000);
                holderNotifications = waitForNotifications(holderClient, "/exn/ipex/agree");
                Notification holderAgreeNote = holderNotifications.getFirst();
                assertNotNull(holderAgreeNote.a.d);

                Object agree = verifierClient.getExchanges().get(holderAgreeNote.a.d);
                LinkedHashMap<String, Object> agreeBody = castObjectToLinkedHashMap(agree);
                LinkedHashMap<String, Object> exn = castObjectToLinkedHashMap(agreeBody.get("exn"));
                agreeSaid = exn.get("d").toString();
                String agreeP = exn.get("p").toString();

                assertEquals(offerSaid, agreeP);

                markAndRemoveNotification(holderClient, holderAgreeNote);

                Object holderCredential = holderClient.getCredentials().get(qviCredentialId);
                LinkedHashMap<String, Object> holderCredentialBody = castObjectToLinkedHashMap(holderCredential);
                LinkedHashMap<String, Object> sad = castObjectToLinkedHashMap(holderCredentialBody.get("sad"));
                LinkedHashMap<String, Object> anc = castObjectToLinkedHashMap(holderCredentialBody.get("anc"));
                LinkedHashMap<String, Object> iss = castObjectToLinkedHashMap(holderCredentialBody.get("iss"));
                String atc = holderCredentialBody.get("atc").toString();
                ArrayList<String> ancatcList = (ArrayList<String>) holderCredentialBody.get("ancatc");
                String ancatc = ancatcList.getFirst();
                String issAtc = holderCredentialBody.get("issatc").toString();

                IpexGrantArgs grantArgs = IpexGrantArgs.builder().build();
                grantArgs.setSenderName(holderAid.name);
                grantArgs.setRecipient(verifierAid.prefix);
                grantArgs.setAcdc(new Serder(sad));
                grantArgs.setAnc(new Serder(anc));
                grantArgs.setIss(new Serder(iss));
                grantArgs.setAcdcAttachment(atc);
                grantArgs.setAncAttachment(ancatc);
                grantArgs.setIssAttachment(issAtc);
                grantArgs.setAgreeSaid(agreeSaid);
                grantArgs.setDatetime(createTimestamp());

                Exchanging.ExchangeMessageResult result = holderClient.getIpex().grant(grantArgs);

                Object op = holderClient.getIpex().submitGrant(
                        holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(verifierAid.prefix)
                );
                waitOperations(holderClient, op);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Verifier receives IPEX grant", () -> {
            List<Notification> verifierNotifications;
            try {
                Thread.sleep(2000);
                verifierNotifications = waitForNotifications(verifierClient, "/exn/ipex/grant");
                Notification verifierGrantNote = verifierNotifications.getFirst();
                assertNotNull(verifierGrantNote.a.d);

                Object grant = holderClient.getExchanges().get(verifierGrantNote.a.d);
                LinkedHashMap<String, Object> grantBody = castObjectToLinkedHashMap(grant);
                LinkedHashMap<String, Object> exn = castObjectToLinkedHashMap(grantBody.get("exn"));
                String p = exn.get("p").toString();

                assertEquals(agreeSaid, p);

                IpexAdmitArgs admitArgs = IpexAdmitArgs.builder().build();
                admitArgs.setSenderName(verifierAid.name);
                admitArgs.setMessage("");
                admitArgs.setGrantSaid(verifierGrantNote.a.d);
                admitArgs.setRecipient(holderAid.prefix);
                admitArgs.setDatetime(createTimestamp());

                Exchanging.ExchangeMessageResult result = verifierClient.getIpex().admit(admitArgs);
                Object op = verifierClient.getIpex().submitAdmit(
                        verifierAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(holderAid.prefix)
                );
                waitOperations(verifierClient, op);
                markAndRemoveNotification(verifierClient, verifierGrantNote);
                Object verifierCredential = verifierClient.getCredentials().get(qviCredentialId);

                LinkedHashMap<String, Object> verifierCredentialBody = castObjectToLinkedHashMap(verifierCredential);
                LinkedHashMap<String, Object> sad = castObjectToLinkedHashMap(verifierCredentialBody.get("sad"));
                LinkedHashMap<String, Object> status = castObjectToLinkedHashMap(verifierCredentialBody.get("status"));
                String s = sad.get("s").toString();
                String i = sad.get("i").toString();
                String sStatus = status.get("s").toString();

                assertEquals(QVI_SCHEMA_SAID, s);
                assertEquals(issuerAid.prefix, i);
                assertEquals("0", sStatus);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Holder IPEX present response", () -> {
            try {
                List<Notification> holderNotifications = waitForNotifications(holderClient, "/exn/ipex/admit");
                markAndRemoveNotification(holderClient, holderNotifications.getFirst());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Map<String, Object> holderRegistry = testSteps.step("Holder create registry for LE credential", () -> {
            String registryName = "vLEI-test-registry";
            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(holderAid.name);
            registryArgs.setRegistryName(registryName);

            try {
                RegistryResult regResult = holderClient.getRegistries().create(registryArgs);

                waitOperations(holderClient, regResult.op());
                Object registries = holderClient.getRegistries().list(holderAid.name);
                List<Map<String, Object>> registriesList = castObjectToListMap(registries);

                assertTrue(!registriesList.isEmpty());
                return registriesList.getFirst();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        String leCredentialId = testSteps.step("Holder create LE (chained) credential", () -> {
            try {
                Object qviCredential = holderClient.getCredentials().get(qviCredentialId);
                LinkedHashMap<String, Object> qviCredentialBody = castObjectToLinkedHashMap(qviCredential);
                LinkedHashMap<String, Object> sadBody = castObjectToLinkedHashMap(qviCredentialBody.get("sad"));

                Map<String, Object> additionalProperties = new LinkedHashMap<>();
                additionalProperties.put("LEI", "5493001KJTIIGC8Y1R17");

                CredentialData.CredentialSubject cSubject  = CredentialData.CredentialSubject.builder().build();
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
                qvi.put("n", sadBody.get("d"));
                qvi.put("s", sadBody.get("s"));

                Map<String, Object> e = new LinkedHashMap<>();
                e.put("d", "");
                e.put("qvi", qvi);

                CredentialData cData = CredentialData.builder().build();
                cData.setA(cSubject);
                cData.setRi(holderRegistry.get("regk").toString());
                cData.setS(LE_SCHEMA_SAID);
                cData.setR(sad);
                cData.setE(e);

                IssueCredentialResult result = holderClient.getCredentials().issue(holderAid.name, cData);
                waitOperations(holderClient, result.getOp());
                return result.getAcdc().getKed().get("d").toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("LE credential IPEX grant", () -> {
            String dt = createTimestamp();
            try {
                Object leCredential = holderClient.getCredentials().get(leCredentialId);

                LinkedHashMap<String, Object> leCredentialBody = castObjectToLinkedHashMap(leCredential);
                assertTrue(!leCredentialBody.isEmpty());

                LinkedHashMap<String, Object> sad = castObjectToLinkedHashMap(leCredentialBody.get("sad"));
                LinkedHashMap<String, Object> anc = castObjectToLinkedHashMap(leCredentialBody.get("anc"));
                LinkedHashMap<String, Object> iss = castObjectToLinkedHashMap(leCredentialBody.get("iss"));

                IpexGrantArgs grantArgs = IpexGrantArgs.builder().build();
                grantArgs.setSenderName(holderAid.name);
                grantArgs.setAcdc(new Serder(sad));
                grantArgs.setAnc(new Serder(anc));
                grantArgs.setIss(new Serder(iss));
                grantArgs.setAncAttachment(null);
                grantArgs.setRecipient(legalEntityAid.prefix);
                grantArgs.setDatetime(dt);

                Exchanging.ExchangeMessageResult result = holderClient.getIpex().grant(grantArgs);
                Object op = holderClient.getIpex().submitGrant(
                        holderAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(legalEntityAid.prefix)
                );
                waitOperations(holderClient, op);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Legal Entity IPEX admit", () -> {
            try {
                Thread.sleep(2000);
                List<Notification> notifications = waitForNotifications(legalEntityClient, "/exn/ipex/grant");
                Notification grantNotification = notifications.getFirst();

                IpexAdmitArgs admitArgs = IpexAdmitArgs.builder().build();
                admitArgs.setSenderName(legalEntityAid.name);
                admitArgs.setMessage("");
                admitArgs.setGrantSaid(grantNotification.a.d);
                admitArgs.setRecipient(holderAid.prefix);
                admitArgs.setDatetime(createTimestamp());

                Exchanging.ExchangeMessageResult result = legalEntityClient.getIpex().admit(admitArgs);
                Object op = legalEntityClient.getIpex().submitAdmit(
                        legalEntityAid.name, result.exn(), result.sigs(), result.atc(), Collections.singletonList(holderAid.prefix)
                );
                waitOperations(legalEntityClient, op);
                markAndRemoveNotification(legalEntityClient, grantNotification);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("LE credential IPEX grant response", () -> {
            try {
                List<Notification> notifications = waitForNotifications(holderClient, "/exn/ipex/admit");
                markAndRemoveNotification(holderClient, notifications.getFirst());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Legal Entity has chained credential", () -> {
            try {
                Thread.sleep(2000);
                Object legalEntityCredential = legalEntityClient.getCredentials().get(leCredentialId);

                LinkedHashMap<String, Object> legalEntityCredentialBody = castObjectToLinkedHashMap(legalEntityCredential);
                LinkedHashMap<String, Object> sad = castObjectToLinkedHashMap(legalEntityCredentialBody.get("sad"));
                LinkedHashMap<String, Object> a = castObjectToLinkedHashMap(sad.get("a"));
                LinkedHashMap<String, Object> status = castObjectToLinkedHashMap(legalEntityCredentialBody.get("status"));
                ArrayList<String> chains = (ArrayList<String>) legalEntityCredentialBody.get("chains");
                LinkedHashMap<String, Object> chainsBody = castObjectToLinkedHashMap(chains.getFirst());
                LinkedHashMap<String, Object> sadInChains = castObjectToLinkedHashMap(chainsBody.get("sad"));
                String atc = legalEntityCredentialBody.get("atc").toString();

                assertEquals(LE_SCHEMA_SAID, sad.get("s").toString());
                assertEquals(holderAid.prefix, sad.get("i").toString());
                assertEquals(legalEntityAid.prefix, a.get("i").toString());
                assertEquals("0", status.get("s").toString());
                assertEquals(qviCredentialId, sadInChains.get("d").toString());
                assertNotNull(atc);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Issuer revoke QVI credential", () -> {
            try {
                RevokeCredentialResult revokeOperation = issuerClient.getCredentials().revoke(issuerAid.name, qviCredentialId, null);
                waitOperations(issuerClient, revokeOperation.getOp());
                Object issuerCredential = issuerClient.getCredentials().get(qviCredentialId);

                LinkedHashMap<String, Object> issuerCredentialBody = castObjectToLinkedHashMap(issuerCredential);
                LinkedHashMap<String, Object> status = castObjectToLinkedHashMap(issuerCredentialBody.get("status"));

                assertEquals("1", status.get("s").toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static class StringData {
        public static final String USAGE_DISCLAIMER = "Usage of a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, does not assert that the Legal Entity is trustworthy, honest, reputable in its business dealings, safe to do business with, or compliant with any laws or that an implied or expressly intended purpose will be fulfilled.";
        public static final String ISSUANCE_DISCLAIMER = "All information in a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, is accurate as of the date the validation process was complete. The vLEI Credential has been issued to the legal entity or person named in the vLEI Credential as the subject; and the qualified vLEI Issuer exercised reasonable care to perform the validation process set forth in the vLEI Ecosystem Governance Framework.";
    }
}
