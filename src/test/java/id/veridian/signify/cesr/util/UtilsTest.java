package org.cardanofoundation.signify.cesr.util;

import org.cardanofoundation.signify.cesr.Saider;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.exception.SignifySerializationException;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    @Test
    public void testSerializeIssExnAttachment() {
        Map<String, Object> sad = new LinkedHashMap<>();
        sad.put("d", "");
        sad.put("v", CoreUtil.versify(CoreUtil.Ident.KERI, null, CoreUtil.Serials.JSON, 0));
        Saider.SaidifyResult saidifiedData = Saider.saidify(sad);
        Map<String, Object> data = saidifiedData.sad();

        // Serialize
        byte[] result = Utils.serializeIssExnAttachment(new Serder(data));

        // Assert
        assertEquals(
                "-VAS-GAB0AAAAAAAAAAAAAAAAAAAAAAAEKZPmzJqhx76bcC2ftPQgeRirmOd8ZBOtGVqHJrSm7F1",
                new String(result)
        );
    }

    @Test
    public void testSerializeACDCAttachment() {
        // Prepare data
        Map<String, Object> sad = new LinkedHashMap<>();
        sad.put("i", "EP-hA0w9X5FDonCDxQv32OTCAvcxkZxgDLOnDb3Jcn3a");
        sad.put("d", "");
        sad.put("v", CoreUtil.versify(CoreUtil.Ident.ACDC, null, CoreUtil.Serials.JSON, 0));
        sad.put("a", Map.of("LEI", "123"));

        Saider.SaidifyResult saidifiedData = Saider.saidify(sad);
        Map<String, Object> data = saidifiedData.sad();

        // Serialize
        byte[] result = Utils.serializeACDCAttachment(new Serder(data));

        assertEquals(
                "-IABEP-hA0w9X5FDonCDxQv32OTCAvcxkZxgDLOnDb3Jcn3a0AAAAAAAAAAAAAAAAAAAAAAAEHGU7u7cSMjMcJ1UyN8r-MnoZ3cDw4sMQNYxRLjqGVJI",
                new String(result)
        );
    }

    @Test
    public void testFromJsonFailureThrowsSerializationExceptionWithPayload() {
        SignifySerializationException exception = assertThrows(
                SignifySerializationException.class,
                () -> Utils.fromJson("{not valid json", Map.class)
        );

        assertNotNull(exception.getCause());
        assertTrue(exception.getMessage().contains("{not valid json"),
                "message should include the offending payload");
    }

    @Test
    public void testJsonStringifyFailureThrowsSerializationException() {
        Object unserializable = new Object() {
            public Object getValue() {
                throw new IllegalStateException("boom");
            }
        };

        SignifySerializationException exception = assertThrows(
                SignifySerializationException.class,
                () -> Utils.jsonStringify(unserializable)
        );

        assertNotNull(exception.getCause());
    }

    @Test
    public void testConcatByteArrays() {
        byte[] array1 = {1, 2, 3};
        byte[] array2 = {4, 5, 6};
        byte[] expected = {1, 2, 3, 4, 5, 6};
        byte[] result = Utils.concatByteArrays(array1, array2);
        assertArrayEquals(expected, result, "The concatenated array is not as expected.");

        array1 = new byte[]{};
        array2 = new byte[]{4, 5, 6};
        expected = new byte[]{4, 5, 6};

        result = Utils.concatByteArrays(array1, array2);
        assertArrayEquals(expected, result, "The concatenated array is not as expected.");

        array1 = new byte[]{1, 2, 3};
        array2 = new byte[]{};
        expected = new byte[]{1, 2, 3};

        result = Utils.concatByteArrays(array1, array2);
        assertArrayEquals(expected, result, "The concatenated array is not as expected.");

        array1 = new byte[]{};
        array2 = new byte[]{};
        expected = new byte[]{};

        result = Utils.concatByteArrays(array1, array2);

        assertArrayEquals(expected, result, "The concatenated array is not as expected.");
    }

    @Test
    public void testDateTimeStringIsFixedWidth() {
        // Whole-second instants must still produce the 6-digit fraction KERI requires
        assertEquals(
                "2026-06-10T06:53:58.000000+00:00",
                Utils.toDateTimeString(java.time.Instant.parse("2026-06-10T06:53:58Z")));
        assertEquals(
                "2026-06-10T06:53:58.166000+00:00",
                Utils.toDateTimeString(java.time.Instant.parse("2026-06-10T06:53:58.166Z")));

        String now = Utils.currentDateTimeString();
        assertEquals(32, now.length(), "KERI datetime must be fixed-width: " + now);
    }
}
