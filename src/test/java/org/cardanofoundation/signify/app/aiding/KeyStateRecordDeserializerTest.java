package org.cardanofoundation.signify.app.aiding;

import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test KeyStateRecordDeserializer handles kt/nt fields in various formats
 */
class KeyStateRecordDeserializerTest {

    @Test
    @DisplayName("Deserialize kt as string")
    void testKtAsString() {
        String json = """
            {
              "vn": [1, 0],
              "i": "EK3",
              "s": "0",
              "p": "",
              "d": "EK3",
              "f": "0",
              "dt": "2023-08-23T15:16:07.553000+00:00",
              "et": "icp",
              "kt": "1",
              "k": ["DPmhSfdhCPxr3EqjxzEtF8TVy0YX7ATo0Uc8oo2cnmY9"],
              "nt": "1",
              "n": ["EAORnRtObOgNiOlMolji-KijC_isa3lRDpHCsol79cOc"],
              "bt": "0",
              "b": [],
              "c": [],
              "ee": { "s": "0", "d": "EK3", "br": [], "ba": [] },
              "di": ""
            }
            """;

        KeyStateRecord record = Utils.fromJson(json, KeyStateRecord.class);

        assertNotNull(record);
        assertEquals("EK3", record.getI());
        assertEquals("1", record.getKt());
        assertEquals("1", record.getNt());
    }

    @Test
    @DisplayName("Deserialize kt as array")
    void testKtAsArray() {
        String json = """
            {
              "vn": [1, 0],
              "i": "EK3",
              "s": "0",
              "p": "",
              "d": "EK3",
              "f": "0",
              "dt": "2023-08-23T15:16:07.553000+00:00",
              "et": "icp",
              "kt": ["1/2", "1/2", "1/2"],
              "k": ["DPmhSfdhCPxr3EqjxzEtF8TVy0YX7ATo0Uc8oo2cnmY9"],
              "nt": ["1/2", "1/2"],
              "n": ["EAORnRtObOgNiOlMolji-KijC_isa3lRDpHCsol79cOc"],
              "bt": "0",
              "b": [],
              "c": [],
              "ee": { "s": "0", "d": "EK3", "br": [], "ba": [] },
              "di": ""
            }
            """;

        KeyStateRecord record = Utils.fromJson(json, KeyStateRecord.class);

        assertNotNull(record);
        assertEquals("EK3", record.getI());
        // Arrays should be converted to JSON string representation
        assertTrue(record.getKt().contains("1/2"));
        assertTrue(record.getNt().contains("1/2"));
    }

    @Test
    @DisplayName("Deserialize kt as number")
    void testKtAsNumber() {
        String json = """
            {
              "vn": [1, 0],
              "i": "EK3",
              "s": "0",
              "p": "",
              "d": "EK3",
              "f": "0",
              "dt": "2023-08-23T15:16:07.553000+00:00",
              "et": "icp",
              "kt": 2,
              "k": ["DPmhSfdhCPxr3EqjxzEtF8TVy0YX7ATo0Uc8oo2cnmY9"],
              "nt": 3,
              "n": ["EAORnRtObOgNiOlMolji-KijC_isa3lRDpHCsol79cOc"],
              "bt": "0",
              "b": [],
              "c": [],
              "ee": { "s": "0", "d": "EK3", "br": [], "ba": [] },
              "di": ""
            }
            """;

        KeyStateRecord record = Utils.fromJson(json, KeyStateRecord.class);

        assertNotNull(record);
        assertEquals("EK3", record.getI());
        assertEquals("2", record.getKt());
        assertEquals("3", record.getNt());
    }

    @Test
    @DisplayName("Deserialize with mixed kt string and nt array")
    void testMixedTypes() {
        String json = """
            {
              "vn": [1, 0],
              "i": "EK3",
              "s": "0",
              "p": "",
              "d": "EK3",
              "f": "0",
              "dt": "2023-08-23T15:16:07.553000+00:00",
              "et": "icp",
              "kt": "1",
              "k": ["DPmhSfdhCPxr3EqjxzEtF8TVy0YX7ATo0Uc8oo2cnmY9"],
              "nt": ["1/2", "1/2"],
              "n": ["EAORnRtObOgNiOlMolji-KijC_isa3lRDpHCsol79cOc"],
              "bt": "0",
              "b": [],
              "c": [],
              "ee": { "s": "0", "d": "EK3", "br": [], "ba": [] },
              "di": ""
            }
            """;

        KeyStateRecord record = Utils.fromJson(json, KeyStateRecord.class);

        assertNotNull(record);
        assertEquals("EK3", record.getI());
        assertEquals("1", record.getKt());
        assertTrue(record.getNt().contains("1/2"));
    }
}

