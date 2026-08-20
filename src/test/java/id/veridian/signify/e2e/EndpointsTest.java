package id.veridian.signify.e2e;

import id.veridian.signify.app.aiding.CreateIdentifierArgs;
import id.veridian.signify.app.aiding.LocSchemeArgs;
import id.veridian.signify.app.clienting.SignifyClient;
import id.veridian.signify.e2e.utils.TestUtils;
import id.veridian.signify.generated.keria.model.OOBI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndpointsTest extends BaseIntegrationTest {

    private static final String AID_NAME = "aid1";

    private static SignifyClient client;
    private static String cid;

    @BeforeAll
    static void setUp() {
        client = getOrCreateClientsAsync(1).getFirst();
        var icpResult = client.identifiers().create(AID_NAME, CreateIdentifierArgs.builder().build());
        TestUtils.waitForCompleted(client, icpResult.op());
        cid = icpResult.serder().getPre();
    }

    @Test
    @Order(1)
    public void canAuthoriseAgentRoleForKeria() {
        assertEquals(0, TestUtils.getEndRoles(client, AID_NAME, "agent").size());

        var rpyResult = client.identifiers().addEndRole(AID_NAME, "agent", client.getAgent().getPre(), null);
        TestUtils.waitForCompleted(client, rpyResult.op());

        List<Map<String, Object>> endRoles = TestUtils.getEndRoles(client, AID_NAME, "agent");
        assertEquals(1, endRoles.size());
        assertEquals(Map.of("cid", cid, "role", "agent", "eid", client.getAgent().getPre()), endRoles.getFirst());
    }

    @Test
    @Order(2)
    public void canAuthoriseAnEndpointWeControl() {
        // For sake of demonstrating this test, cid = eid.
        // Other use cases might have a different agent act as cid.
        var endRpyResult = client.identifiers().addEndRole(AID_NAME, "mailbox", cid, null);
        TestUtils.waitForCompleted(client, endRpyResult.op());

        List<Map<String, Object>> endRoles = TestUtils.getEndRoles(client, AID_NAME, "mailbox");
        assertEquals(1, endRoles.size());
        assertEquals(Map.of("cid", cid, "role", "mailbox", "eid", cid), endRoles.getFirst());

        var locRpyResult = client.identifiers().addLocScheme(AID_NAME, LocSchemeArgs.builder()
                .url("https://mymailbox.com")
                .scheme("https")
                .build());
        TestUtils.waitForCompleted(client, locRpyResult.op());

        OOBI oobi = client.oobis().get(AID_NAME, "mailbox").orElseThrow();
        assertEquals("https://mymailbox.com/oobi/" + cid + "/mailbox/" + cid, oobi.getOobis().getFirst());
    }
}
