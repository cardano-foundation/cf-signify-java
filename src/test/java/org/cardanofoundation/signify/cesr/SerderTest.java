package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Version;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerderTest {

    @Test
    @DisplayName("should parse a KERI event version string")
    void shouldParseKeriEventVersionString() {
        CoreUtil.DeversifyResult result = CoreUtil.deversify("KERI10JSON00011c_");
        assertEquals(Serials.JSON.getValue(), result.kind().getValue());
        assertEquals(new Version(1, 0), result.version());
        assertEquals("00011c", result.string());
    }

    @Test
    @DisplayName("should create KERI events from dicts")
    void shouldCreateKeriEventsFromDicts() throws Exception {
        int sith = 1;
        int nsith = 1;
        int sn = 0;
        int toad = 0;

        byte[] raw = new byte[]{
            5, (byte)170, (byte)143, 45, 83, (byte)154, (byte)233, (byte)250,
            85, (byte)156, 2, (byte)156, (byte)155, 8, 72, 117
        };

        Salter salter = new Salter(RawArgs.builder().code(MatterCodex.Salt_128.getValue()).raw(raw).build());

        Signer skp0 = salter.signer(
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            "A",
            Tier.LOW,
            true
        );
        List<String> keys = new ArrayList<>();
        keys.add(skp0.getVerfer().getQb64());

        Signer skp1 = salter.signer(
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            "N",
            Tier.LOW,
            true
        );

        Diger ndiger = new Diger(
            RawArgs.builder().build(),
            skp1.getVerfer().getQb64b());
        List<String> nxt = new ArrayList<>();
        nxt.add(ndiger.getQb64());
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
        assertEquals(
            "{\"v\":\"KERI10JSON0000d3_\",\"t\":\"icp\",\"d\":\"\",\"i\":\"\",\"s\":\"0\"," +
            "\"kt\":\"1\",\"k\":[\"DAUDqkmn-hqlQKD8W-FAEa5JUvJC2I9yarEem-AAEg3e\"],\"nt\":\"1\"," +
            "\"n\":[\"EAKUR-LmLHWMwXTLWQ1QjxHrihBmwwrV2tYaSG7hOrWj\"],\"bt\":\"0\",\"b\":[],\"c\":[],\"a\":[]}",
            serder.getRaw()
        );

        Prefixer aid0 = new Prefixer(MatterCodex.Ed25519.getValue(), ked0);

        assertEquals(MatterCodex.Ed25519.getValue(), aid0.getCode());
        assertEquals(skp0.getVerfer().getQb64(), aid0.getQb64());
        assertEquals(
            "DAUDqkmn-hqlQKD8W-FAEa5JUvJC2I9yarEem-AAEg3e",
            skp0.getVerfer().getQb64()
        );

        aid0 = new Prefixer(MatterCodex.Blake3_256.getValue(), ked0);
        assertEquals(
            "ECHOi6qRaswNpvytpCtpvEh2cB2aLAwVHBLFinno3YVW",
            aid0.getQb64()
        );

        Map<String, Object> attr = new LinkedHashMap<>();
        attr.put("n", "Lenksjö");
        ked0.put("a", attr);

        Serder serder1 = new Serder(ked0);
        assertEquals("KERI10JSON000139_", serder1.getKed().get("v"));
    }


}
