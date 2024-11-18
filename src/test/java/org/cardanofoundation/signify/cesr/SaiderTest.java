package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Saider.DeriveResult;
import org.cardanofoundation.signify.cesr.Saider.SaidifyResult;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ident;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Version;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SaiderTest {

    @Test
    @DisplayName("should create Saidified dicts")
    void shouldCreateSaidifiedDicts() {
        Serials kind = Serials.JSON;
        String code = MatterCodex.Blake3_256.getValue();

        // Create version string
        String vs = CoreUtil.versify(
            Ident.KERI,
            new Version(),
            kind,
            0  // vacuous size = 0
        );
        assertEquals("KERI10JSON000000_", vs);

        // Create nested attributes map
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("d", "EBabiu_JCkE0GbiglDXNB5C4NQq-hiGgxhHKXBxkiojg");
        attributes.put("i", "EB0_D51cTh_q6uOQ-byFiv5oNXZ-cxdqCqBAa4JmBLtb");
        attributes.put("name", "John Jones");
        attributes.put("role", "Founder");

        // Create main sad4 map
        Map<String, Object> sad4 = new LinkedHashMap<>();
        sad4.put("v", vs);
        sad4.put("t", "rep");
        sad4.put("d", "");  // vacuous said
        sad4.put("dt", "2020-08-22T17:50:12.988921+00:00");
        sad4.put("r", "logs/processor");
        sad4.put("a", attributes);

        Saider saider = new Saider(sad4);  // default version string code, kind, and label
        assertEquals(code, saider.getCode());
        assertEquals(
            "ELzewBpZHSENRP-sL_G_2Ji4YDdNkns9AzFzufleJqdw",
            saider.getQb64()
        );
        assertTrue(saider.verify(sad4, false, false, null, "d"));
        assertFalse(saider.verify(sad4, false, true, null, "d"));
        assertFalse(saider.verify(sad4, true, false, null, "d"));

        Map<String, Object> sad5 = new LinkedHashMap<>(sad4);
        sad5.put("d", saider.getQb64()); // assign said to label field
        assertTrue(saider.verify(sad5, true, false, null, "d")); // default kind label

        Map<String, Object> sad6 = new LinkedHashMap<>(sad5);
        DeriveResult dr = Saider.derive(sad4, code, kind, "d");
        sad6.put("v", dr.sad().get("v"));
        assertTrue(saider.verify(sad6, true, false, null, "d"));

        String said3 = saider.getQb64();
        saider = new Saider(said3);
        assertEquals(code, saider.getCode());
        assertEquals(said3, saider.getQb64());

        String ser5 = Utils.jsonStringify(sad5);
        String expectedSer = "{\"v\":\"KERI10JSON000000_\",\"t\":\"rep\",\"d\":\"ELzewBpZHSENRP-sL_G_2Ji4YDdNkns9AzFzufleJqdw\",\"dt\":\"2020-08-22T17:50:12.988921+00:00\",\"r\":\"logs/processor\",\"a\":{\"d\":\"EBabiu_JCkE0GbiglDXNB5C4NQq-hiGgxhHKXBxkiojg\",\"i\":\"EB0_D51cTh_q6uOQ-byFiv5oNXZ-cxdqCqBAa4JmBLtb\",\"name\":\"John Jones\",\"role\":\"Founder\"}}";
        assertEquals(expectedSer, ser5);
    }
} 