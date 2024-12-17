package org.cardanofoundation.signify.cesr.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidSizeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.exceptions.serialize.SerializeException;

import java.math.BigInteger;
import java.util.*;

public class Utils {
    public static final int CRYPTO_BOX_SEAL_BYTES = 48;

    public static byte[] intToBytes(BigInteger value, int size) {
        if (value.signum() < 0) {
            throw new InvalidValueException("Value must be non-negative");
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
            throw new InvalidSizeException(
                String.format("Value too large: needs %d bytes, but size is limited to %d",
                    valueBytes.length, size)
            );
        }
        // Copy to result array, padding with leading zeros if necessary
        int offset = size - valueBytes.length;
        System.arraycopy(valueBytes, 0, result, offset, valueBytes.length);

        return result;
    }

    public static byte[] intToBytes(int value, int length) {
        byte[] byteArray = new byte[length]; // Assuming a 4-byte integer (32 bits)
    
        for (int index = byteArray.length - 1; index >= 0; index--) {
            byte byteValue = (byte) (value & 0xff);
            byteArray[index] = byteValue;
            value = (value - byteValue) / 256;
        }
        return byteArray;
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
            throw new SerializeException("Error while stringify");
        }
    }

    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return Map.of();
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(obj, new TypeReference<>() {});
        } catch (Exception e) {
            throw new SerializeException("Unable to create map from object");
        }
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

    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
