package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.LocSchemeArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestSteps;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.EndRole;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.LocSchemeMetadata;
import org.cardanofoundation.signify.generated.keria.model.OOBI;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.cardanofoundation.signify.e2e.MultisigJoinTest.getOobisIndexAt0;
import static org.junit.jupiter.api.Assertions.*;

public class LocSchemesByEidTest extends BaseIntegrationTest {

    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    TestSteps testSteps = new TestSteps();

    @Test
    public void testLocSchemesByIndexerEid() throws Exception {
        List<SignifyClient> clients = getOrCreateClientsAsync(2);
        SignifyClient user1 = clients.get(0);
        SignifyClient resolver = clients.get(1);

        String indexerNodeName = "indexer-node";
        String resolverName = "resolver";

        List<String> wits = env.witnessIds();
        CreateIdentifierArgs createArgs = CreateIdentifierArgs.builder()
                .wits(wits)
                .toad(wits.size())
                .build();

        testSteps.step("User1 creates AID with indexer end role and location scheme", () -> {
            List<HabState> habs = getOrCreateAIDAsync(new CreateAidArgs(user1, indexerNodeName, createArgs));
            HabState hab = habs.getFirst();
            String indexerAid = hab.getPrefix();
            String stamp = TestUtils.createTimestamp();

            var agentResult = user1.identifiers().addEndRole(indexerNodeName, "agent", user1.getAgent().getPre(), stamp);
            TestUtils.waitForCompleted(user1, agentResult.op());

            var endRoleResult = user1.identifiers().addEndRole(indexerNodeName, "indexer", indexerAid, stamp);
            TestUtils.waitForCompleted(user1, endRoleResult.op());

            LocSchemeArgs locSchemeArgs = LocSchemeArgs.builder()
                    .url("http://indexer.example.com")
                    .scheme("http")
                    .eid(indexerAid)
                    .build();
            var locSchemeResult = user1.identifiers().addLocScheme(indexerNodeName, locSchemeArgs);
            TestUtils.waitForCompleted(user1, locSchemeResult.op());
        });

        testSteps.step("Resolver creates AID and resolves user1's agent OOBI", () -> {
            getOrCreateAIDAsync(new CreateAidArgs(resolver, resolverName, createArgs));

            OOBI oobi = user1.oobis().get(indexerNodeName, "agent").get();
            String agentOobi = getOobisIndexAt0(oobi);
            TestUtils.resolveOobi(resolver, agentOobi, indexerNodeName);
        });

        testSteps.step("Resolver resolves indexer end role OOBI", () -> {
            HabState indexerHab = user1.identifiers().get(indexerNodeName).get();
            String indexerAid = indexerHab.getPrefix();

            OOBI oobi = user1.oobis().get(indexerNodeName, "agent").get();
            String oobiBase = getOobisIndexAt0(oobi).split("/oobi/")[0];
            String indexerOobi = oobiBase + "/oobi/" + indexerAid + "/indexer/" + indexerAid;

            var resolveResult = resolver.oobis().resolve(indexerOobi, null);
            TestUtils.waitForCompleted(resolver, resolveResult);
        });

        testSteps.step("Resolver fetches location schemes by indexer EID", () -> {
            HabState indexerHab = user1.identifiers().get(indexerNodeName).get();
            String indexerAid = indexerHab.getPrefix();

            List<EndRole> roles = resolver.oobis().endroles(indexerAid, "indexer");
            assertEquals(1, roles.size(), "Should have one indexer role");

            EndRole indexerRole = roles.get(0);
            assertEquals("indexer", indexerRole.getRole());

            String indexerEid = indexerRole.getEid();
            List<LocSchemeMetadata> locSchemes = resolver.oobis().locschemes(indexerEid);
            assertEquals(1, locSchemes.size(), "Should have one location scheme");

            LocSchemeMetadata scheme = locSchemes.get(0);
            assertEquals("http", scheme.getScheme());
            assertEquals("http://indexer.example.com", scheme.getUrl());
        });
    }
}
