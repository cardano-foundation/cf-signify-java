package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialVerifyOptions;
import org.cardanofoundation.signify.app.credentialing.credentials.IssueCredentialResult;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryVerifyOptions;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.CompletedOperation;
import org.cardanofoundation.signify.generated.keria.model.Credential;
import org.cardanofoundation.signify.generated.keria.model.Operation;
import org.cardanofoundation.signify.generated.keria.model.QueryOperation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class VerifyCredentialTest extends BaseIntegrationTest {
    private final ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    private final String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    private final String QVI_SCHEMA_URL = env.vleiServerUrl() + "/oobi/" + QVI_SCHEMA_SAID;
    private final TestSteps testSteps = new TestSteps();

    private static SignifyClient issuerClient, verifierClient;
    private TestUtils.Aid issuerAid, verifierAid;

    @BeforeAll
    public static void getClients() {
        List<SignifyClient> clients = getOrCreateClientsAsync(2);
        issuerClient = clients.get(0);
        verifierClient = clients.get(1);
    }

    @BeforeEach
    public void getAid() {
        List<TestUtils.Aid> aids = createAidAsync(
                new CreateAidArgs(issuerClient, "issuer"),
                new CreateAidArgs(verifierClient, "verifier"));
        issuerAid = aids.get(0);
        verifierAid = aids.get(1);
    }

    @BeforeEach
    public void getContact() {
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(verifierClient, "issuer", issuerAid.oobi));
    }

    @AfterAll
    public static void cleanup() {
        List<SignifyClient> clients = Arrays.asList(issuerClient, verifierClient);
        assertOperations(clients);
        assertNotifications(clients);
    }

    @Test
    public void verify_credential_workflow() {
        testSteps.step("Resolve schema oobis", () -> {
            resolveOobisAsync(
                    new ResolveOobisArgs(issuerClient, QVI_SCHEMA_URL, null),
                    new ResolveOobisArgs(verifierClient, QVI_SCHEMA_URL, null));
        });

        RegistryResult regResult = testSteps.step("Issuer create registry", () -> {
            CreateRegistryArgs registryArgs = CreateRegistryArgs.builder().build();
            registryArgs.setName(issuerAid.name);
            registryArgs.setRegistryName("vLEI-verify-registry");

            RegistryResult result = issuerClient.registries().create(registryArgs);
            waitForCompleted(issuerClient, result.op());
            return result;
        });

        IssueCredentialResult issResult = testSteps.step("Issuer issue credential", () -> {
            Map<String, Object> vcdata = new HashMap<>();
            vcdata.put("LEI", "5493001KJTIIGC8Y1R17");

            CredentialData.CredentialSubject a = CredentialData.CredentialSubject.builder().build();
            a.setI(verifierAid.prefix);
            a.setAdditionalProperties(vcdata);

            CredentialData cData = CredentialData.builder().build();
            cData.setRi(regResult.regser().getPre());
            cData.setS(QVI_SCHEMA_SAID);
            cData.setA(a);

            IssueCredentialResult result = issuerClient.credentials().issue(issuerAid.name, cData);
            waitForCompleted(issuerClient, result.getOp());
            return result;
        });

        String credentialId = issResult.getAcdc().getKed().get("d").toString();

        testSteps.step("Verifier verifies registry (vcp)", () -> {
            QueryOperation ksq = verifierClient.keyStates().query(issuerAid.prefix, "1");
            waitForCompleted(verifierClient, ksq);

            String vcpAtc = new String(Utils.serializeIssExnAttachment(regResult.serder()));
            Operation op = verifierClient.registries()
                    .verify(new RegistryVerifyOptions(regResult.regser(), vcpAtc));

            Operation completed = waitOperation(verifierClient, op);
            assertTrue(completed instanceof CompletedOperation, "registry verify op should complete");
        });

        testSteps.step("Verifier verifies credential (iss + acdc)", () -> {
            QueryOperation ksq = verifierClient.keyStates().query(issuerAid.prefix, "2");
            waitForCompleted(verifierClient, ksq);

            String issAtc = new String(Utils.serializeIssExnAttachment(issResult.getAnc()));
            Operation op = verifierClient.credentials().verify(
                    new CredentialVerifyOptions(issResult.getAcdc(), issResult.getIss(), issAtc, null));

            Operation completed = waitOperation(verifierClient, op);
            assertTrue(completed instanceof CompletedOperation, "credential verify op should complete");
        });

        testSteps.step("Verifier can retrieve the verified credential", () -> {
            Optional<Credential> cred = verifierClient.credentials().get(credentialId);
            assertTrue(cred.isPresent(), "verified credential should be retrievable");
            assertEquals(QVI_SCHEMA_SAID, cred.get().getSad().getS());
        });
    }
}
