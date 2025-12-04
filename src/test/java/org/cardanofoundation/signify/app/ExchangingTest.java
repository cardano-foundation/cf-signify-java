package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.cardanofoundation.signify.app.Exchanging.exchange;
import static org.junit.jupiter.api.Assertions.*;

import java.security.DigestException;
import java.util.*;

public class ExchangingTest extends BaseMockServerTest {
    
    @Test
    @DisplayName("should create an exchange message with no transposed attachments")
    public void shouldCreateExchangeMessageWithNoTransposedAttachments() throws DigestException, LibsodiumException {
        String dt = "2023-08-30T17:22:54.183Z";
        
        Exchanging.ExchangeResult result = exchange(
            "/multisig/vcp",
            new HashMap<>(),
            "test",
            "",
            dt,
            null,
            null,
            null);
        Serder exn = result.serder();
        byte[] end = result.end();
        
        Map<String, Object> expectedKed = new LinkedHashMap<>();
        expectedKed.put("a", Map.of("i", ""));
        expectedKed.put("d", "EPWm8LWxxQXmXlB8gbTZKDy7NIwXxpx49N_ZYTa5QkJV");
        expectedKed.put("dt", "2023-08-30T17:22:54.183Z");
        expectedKed.put("e", new HashMap<>());
        expectedKed.put("i", "test");
        expectedKed.put("p", "");
        expectedKed.put("q", new HashMap<>());
        expectedKed.put("r", "/multisig/vcp");
        expectedKed.put("rp", "");
        expectedKed.put("t", "exn");
        expectedKed.put("v", "KERI10JSON0000bf_");

        assertEquals(expectedKed, exn.getKed());
        assertArrayEquals(new byte[0], end);

        int sith = 1;
        int nsith = 1;
        int sn = 0;
        int toad = 0;

        byte[] raw = new byte[]{
            5, (byte)170, (byte)143, 45, 83, (byte)154, (byte)233, (byte)250, 
            85, (byte)156, 2, (byte)156, (byte)155, 8, 72, 117
        };
        
        Salter salter = new Salter(RawArgs.builder().raw(raw).build());
        Signer skp0 = salter.signer(
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            "A",
            Tier.LOW,
            true
        );
        List<String> keys = Collections.singletonList(skp0.getVerfer().getQb64());

        Signer skp1 = salter.signer(
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            "N",
            Tier.LOW,
            true
        );
        
        Diger ndiger = new Diger(RawArgs.builder().build(), skp1.getVerfer().getQb64b());
        List<String> nxt = Collections.singletonList(ndiger.getQb64());
        
        assertEquals("EAKUR-LmLHWMwXTLWQ1QjxHrihBmwwrV2tYaSG7hOrWj", nxt.getFirst());

        Map<String, Object> ked0 = new LinkedHashMap<>();
        ked0.put("v", "KERI10JSON000000_");
        ked0.put("t", Ilks.ICP.getValue());
        ked0.put("d", "");
        ked0.put("i", "");
        ked0.put("s", Integer.toHexString(sn));
        ked0.put("kt", Integer.toHexString(sith));
        ked0.put("k", keys);
        ked0.put("nt", Integer.toHexString(nsith));
        ked0.put("n", nxt);
        ked0.put("bt", Integer.toHexString(toad));
        ked0.put("b", new ArrayList<>());
        ked0.put("c", new ArrayList<>());
        ked0.put("a", new ArrayList<>());

        Serder serder = new Serder(ked0);
        Siger siger = (Siger) skp0.sign(serder.getRaw().getBytes(), 0);

        assertEquals(
            "AAAPkMTS3LrrhVuQB0k4UndDN0xIfEiKYaN7rTlQ_q9ImnBcugwNO8VWTALXzWoaldJEC1IOpEGkEnjZfxxIleoI",
            siger.getQb64()
        );

        Map<String, Object> ked1 = new LinkedHashMap<>();
        ked1.put("v", "KERI10JSON000000_");
        ked1.put("t", Ilks.VCP.getValue());
        ked1.put("d", "");
        ked1.put("i", "");
        ked1.put("s", "0");
        ked1.put("bt", Integer.toHexString(toad));
        ked1.put("b", new ArrayList<>());
        
        Serder vcp = new Serder(ked1);

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("icp", Arrays.asList(serder, siger.getQb64()));
        embeds.put("vcp", Arrays.asList(vcp, null));

        result = exchange("/multisig/vcp", new LinkedHashMap<>(), "test", "", dt, null, null, embeds);
        exn = result.serder();
        end = result.end();

        // Assert the final exchange message structure
        Map<String, Object> expectedFinalKed = new LinkedHashMap<>();
        Map<String, Object> embeddedData = new LinkedHashMap<>();

        // Build embedded ICP data
        Map<String, Object> icpData = new LinkedHashMap<>();
        icpData.put("a", new ArrayList<>());
        icpData.put("b", new ArrayList<>());
        icpData.put("bt", "0");
        icpData.put("c", new ArrayList<>());
        icpData.put("d", "");
        icpData.put("i", "");
        icpData.put("k", Collections.singletonList("DAUDqkmn-hqlQKD8W-FAEa5JUvJC2I9yarEem-AAEg3e"));
        icpData.put("kt", "1");
        icpData.put("n", Collections.singletonList("EAKUR-LmLHWMwXTLWQ1QjxHrihBmwwrV2tYaSG7hOrWj"));
        icpData.put("nt", "1");
        icpData.put("s", "0");
        icpData.put("t", "icp");
        icpData.put("v", "KERI10JSON0000d3_");

        // Build embedded VCP data
        Map<String, Object> vcpData = new LinkedHashMap<>();
        vcpData.put("b", new ArrayList<>());
        vcpData.put("bt", "0");
        vcpData.put("d", "");
        vcpData.put("i", "");
        vcpData.put("s", "0");
        vcpData.put("t", "vcp");
        vcpData.put("v", "KERI10JSON000049_");

        embeddedData.put("d", "EDPWpKtMoPwro_Of8TQzpNMGdtmfyWzqTcRKQ01fGFRi");
        embeddedData.put("icp", icpData);
        embeddedData.put("vcp", vcpData);

        expectedFinalKed.put("v", "KERI10JSON00021b_");
        expectedFinalKed.put("t", "exn");
        expectedFinalKed.put("d", "EOK2xNjB5xlSvizCUrkFKbdF4j1nsGpvt6TR1HL0wvaY");
        expectedFinalKed.put("i", "test");
        expectedFinalKed.put("rp", "");
        expectedFinalKed.put("p", "");
        expectedFinalKed.put("dt", "2023-08-30T17:22:54.183Z");
        expectedFinalKed.put("r", "/multisig/vcp");
        expectedFinalKed.put("q", new LinkedHashMap<>());
        expectedFinalKed.put("a", Map.of("i", ""));
        expectedFinalKed.put("e", embeddedData);

        assertEquals(expectedFinalKed, exn.getKed());
        assertEquals(
            "-LAZ5AACAA-e-icpAAAPkMTS3LrrhVuQB0k4UndDN0xIfEiKYaN7rTlQ_q9ImnBcugwNO8VWTALXzWoaldJEC1IOpEGkEnjZfxxIleoI",
            new String(end)
        );
    }

    @Test
    @DisplayName("Send from events")
    void sendFromEvents() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Exchanging.Exchanges exchange = client.exchanges();
        int sith = 1;
        int nsith = 1;
        int sn = 0;
        int toad = 0;

        byte[] raw = new byte[]{
            5, (byte)170, (byte)143, 45, 83, (byte)154, (byte)233, (byte)250,
            85, (byte)156, 2, (byte)156, (byte)155, 8, 72, 117
        };

        Salter salter = new Salter(RawArgs.builder().raw(raw).build());
        Signer skp0 = salter.signer(
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            "A",
            Tier.LOW,
            true
        );
        List<String> keys = Collections.singletonList(skp0.getVerfer().getQb64());

        Signer skp1 = salter.signer(
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            "N",
            Tier.LOW,
            true
        );

        Diger ndiger = new Diger(RawArgs.builder().build(), skp1.getVerfer().getQb64b());
        List<String> nxt = Collections.singletonList(ndiger.getQb64());

        assertEquals("EAKUR-LmLHWMwXTLWQ1QjxHrihBmwwrV2tYaSG7hOrWj", nxt.getFirst());

        Map<String, Object> ked0 = new HashMap<>();
        ked0.put("v", "KERI10JSON000000_");
        ked0.put("t", Ilks.ICP.getValue());
        ked0.put("d", "");
        ked0.put("i", "");
        ked0.put("s", Integer.toHexString(sn));
        ked0.put("kt", Integer.toHexString(sith));
        ked0.put("k", keys);
        ked0.put("nt", Integer.toHexString(nsith));
        ked0.put("n", nxt);
        ked0.put("bt", Integer.toHexString(toad));
        ked0.put("b", new ArrayList<>());
        ked0.put("c", new ArrayList<>());
        ked0.put("a", new ArrayList<>());

        Serder serder = new Serder(ked0);

        exchange.sendFromEvents("aid1", "", serder, Collections.singletonList(""), "", new ArrayList<>());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/identifiers/aid1/exchanges", request.getPath());
    }

    @Test
    @DisplayName("Get exchange")
    void getExchange() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Exchanging.Exchanges exchanges = client.exchanges();
        String exchangeId = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";

        exchanges.get(exchangeId);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/exchanges/" + exchangeId, request.getPath());
    }
}
