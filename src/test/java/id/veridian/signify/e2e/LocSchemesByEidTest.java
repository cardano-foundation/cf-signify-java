package id.veridian.signify.e2e;

import id.veridian.signify.app.aiding.CreateIdentifierArgs;
import id.veridian.signify.app.aiding.LocSchemeArgs;
import id.veridian.signify.app.clienting.SignifyClient;
import id.veridian.signify.e2e.utils.ResolveEnv;
import id.veridian.signify.e2e.utils.TestSteps;
import id.veridian.signify.e2e.utils.TestUtils;
import id.veridian.signify.generated.keria.model.EndRole;
import id.veridian.signify.generated.keria.model.HabState;
import id.veridian.signify.generated.keria.model.LocSchemeMetadata;
import id.veridian.signify.generated.keria.model.OOBI;
import org.junit.jupiter.api.Test;

import java.util.List;

import static id.veridian.signify.e2e.MultisigJoinTest.getOobisIndexAt0;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocSchemesByEidTest extends BaseIntegrationTest {

    private static final String MAILBOX_URL = "http://mymailbox.example.com";
    private static final String MAILBOX_SCHEME = "http";

    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    TestSteps testSteps = new TestSteps();

    @Test
    public void testLocSchemesByEid() {
        List<SignifyClient> clients = getOrCreateClientsAsync(2);
        SignifyClient user1 = clients.get(0);
        SignifyClient resolver = clients.get(1);

        String mailboxNodeName = "mailbox-node";
        String resolverName = "resolver";

        List<String> wits = env.witnessIds();
        CreateIdentifierArgs createArgs = CreateIdentifierArgs.builder()
                .wits(wits)
                .toad(wits.size())
                .build();

        String mailboxAid = testSteps.step("User1 creates AID with mailbox end role and location scheme", () -> {
            List<HabState> habs = getOrCreateAIDAsync(new CreateAidArgs(user1, mailboxNodeName, createArgs));
            String aid = habs.getFirst().getPrefix();
            String stamp = TestUtils.createTimestamp();

            var agentResult = user1.identifiers()
                    .addEndRole(mailboxNodeName, "agent", user1.getAgent().getPre(), stamp);
            TestUtils.waitForCompleted(user1, agentResult.op());

            var endRoleResult = user1.identifiers().addEndRole(mailboxNodeName, "mailbox", aid, stamp);
            TestUtils.waitForCompleted(user1, endRoleResult.op());

            var locSchemeResult = user1.identifiers().addLocScheme(mailboxNodeName, LocSchemeArgs.builder()
                    .url(MAILBOX_URL)
                    .scheme(MAILBOX_SCHEME)
                    .build());
            TestUtils.waitForCompleted(user1, locSchemeResult.op());

            return aid;
        });

        String agentOobi = testSteps.step("Resolver creates AID and resolves user1's agent OOBI", () -> {
            getOrCreateAIDAsync(new CreateAidArgs(resolver, resolverName, createArgs));

            OOBI oobi = user1.oobis().get(mailboxNodeName, "agent").orElseThrow();
            String agent = getOobisIndexAt0(oobi);
            TestUtils.resolveOobi(resolver, agent, mailboxNodeName);
            return agent;
        });

        testSteps.step("Resolver resolves the mailbox end role OOBI", () -> {
            String oobiBase = agentOobi.split("/oobi/")[0];
            String mailboxOobi = oobiBase + "/oobi/" + mailboxAid + "/mailbox/" + mailboxAid;
            TestUtils.waitForCompleted(resolver, resolver.oobis().resolve(mailboxOobi, null));
        });

        testSteps.step("Resolver fetches location schemes by mailbox EID", () -> {
            List<EndRole> roles = resolver.oobis().endroles(mailboxAid, "mailbox");
            EndRole mailboxRole = roles.stream()
                    .filter(role -> "mailbox".equals(role.getRole()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("no mailbox end role for " + mailboxAid));

            List<LocSchemeMetadata> locSchemes = resolver.oobis().locschemes(mailboxRole.getEid());
            assertTrue(
                    locSchemes.stream().anyMatch(scheme ->
                            MAILBOX_SCHEME.equals(scheme.getScheme()) && MAILBOX_URL.equals(scheme.getUrl())),
                    "expected " + MAILBOX_SCHEME + " " + MAILBOX_URL + " in " + locSchemes);
        });
    }
}
