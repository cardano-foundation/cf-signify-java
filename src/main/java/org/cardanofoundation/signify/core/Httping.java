package org.cardanofoundation.signify.core;

public class Httping {
    public static String HEADER_SIG_INPUT = normalize("Signature-Input");
    public static String HEADER_SIG_TIME = normalize("Signify-Timestamp");

    public static String normalize(String header) {
        return header.trim();
    }

}
