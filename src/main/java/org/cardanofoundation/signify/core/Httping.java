package org.cardanofoundation.signify.core;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.cesr.util.Utils;

import com.goterl.lazysodium.exceptions.SodiumException;

import lombok.Getter;
import lombok.Setter;

public class Httping {
    public static String HEADER_SIG_INPUT = normalize("Signature-Input");
    public static String HEADER_SIG_TIME = normalize("Signify-Timestamp");

    public static String normalize(String header) {
        return header.trim();
    }

    @Getter
    @Setter
    public static class SiginputArgs {
        private String name;
        private String method;
        private String path;
        private Map<String, String> headers;
        private List<String> fields;
        private Integer expires;
        private String nonce;
        private String alg;
        private String keyid;
        private String context;
    }

    @Getter
    @Setter
    public static class Inputage {
        private Object name;
        private Object fields;
        private Object created;
        private Object expires;
        private Object nonce;
        private Object alg;
        private Object keyid;
        private Object context;
    }

    public record SiginputResult(Map<String, String> headers, Object sig) {
    }

    public static SiginputResult siginput(Signer signer, SiginputArgs args) throws SodiumException {
        final List<String> items = new ArrayList<>();
        List<Entry<String, Map<String, String>>> ifields = new ArrayList<>();

        for (String field : args.fields) {
            if (field.startsWith("@")) {
                switch (field) {
                    case "@method":
                        items.add("\"" + field + "\": " + args.method);
                        ifields.add(new AbstractMap.SimpleEntry<>(field, new HashMap<>()));
                        break;
                    case "@path":
                        items.add("\"" + field + "\": " + args.path);
                        ifields.add(new AbstractMap.SimpleEntry<>(field, new HashMap<>()));
                        break;
                }
            } else {
                if (!args.headers.containsKey(field)) continue;

                ifields.add(new AbstractMap.SimpleEntry<>(field, new HashMap<>()));
                String value = normalize(args.headers.get(field).toString());
                items.add("\"" + field + "\": " + value);
            }
        }

        Map<String, Object> nameParams = new HashMap<>();
        long now = System.currentTimeMillis() / 1000;
        nameParams.put("created", now);

        List<String> values = new ArrayList<>();
        values.add("(" + String.join(" ", ifields.stream().map(Map.Entry::getKey).toArray(String[]::new)) + ")");
        values.add("created=" + now);

        if (args.expires != null) {
            values.add("expires=" + args.expires);
            nameParams.put("expires", args.expires);
        }
        if (args.nonce != null) {
            values.add("nonce=" + args.nonce);
            nameParams.put("nonce", args.nonce);
        }
        if (args.keyid != null) {
            values.add("keyid=" + args.keyid);
            nameParams.put("keyid", args.keyid);
        }
        if (args.context != null) {
            values.add("context=" + args.context);
            nameParams.put("context", args.context);
        }
        if (args.alg != null) {
            values.add("alg=" + args.alg);
            nameParams.put("alg", args.alg);
        }

        Map<String, Object> sid = new HashMap<>();
        sid.put(args.name, Arrays.asList(ifields, nameParams));

        String params = String.join(";", values);
        items.add("\"@signature-params: " + params + "\"");

        String ser = String.join("\n", items);
        Object sig = signer.sign(ser.getBytes());

        Map<String, String> headers = new HashMap<>();

        // TODO find the way to serialize the map like in signify-ts
        headers.put(HEADER_SIG_INPUT, Utils.jsonStringify(sid));

        return new SiginputResult(headers, sig);
    }

    public static List<Inputage> desiginput(String value) {
        // TODO implement deserialization
        return new ArrayList<>();
    }

    /**
     * Parse start, end and total from HTTP Content-Range header value
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
