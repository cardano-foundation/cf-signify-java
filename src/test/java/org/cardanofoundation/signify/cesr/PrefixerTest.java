package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrefixerTest {

    @Test
    @DisplayName("should create autonomic identifier prefix using derivation as determined by code from ked")
    void shouldCreateAutonomicIdentifierPrefix() {
        // Create verkey byte array (from keripy)
        byte[] verkey = new byte[] {
            (byte)172, (byte)114, (byte)218, (byte)200, (byte)51, (byte)126, (byte)153, (byte)114, 
            (byte)175, (byte)235, (byte)96, (byte)192, (byte)140, (byte)82, (byte)215, (byte)215, 
            (byte)246, (byte)57, (byte)200, (byte)69, (byte)30, (byte)210, (byte)240, (byte)61, 
            (byte)96, (byte)247, (byte)191, (byte)138, (byte)24, (byte)138, (byte)96, (byte)113
        };

        // Test direct derivation
        Prefixer prefixer = new Prefixer(
            RawArgs.builder()
                .raw(verkey)
                .code(MatterCodex.Ed25519.getValue())
                .build(),
            null
        );

        assertEquals(MatterCodex.Ed25519.getValue(), prefixer.getCode());
        assertEquals(
            "DKxy2sgzfplyr-tgwIxS19f2OchFHtLwPWD3v4oYimBx",
            prefixer.getQb64()
        );

        // Test digest derivation from inception ked
        String vs = CoreUtil.versify(
            CoreUtil.Ident.KERI,
            new CoreUtil.Version(),
            CoreUtil.Serials.JSON,
            0
        );
        int sn = 0;
        String ilk = CoreUtil.Ilks.ICP.getValue();
        String sith = "1";
        ArrayList<String> keys = new ArrayList<>();
        keys.add(new Prefixer(
            RawArgs.builder()
                .raw(verkey)
                .code(MatterCodex.Ed25519.getValue())
                .build(),
            null
        ).getQb64());
        String nxt = "";
        int toad = 0;
        ArrayList<String> wits = new ArrayList<>();
        ArrayList<String> cnfg = new ArrayList<>();

        Map<String, Object> ked = new LinkedHashMap<>();
        ked.put("v", vs);                         // version string
        ked.put("i", "");                         // qb64 prefix
        ked.put("s", Integer.toHexString(sn));    // hex string no leading zeros lowercase
        ked.put("t", ilk);
        ked.put("kt", sith);                      // hex string no leading zeros lowercase
        ked.put("k", keys);                       // list of qb64
        ked.put("n", nxt);                        // hash qual Base64
        ked.put("wt", Integer.toHexString(toad)); // hex string no leading zeros lowercase
        ked.put("w", wits);                       // list of qb64 may be empty
        ked.put("c", cnfg);                       // list of config ordered mappings may be empty

        prefixer = new Prefixer(
            RawArgs.builder()
                .code(MatterCodex.Blake3_256.getValue())
                .build(),
            ked
        );

        assertEquals(
            "ELEjyRTtmfyp4VpTBTkv_b6KONMS1V8-EW-aGJ5P_QMo",
            prefixer.getQb64()
        );
    }
}

