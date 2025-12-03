package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.*;
import org.cardanofoundation.signify.app.aiding.IdentifierController;
import org.cardanofoundation.signify.app.clienting.exception.HeaderVerificationException;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.coring.KeyStates;
import org.cardanofoundation.signify.app.coring.Oobis;
import org.cardanofoundation.signify.app.coring.Operations;
import org.cardanofoundation.signify.app.credentialing.Schemas;
import org.cardanofoundation.signify.app.credentialing.credentials.Credentials;
import org.cardanofoundation.signify.app.credentialing.ipex.Ipex;
import org.cardanofoundation.signify.app.credentialing.registries.Registries;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClientingTest extends BaseMockServerTest {

    @Test
    @DisplayName("SignifyClient initialization")
    void testSignifyClientInitialization() throws Exception {
        InvalidValueException exception = assertThrows(
                InvalidValueException.class,
                () -> new SignifyClient(url, "short", Tier.low, bootUrl, null)
        );
        assertEquals("bran must be 21 characters", exception.getMessage());

        SignifyClient client = new SignifyClient(
                url,
                bran,
                Tier.low,
                bootUrl,
                null
        );

        assertEquals(bran, client.getBran());
        assertEquals(url, client.getUrl());
        assertEquals(bootUrl, client.getBootUrl());
        assertEquals(Tier.low, client.getTier());
        assertEquals(0, client.getPidx());
        assertEquals(
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
                client.getController().getPre()
        );
        assertEquals("signify:controller", client.getController().getStem());
        assertEquals(Tier.low, client.getController().getTier());

        String expectedSerderRaw = """
                {"v":"KERI10JSON00012b_","t":"icp",\
                "d":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",\
                "i":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose","s":"0",\
                "kt":"1","k":["DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"],\
                "nt":"1","n":["EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"],\
                "bt":"0","b":[],"c":[],"a":[]}""";
        assertEquals(expectedSerderRaw, client.getController().getSerder().getRaw());
        assertEquals("0", client.getController().getSerder().getKed().get("s"));

        client.boot();

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/boot", request.getPath());
        assertEquals("application/json", request.getHeader("Content-Type"));

        String expectedRequestBody = """
                {"icp":{"v":"KERI10JSON00012b_","t":"icp",\
                "d":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",\
                "i":"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose","s":"0",\
                "kt":"1","k":["DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"],\
                "nt":"1","n":["EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"],\
                "bt":"0","b":[],"c":[],"a":[]},\
                "sig":"AACJwsJ0mvb4VgxD87H4jIsiT1QtlzznUy9zrX3lGdd48jjQRTv8FxlJ8ClDsGtkvK4Eekg5p-oPYiPvK_1eTXEG",\
                "stem":"signify:controller","pidx":1,"tier":"low"}""";

        assertEquals(
                objectMapper.readTree(expectedRequestBody),
                objectMapper.readTree(request.getBody().readUtf8())
        );
        cleanUpRequest();

        client.connect();

        // Verify the state HTTP request
        RecordedRequest stateRequest = mockWebServer.takeRequest();
        assertEquals("GET", stateRequest.getMethod());
        assertTrue(stateRequest.getPath().startsWith("/agent"));
//        assertEquals("application/json", stateRequest.getHeader("Content-Type"));

        // Validate agent
        assertEquals(
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                client.getAgent().getPre()
        );
        assertEquals(
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
                client.getAgent().getAnchor()
        );
        assertEquals(
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                client.getAgent().getSaid()
        );
        assertEquals("0", client.getAgent().getState().get("s"));
        assertEquals(
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                client.getAgent().getState().get("d")
        );

        // Validate approve delegation
        assertEquals("1", client.getController().getSerder().getKed().get("s"));
        assertEquals("ixn", client.getController().getSerder().getKed().get("t"));

        List<Object> actions = (List<Object>) client.getController().getSerder().getKed().get("a");
        Map<String, Object> actionMap = Utils.toMap(actions.getFirst());
        assertEquals(
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                actionMap.get("i")
        );
        assertEquals(
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                actionMap.get("d")
        );
        assertEquals("0", actionMap.get("s"));

        // Validate data
        Object[] data = client.getData();
        assertEquals(url, data[0]);
        assertEquals(bran, data[1]);

        // Validate service instances
        assertInstanceOf(IdentifierController.class, client.identifiers());
        assertInstanceOf(Operations.class, client.operations());
        assertInstanceOf(Coring.KeyEvents.class, client.keyEvents());
        assertInstanceOf(KeyStates.class, client.keyStates());
        assertInstanceOf(Credentials.class, client.credentials());
        assertInstanceOf(Registries.class, client.registries());
        assertInstanceOf(Schemas.class, client.schemas());
        assertInstanceOf(Ipex.class, client.ipex());
        assertInstanceOf(Contacting.Challenges.class, client.challenges());
        assertInstanceOf(Contacting.Contacts.class, client.contacts());
        assertInstanceOf(Notifying.Notifications.class, client.notifications());
        assertInstanceOf(Escrowing.Escrows.class, client.escrows());
        assertInstanceOf(Oobis.class, client.oobis());
        assertInstanceOf(Exchanging.Exchanges.class, client.exchanges());
        assertInstanceOf(Grouping.Groups.class, client.groups());
        cleanUpRequest();

    }

    @Test
    @DisplayName("Signed Fetch")
    void testSignedFetch() throws Exception {
        // Siged fetch
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.low, bootUrl, null);

        client.connect();
        cleanUpRequest();

        client.fetch("/contacts", "GET", null);
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/contacts", request.getPath());
        assertEquals("ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose", request.getHeaders().get("signify-resource"));
        cleanUpRequest();

        //test bad request
        Exception exception = assertThrows(
                HeaderVerificationException.class,
                () -> client.fetch("/different-remote-agent", "GET", null)
        );

        assertEquals("Message from a different remote agent", exception.getMessage());

        exception = assertThrows(
                IllegalArgumentException.class,
                () -> client.fetch("/invalid-signature", "GET", null)
        );

        assertEquals("Signature for EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei invalid.", exception.getMessage());
    }

    @Test
    public void testJsonObject() throws Exception {
        final ObjectMapper obj = new ObjectMapper();
        final Map<String, Object> ICP_EVENT_OBJ = new LinkedHashMap<>() {{
            put("v", "KERI10JSON00012b_");
            put("t", "icp");
            put("d", "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose");
            put("i", "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose");
            put("s", "0");
            put("kt", "1");
            put("k", List.of("DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc"));
            put("nt", "1");
            put("n", List.of("EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL"));
            put("bt", "0");
            put("b", List.of());
            put("c", List.of());
            put("a", List.of());
        }};

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("icp", ICP_EVENT_OBJ);
        data.put("sig", "AACJwsJ0mvb4VgxD87H4jIsiT1QtlzznUy9zrX3lGdd48jjQRTv8FxlJ8ClDsGtkvK4Eekg5p-oPYiPvK_1eTXEG");
        data.put("stem", "signify:controller");
        data.put("pidx", 1);
        data.put("tier", Salter.Tier.low);

        String expectedData = "{\"icp\":{\"v\":\"KERI10JSON00012b_\",\"t\":\"icp\",\"d\":\"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose\",\"i\":\"ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose\",\"s\":\"0\",\"kt\":\"1\",\"k\":[\"DAbWjobbaLqRB94KiAutAHb_qzPpOHm3LURA_ksxetVc\"],\"nt\":\"1\",\"n\":[\"EIFG_uqfr1yN560LoHYHfvPAhxQ5sN6xZZT_E3h7d2tL\"],\"bt\":\"0\",\"b\":[],\"c\":[],\"a\":[]},\"sig\":\"AACJwsJ0mvb4VgxD87H4jIsiT1QtlzznUy9zrX3lGdd48jjQRTv8FxlJ8ClDsGtkvK4Eekg5p-oPYiPvK_1eTXEG\",\"stem\":\"signify:controller\",\"pidx\":1,\"tier\":\"low\"}";
        assertEquals(obj.writeValueAsString(data), expectedData);
    }
}
