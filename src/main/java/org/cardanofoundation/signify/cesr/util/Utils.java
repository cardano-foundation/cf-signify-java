package org.cardanofoundation.signify.cesr.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;

public class Utils {
    public static boolean isLessThan(BigInteger a, Number b) {
        BigInteger newb = BigInteger.valueOf((long) b);
        return a.compareTo(newb) <= 0;
    }

    public static byte[] intToBytes(BigInteger value, int size) {
        byte[] bytes = new byte[size];
        byte[] valueBytes = value.toByteArray();
        int start = Math.max(0, valueBytes.length - size);
        for (int i = 0; i < size; i++) {
            bytes[i] = (i + start < valueBytes.length) ? valueBytes[i + start] : 0;
        }
        return bytes;
    }

    public static BigInteger bytesToInt(byte[] ar) {
        return new BigInteger(1, ar);
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
