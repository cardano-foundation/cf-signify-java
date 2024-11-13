package org.cardanofoundation.signify.cesr.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;

public class Utils {
    public static byte[] intToBytes(BigInteger value, int size) {
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }

        byte[] result = new byte[size];
        byte[] valueBytes = value.toByteArray();

        // Remove leading zero byte if present (BigInteger's sign byte)
        if (valueBytes.length > 1 && valueBytes[0] == 0) {
            byte[] tmp = new byte[valueBytes.length - 1];
            System.arraycopy(valueBytes, 1, tmp, 0, tmp.length);
            valueBytes = tmp;
        }

        if (valueBytes.length > size) {
            throw new IllegalArgumentException(
                String.format("Value too large: needs %d bytes, but size is limited to %d",
                    valueBytes.length, size)
            );
        }
        // Copy to result array, padding with leading zeros if necessary
        int offset = size - valueBytes.length;
        System.arraycopy(valueBytes, 0, result, offset, valueBytes.length);

        return result;
    }

    public static BigInteger bytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return BigInteger.ZERO;
        }
        // Ensure positive number by adding a leading zero byte
        byte[] positiveBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, positiveBytes, 1, bytes.length);
        return new BigInteger(positiveBytes);
    }

    public static String jsonStringify(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error while stringify");
        }
    }

    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return Map.of();
        }

        Map<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (Exception e) {
                throw new RuntimeException("Unable to create map form object");
            }
        }

        return map;
    }

    public static List<String> toList(Object obj) {
        return switch (obj) {
            case String s -> List.of(s);
            case Object[] arr -> Arrays.stream(arr)
                .map(String::valueOf)
                .toList();
            case Collection<?> col -> col.stream()
                .map(String::valueOf)
                .toList();
            case null, default -> Collections.emptyList();
        };
    }
}
