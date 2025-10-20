package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.app.credentialing.credentials.*;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.Callable;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class VerifyCredentialTest extends BaseIntegrationTest {
    private ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    private String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    private String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    private String vLEIServerHostUrl = env.vleiServerUrl() + "/oobi";
    private String QVI_SCHEMA_URL = vLEIServerHostUrl + "/" + QVI_SCHEMA_SAID;
    private String LE_SCHEMA_URL = vLEIServerHostUrl + "/" + LE_SCHEMA_SAID;
    TestSteps testSteps = new TestSteps();

    private static SignifyClient issuerClient, verifierClient, holderClient, legalEntityClient;
    private TestUtils.Aid issuerAid, holderAid, legalEntityAid;
    
    // Global variables to store QVI credential components
    private static Map<String, Object> vcpEvent;
    private static String vcpAttachment;
    private static Map<String, Object> issEvent;
    private static String issAttachment;
    private static Map<String, Object> acdcEvent;
    private static String qviCredentialId;
    
    // Global variables to store LE (chained) credential components
    private static Map<String, Object> leVcpEvent;
    private static String leVcpAttachment;
    private static Map<String, Object> leIssEvent;
    private static String leIssAttachment;
    private static Map<String, Object> leAcdcEvent;
    private static String leCredentialId;
    private static String leCredentialCesr;

    @BeforeAll
    public static void getClients() throws Exception {
        List<SignifyClient> clients = getOrCreateClientsAsync(4);
        issuerClient = clients.get(0);
        verifierClient = clients.get(1);
        holderClient = clients.get(2);
        legalEntityClient = clients.get(3);
    }

    @BeforeEach
    public void getAid() throws Exception {
        List<TestUtils.Aid> aids = createAidAsync(
                new CreateAidArgs(issuerClient, "issuer"),
                new CreateAidArgs(verifierClient, "verifier"),
                new CreateAidArgs(holderClient, "holder"),
                new CreateAidArgs(legalEntityClient, "legal-entity")
        );
        issuerAid = aids.get(0);
        holderAid = aids.get(2);
        legalEntityAid = aids.get(3);
    }

    @BeforeEach
    public void getContact() {
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(issuerClient, "holder", holderAid.oobi),
                new GetOrCreateContactArgs(verifierClient, "issuer", issuerAid.oobi),
                new GetOrCreateContactArgs(verifierClient, "holder", holderAid.oobi),
                new GetOrCreateContactArgs(holderClient, "issuer", issuerAid.oobi),
                new GetOrCreateContactArgs(holderClient, "legal-entity", legalEntityAid.oobi),
                new GetOrCreateContactArgs(legalEntityClient, "holder", holderAid.oobi),
                new GetOrCreateContactArgs(legalEntityClient, "issuer", issuerAid.oobi)
        );
        System.out.println("Created contact successfully");
    }

    @AfterAll
    public static void cleanup() throws Exception {
        List<SignifyClient> clients = Arrays.asList(
                issuerClient,
                verifierClient,
                holderClient,
                legalEntityClient
        );
        assertOperations(clients);
        assertNotifications(clients);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verify_credential_workflow() throws Exception {
        testSteps.step("Resolve schema oobis", () -> {
            resolveOobisAsync(
                    new ResolveOobisArgs(issuerClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(issuerClient, LE_SCHEMA_URL, null),
                    new ResolveOobisArgs(verifierClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(verifierClient, LE_SCHEMA_URL, null),
                    new ResolveOobisArgs(verifierClient, issuerAid.oobi, null),
                    new ResolveOobisArgs(holderClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(holderClient, LE_SCHEMA_URL, null),
                    new ResolveOobisArgs(legalEntityClient, LE_SCHEMA_URL, null)
            );
        });

        HashMap<String, Object> registry = testSteps.step("Create registry", () -> {
            String registryName = "vLEI-test-registry";
            HashMap<String, Object> registryData = new HashMap<>();

            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(issuerAid.name);
            registryArgs.setRegistryName(registryName);
            
            RegistryResult regResult = issuerClient.registries().create(registryArgs);
            waitOperation(issuerClient, regResult.op());
            
            Object registries = issuerClient.registries().list(issuerAid.name);
            List<Map<String, Object>> registriesList = castObjectToListMap(registries);
            
            registryData.put("name", registriesList.getFirst().get("name").toString());
            registryData.put("regk", registriesList.getFirst().get("regk").toString());
            
            assertEquals(1, registriesList.size());
            assertEquals(registryName, registryData.get("name"));
            
            return registryData;
        });

        qviCredentialId = testSteps.step("Issue QVI credential and extract components", () -> {
            Map<String, Object> vcdata = new HashMap<>();
            vcdata.put("LEI", "5493001KJTIIGC8Y1R17");

            CredentialData.CredentialSubject a = CredentialData.CredentialSubject.builder().build();
            a.setI(holderAid.prefix); // Credential subject is holder
            a.setAdditionalProperties(vcdata);

            CredentialData cData = CredentialData.builder().build();
            cData.setRi(registry.get("regk").toString());
            cData.setS(QVI_SCHEMA_SAID);
            cData.setA(a);

            IssueCredentialResult issResult = issuerClient.credentials().issue(issuerAid.name, cData);
            waitOperation(issuerClient, issResult.getOp());
            String credId = issResult.getAcdc().getKed().get("d").toString();
            
            // Get the credential with CESR format to extract components
            Optional<Object> credentialOpt = issuerClient.credentials().get(credId, true);
            String credentialCesr = (String) credentialOpt.get();
            
            // Parse CESR data to extract VCP, ISS, and ACDC events
            List<Map<String, Object>> cesrData = parseCESRData(credentialCesr);
            
            for (Map<String, Object> eventData : cesrData) {
                Map<String, Object> event = (Map<String, Object>) eventData.get("event");
                
                // Check for event type
                Object eventTypeObj = event.get("t");
                if (eventTypeObj != null) {
                    String eventType = eventTypeObj.toString();
                    switch (eventType) {
                        case "vcp":
                            vcpEvent = event;
                            vcpAttachment = (String) eventData.get("atc");
                            break;
                        case "iss":
                            issEvent = event;
                            issAttachment = (String) eventData.get("atc");
                            break;
                    }
                } else {
                    // Check if this is an ACDC (credential data) without "t" field
                    if (event.containsKey("s") && event.containsKey("a") && event.containsKey("i")) {
                        Object schemaObj = event.get("s");
                        if (schemaObj != null && QVI_SCHEMA_SAID.equals(schemaObj.toString())) {
                            acdcEvent = event;
                        }
                    }
                }
            }
            
            // Verify all components were extracted
            assertNotNull(vcpEvent, "VCP event should be extracted");
            assertNotNull(vcpAttachment, "VCP attachment should be extracted");
            assertNotNull(issEvent, "ISS event should be extracted");
            assertNotNull(issAttachment, "ISS attachment should be extracted");
            assertNotNull(acdcEvent, "ACDC event should be extracted");
            
            System.out.println("Successfully extracted all credential components");
            return credId;
        });

        Map<String, Object> holderRegistry = testSteps.step("Holder create registry for LE credential", () -> {
            String registryName = "vLEI-test-registry-le";
            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(holderAid.name);
            registryArgs.setRegistryName(registryName);

            RegistryResult regResult = holderClient.registries().create(registryArgs);
            waitOperation(holderClient, regResult.op());
            
            Object registries = holderClient.registries().list(holderAid.name);
            List<Map<String, Object>> registriesList = castObjectToListMap(registries);
            
            assertTrue(!registriesList.isEmpty());
            return registriesList.getFirst();
        });

        leCredentialId = testSteps.step("Holder create LE (chained) credential and extract components", () -> {
            // First, holder must verify the QVI registry using VCP
                System.out.println("\n=== Holder Verifying QVI Registry ===");

                Object op3 = holderClient.keyStates().query(issuerAid.prefix, "1");
                waitOperation(holderClient, op3);

                Serder holderVcpSerder = new Serder(vcpEvent);
                Object holderRegistryVerifyOp = holderClient.registries().verify(holderVcpSerder, vcpAttachment);
                
                Operation<?> holderRegistryOperation = waitOperation(holderClient, holderRegistryVerifyOp);
                assertTrue(holderRegistryOperation.isDone());
                
                // Second, holder must verify the QVI credential using ISS and ACDC
                System.out.println("\n=== Holder Verifying QVI Credential ===");

                Object op4 = holderClient.keyStates().query(issuerAid.prefix, "1");
                waitOperation(holderClient, op4);

                Serder holderAcdcSerder = new Serder(acdcEvent);
                Serder holderIssSerder = new Serder(issEvent);

                Object holderCredentialVerifyOp = holderClient.credentials().verify(holderAcdcSerder, holderIssSerder, null, issAttachment);

                Operation<?> holderCredentialOperation = waitOperation(holderClient, holderCredentialVerifyOp);
                assertTrue(holderCredentialOperation.isDone());
                
                System.out.println("✓ Holder verification steps completed, now retrieving QVI credential");
                
                // Get the QVI credential from holder
                Object qviCredential = holderClient.credentials().get(qviCredentialId).get();
                LinkedHashMap<String, Object> qviCredentialBody = castObjectToLinkedHashMap(qviCredential);
                LinkedHashMap<String, Object> sadBody = castObjectToLinkedHashMap(qviCredentialBody.get("sad"));

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

                IssueCredentialResult result = holderClient.credentials().issue(holderAid.name, cData);
                waitOperation(holderClient, result.getOp());
                String leCredId = result.getAcdc().getKed().get("d").toString();
                
                System.out.println("LE Credential Issued Successfully!");
                
                // Get the LE credential with CESR format to extract components
                Optional<Object> leCredentialOpt = holderClient.credentials().get(leCredId, true);
                leCredentialCesr = (String) leCredentialOpt.get();
                
                // Parse CESR data to extract VCP, ISS, and ACDC events for LE credential
                List<Map<String, Object>> leCesrData = parseCESRData(leCredentialCesr);
                
                // Collect all VCP, ISS, and ACDC events for chained credential verification
                List<Map<String, Object>> allVcpEvents = new ArrayList<>();
                List<String> allVcpAttachments = new ArrayList<>();
                List<Map<String, Object>> allIssEvents = new ArrayList<>();
                List<String> allIssAttachments = new ArrayList<>();
                List<Map<String, Object>> allAcdcEvents = new ArrayList<>();
                
                for (Map<String, Object> eventData : leCesrData) {
                    Map<String, Object> event = (Map<String, Object>) eventData.get("event");
                    
                    // Check for event type
                    Object eventTypeObj = event.get("t");
                    if (eventTypeObj != null) {
                        String eventType = eventTypeObj.toString();
                        switch (eventType) {
                            case "vcp":
                                allVcpEvents.add(event);
                                allVcpAttachments.add((String) eventData.get("atc"));
                                break;
                            case "iss":
                                allIssEvents.add(event);
                                allIssAttachments.add((String) eventData.get("atc"));
                                break;
                        }
                    } else {
                        // Check if this is an ACDC (credential data) without "t" field
                        if (event.containsKey("s") && event.containsKey("a") && event.containsKey("i")) {
                            Object schemaObj = event.get("s");
                            if (schemaObj != null) {
                                allAcdcEvents.add(event);
                            }
                        }
                    }
                }
                
                // Set the LE-specific events (last ones in the chain)
                if (!allVcpEvents.isEmpty()) {
                    leVcpEvent = allVcpEvents.get(allVcpEvents.size() - 1);
                    leVcpAttachment = allVcpAttachments.get(allVcpAttachments.size() - 1);
                }
                if (!allIssEvents.isEmpty()) {
                    leIssEvent = allIssEvents.get(allIssEvents.size() - 1);
                    leIssAttachment = allIssAttachments.get(allIssAttachments.size() - 1);
                }
                if (!allAcdcEvents.isEmpty()) {
                    // Find the LE ACDC event specifically
                    for (Map<String, Object> acdcEvent : allAcdcEvents) {
                        Object schemaObj = acdcEvent.get("s");
                        if (schemaObj != null && LE_SCHEMA_SAID.equals(schemaObj.toString())) {
                            leAcdcEvent = acdcEvent;
                            break;
                        }
                    }
                }
                
                // Verify all LE components were extracted
                assertNotNull(leVcpEvent, "LE VCP event should be extracted");
                assertNotNull(leVcpAttachment, "LE VCP attachment should be extracted");
                assertNotNull(leIssEvent, "LE ISS event should be extracted");
                assertNotNull(leIssAttachment, "LE ISS attachment should be extracted");
                assertNotNull(leAcdcEvent, "LE ACDC event should be extracted");
                
                System.out.println("Successfully extracted all LE credential components");
                return leCredId;
        });

        testSteps.steps("Verifier verify all registries using all VCP events", (Callable<Void>) () -> {
            // Query all relevant key states
            Object op4 = verifierClient.keyStates().query(holderAid.prefix, "1");
            waitOperation(verifierClient, op4);
            Object op5 = verifierClient.keyStates().query(issuerAid.prefix, "1");
            waitOperation(verifierClient, op5);
            
            System.out.println("\n=== Verifying All VCP Events in Chain ===");
            
            List<Map<String, Object>> leCesrData = parseCESRData(leCredentialCesr);
            
            List<Map<String, Object>> allVcpEvents = new ArrayList<>();
            List<String> allVcpAttachments = new ArrayList<>();
            
            for (Map<String, Object> eventData : leCesrData) {
                Map<String, Object> event = (Map<String, Object>) eventData.get("event");
                Object eventTypeObj = event.get("t");
                if (eventTypeObj != null && "vcp".equals(eventTypeObj.toString())) {
                    allVcpEvents.add(event);
                    allVcpAttachments.add((String) eventData.get("atc"));
                }
            }
            
            // Verify each VCP event (registry) in the chain
            for (int i = 0; i < allVcpEvents.size(); i++) {
                Map<String, Object> vcpEvent = allVcpEvents.get(i);
                String vcpAttachment = allVcpAttachments.get(i);
                Serder vcpSerder = new Serder(vcpEvent);
                Object registryVerifyOp = verifierClient.registries().verify(vcpSerder, vcpAttachment);
                
                Operation<?> registryOperation = waitOperation(verifierClient, registryVerifyOp);
                assertTrue(registryOperation.isDone());
                System.out.println("✓ VCP #" + (i + 1) + " verification completed successfully");
            }
            
            System.out.println("Completed verification of " + allVcpEvents.size() + " VCP events in the chain");
            return null;
        });

        testSteps.steps("Verifier verify all credentials using all ISS and ACDC events", (Callable<Void>) () -> {
            // Query all relevant key states
            Object op6 = verifierClient.keyStates().query(holderAid.prefix, "1");
            waitOperation(verifierClient, op6);
            Object op7 = verifierClient.keyStates().query(issuerAid.prefix, "1");
            waitOperation(verifierClient, op7);

            System.out.println("\n=== Verifying All ISS and ACDC Events in Chain ===");
                
                // Parse the existing CESR data to extract ISS and ACDC events
                List<Map<String, Object>> leCesrData = parseCESRData(leCredentialCesr);
                
                List<Map<String, Object>> allIssEvents = new ArrayList<>();
                List<String> allIssAttachments = new ArrayList<>();
                List<Map<String, Object>> allAcdcEvents = new ArrayList<>();
                
                // Collect all ISS and ACDC events from the parsed CESR data
                for (Map<String, Object> eventData : leCesrData) {
                    Map<String, Object> event = (Map<String, Object>) eventData.get("event");
                    Object eventTypeObj = event.get("t");
                    
                    if (eventTypeObj != null && "iss".equals(eventTypeObj.toString())) {
                        allIssEvents.add(event);
                        allIssAttachments.add((String) eventData.get("atc"));
                    } else if (eventTypeObj == null && event.containsKey("s") && event.containsKey("a") && event.containsKey("i")) {
                        // This is an ACDC event
                        allAcdcEvents.add(event);
                    }
                }
                
                // Verify each credential in the chain (ISS + ACDC pairs)
                for (int i = 0; i < Math.min(allIssEvents.size(), allAcdcEvents.size()); i++) {
                    Map<String, Object> issEvent = allIssEvents.get(i);
                    Map<String, Object> acdcEvent = allAcdcEvents.get(i);
                    String issAttachment = allIssAttachments.get(i);
                    Serder acdcSerder = new Serder(acdcEvent);
                    Serder issSerder = new Serder(issEvent);
                    
                    Object credentialVerifyOp = verifierClient.credentials().verify(acdcSerder, issSerder, null, issAttachment);              
                    Operation<?> credentialOperation = waitOperation(verifierClient, credentialVerifyOp);
                    assertTrue(credentialOperation.isDone());
                }
                
                // Verify the credential is available from verifier
                Optional<Object> verifiedLeCredential = verifierClient.credentials().get(leCredentialId, false);
                assertTrue(verifiedLeCredential.isPresent(), "Verified LE credential should be retrievable");
                System.out.println("✓ All credentials in the chain verified successfully");
                
                // Check for chain information
                assertTrue(leCredentialCesr.contains(qviCredentialId));
            return null;
        });
    }

    static class StringData {
        static final String USAGE_DISCLAIMER = "Usage of a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, does not assert that the Legal Entity is trustworthy, honest, reputable in its business dealings, safe to do business with, or compliant with any laws or that an implied or expressly intended purpose will be fulfilled.";
        static final String ISSUANCE_DISCLAIMER = "All information in a valid, unexpired, and non-revoked vLEI Credential, as defined in the associated Ecosystem Governance Framework, is accurate as of the date the validation process was complete. The vLEI Credential has been issued to the legal entity or person named in the vLEI Credential as the subject; and the qualified vLEI Issuer exercised reasonable care to perform the validation process set forth in the vLEI Ecosystem Governance Framework.";
    }

    /**
     * Parses CESR format string into an array of events with their attachments
     * CESR format: {json_event}{attachment}{json_event}{attachment}...
     * 
     * @param cesrData The CESR format string
     * @return List of maps containing "event" and "atc" keys
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseCESRData(String cesrData) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        int index = 0;
        while (index < cesrData.length()) {
            // Find the start of JSON event (look for opening brace)
            if (cesrData.charAt(index) == '{') {
                // Find the end of JSON event by counting braces
                int braceCount = 0;
                int jsonStart = index;
                int jsonEnd = index;
                
                for (int i = index; i < cesrData.length(); i++) {
                    char ch = cesrData.charAt(i);
                    if (ch == '{') {
                        braceCount++;
                    } else if (ch == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            jsonEnd = i + 1;
                            break;
                        }
                    }
                }
                
                // Extract JSON event
                String jsonEvent = cesrData.substring(jsonStart, jsonEnd);
                
                // Find attachment data (everything until next '{' or end of string)
                int attachmentStart = jsonEnd;
                int attachmentEnd = cesrData.length();
                
                for (int i = attachmentStart; i < cesrData.length(); i++) {
                    if (cesrData.charAt(i) == '{') {
                        attachmentEnd = i;
                        break;
                    }
                }
                
                String attachment = "";
                if (attachmentStart < attachmentEnd) {
                    attachment = cesrData.substring(attachmentStart, attachmentEnd);
                }
                
                // Parse JSON event to Object
                try {
                    Map<String, Object> eventObj = Utils.fromJson(jsonEvent, Map.class);
                    
                    Map<String, Object> eventMap = new LinkedHashMap<>();
                    eventMap.put("event", eventObj);
                    eventMap.put("atc", attachment);
                    result.add(eventMap);
                } catch (Exception e) {
                    System.err.println("Failed to parse JSON event: " + jsonEvent);
                    e.printStackTrace();
                }
                
                index = attachmentEnd;
            } else {
                index++;
            }
        }
        
        return result;
    }
}