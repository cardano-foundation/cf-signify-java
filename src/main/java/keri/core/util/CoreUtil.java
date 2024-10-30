package keri.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.util.stream.Collectors;

public class CoreUtil {

    public static final Map<Integer, String> b64ChrByIdx = new HashMap<>() {{
        put(0, "A");
        put(1, "B");
        put(2, "C");
        put(3, "D");
        put(4, "E");
        put(5, "F");
        put(6, "G");
        put(7, "H");
        put(8, "I");
        put(9, "J");
        put(10, "K");
        put(11, "L");
        put(12, "M");
        put(13, "N");
        put(14, "O");
        put(15, "P");
        put(16, "Q");
        put(17, "R");
        put(18, "S");
        put(19, "T");
        put(20, "U");
        put(21, "V");
        put(22, "W");
        put(23, "X");
        put(24, "Y");
        put(25, "Z");
        put(26, "a");
        put(27, "b");
        put(28, "c");
        put(29, "d");
        put(30, "e");
        put(31, "f");
        put(32, "g");
        put(33, "h");
        put(34, "i");
        put(35, "j");
        put(36, "k");
        put(37, "l");
        put(38, "m");
        put(39, "n");
        put(40, "o");
        put(41, "p");
        put(42, "q");
        put(43, "r");
        put(44, "s");
        put(45, "t");
        put(46, "u");
        put(47, "v");
        put(48, "w");
        put(49, "x");
        put(50, "y");
        put(51, "z");
        put(52, "0");
        put(53, "1");
        put(54, "2");
        put(55, "3");
        put(56, "4");
        put(57, "5");
        put(58, "6");
        put(59, "7");
        put(60, "8");
        put(61, "9");
        put(62, "-");
        put(63, "_");
    }};

    public static final Map<String, Integer> b64IdxByChr = b64ChrByIdx.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public static String intToB64(int i, int l) {
        StringBuilder out = new StringBuilder();
        while (l != 0) {
            out.insert(0, b64ChrByIdx.get(i % 64));
            i = i / 64;
            if (i == 0) {
                break;
            }
        }   

        int x = l - out.length();
        for (int j = 0; j < x; j++) {
            out.insert(0, 'A');
        }

        return out.toString();
    }

    public static String encodeBase64Url(byte[] buffer) {
        if(buffer == null) {
            throw new IllegalArgumentException("`buffer` must be a byte array.");
        }
        String base64 = Base64.getEncoder().encodeToString(buffer);
        return base64.replace('+', '-')
                    .replace('/', '_')
                    .replace("=", "");
    }

    public static byte[] decodeBase64Url(String input) {
        if (input == null) {
            throw new IllegalArgumentException("`input` must be a string.");
        }

        int n = input.length() % 4;
        String padded = input + "=".repeat(n > 0 ? 4 - n : n);
        String base64String = padded.replace('-', '+').replace('_', '/');
        return Base64.getDecoder().decode(base64String);
    }

    public static int readInt(byte[] array) {
        int value = 0;
        for (byte b : array) {
            value = value * 256 + (b & 0xFF);
        }
        return value;
    }

    public static int b64ToInt(String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Empty string, conversion undefined.");
        }

        int i = 0;
        String[] rev = new StringBuilder(s).reverse().toString().split("");
        for (int e = 0; e < rev.length; e++) {
            String c = rev[e];
            i |= b64IdxByChr.get(c) * (1 << (e * 6));
        }

        return i;
    }
}
