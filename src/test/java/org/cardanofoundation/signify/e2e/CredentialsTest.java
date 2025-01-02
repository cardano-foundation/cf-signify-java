package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import io.qameta.allure.model.Link;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialFilter;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialState;
import org.cardanofoundation.signify.app.credentialing.credentials.IssueCredentialResult;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexGrantArgs;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.Retry;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CredentialsTest extends TestUtils {
    private ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    private String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    private String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    private String vLEIServerHostUrl = env.vleiServerUrl() + "/oobi";
    private String QVI_SCHEMA_URL = vLEIServerHostUrl + "/" + QVI_SCHEMA_SAID;
    private String LE_SCHEMA_URL = vLEIServerHostUrl + "/" + LE_SCHEMA_SAID;
    TestSteps testSteps = new TestSteps();
    Retry retry = new Retry();
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
    public static void cleanup() throws SodiumException, IOException, InterruptedException {
        // TO-DO:
//        assertOperations(Collections.singletonList(issuerClient));
//        assertOperations(Collections.singletonList(holderClient));
//        assertOperations(Collections.singletonList(verifierClient));
//        assertOperations(Collections.singletonList(legalEntityClient));

        // TO-DO: assertNotifications miss func in TestUtils
//        assertNotifications(Collections.singletonList(issuerClient));
//        assertNotifications(Collections.singletonList(holderClient));
//        assertNotifications(Collections.singletonList(verifierClient));
//        assertNotifications(Collections.singletonList(legalEntityClient));
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
                waitOperation(issuerClient, regResult.op());
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
            waitOperation(issuerClient, issResult.getOp());
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
                // TO-DO: error grant
                Object op = issuerClient.getIpex().submitGrant(issuerAid.name, result.exn(), result.sigs(), result.atc(), holderAidPrefix);
                waitOperation(issuerClient, op);
            } catch (SodiumException | IOException | InterruptedException | DigestException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        testSteps.step("Holder can get the credential status before or without holding", () -> {
            CredentialState state = CredentialState.builder().build();
            try {
                Thread.sleep(3000);
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
            } catch (SodiumException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Object> castObjectToLinkedHashMap(Object object) {
        return (LinkedHashMap<String, Object>) object;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> castObjectToListMap(Object object) {
        return (List<Map<String, Object>>) object;
    }
}
