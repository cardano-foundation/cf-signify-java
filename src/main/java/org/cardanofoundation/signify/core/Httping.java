package org.cardanofoundation.signify.core;

public class Httping {
    public static String HEADER_SIG_INPUT = normalize("Signature-Input");
    public static String HEADER_SIG_TIME = normalize("Signify-Timestamp");

    public static String normalize(String header) {
        return header.trim();
    }

    /** Parse start, end and total from HTTP Content-Range header value
     * @param header HTTP Range header value
     * @param typ type of range, e.g. "aids"
     * @return object with start, end and total properties
     */
    public static RangeInfo parseRangeHeaders(String header, String typ) {
        if (header != null) {
            String data = header.replace(typ + " ", "");
            String[] values = data.split("/");
            String[] rng = values[0].split("-");

            return new RangeInfo(
                Integer.parseInt(rng[0]),
                Integer.parseInt(rng[1]),
                Integer.parseInt(values[1])
            );
        } else {
            return new RangeInfo(0, 0, 0);
        }
    }

    public record RangeInfo(int start, int end, int total) {}
}
