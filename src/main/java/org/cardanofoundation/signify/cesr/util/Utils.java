package org.cardanofoundation.signify.cesr.util;

import java.math.BigInteger;

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
}
