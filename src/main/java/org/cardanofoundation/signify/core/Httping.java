package org.cardanofoundation.signify.core;

import java.util.*;
import java.util.Map.Entry;

import org.cardanofoundation.signify.cesr.Signer;

import com.goterl.lazysodium.exceptions.SodiumException;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.greenbytes.http.sfv.*;
import org.greenbytes.http.sfv.Dictionary;

public class Httping {
    public static String HEADER_SIG_INPUT = normalize("Signature-Input");
    public static String HEADER_SIG_TIME = normalize("Signify-Timestamp");

    public static String normalize(String header) {
        return header.toLowerCase().trim();
    }

    public static SiginputResult siginput(Signer signer, SiginputArgs args) throws SodiumException {
        final List<String> items = new ArrayList<>();
        final List<String> ifields = new ArrayList<>();
        for (String field : args.fields) {
            if (field.startsWith("@")) {
                switch (field) {
                    case "@method":
                        items.add("\"" + field + "\": " + args.method);
                        ifields.add(field);
                        break;
                    case "@path":
                        items.add("\"" + field + "\": " + args.path);
                        ifields.add(field);
                        break;
                }
            } else {
                if (!args.headers.containsKey(field)) continue;

                ifields.add(field);
                String value = args.headers.get(field);
                items.add("\"" + field + "\": " + value);
            }
        }

        Map<String, Object> nameParams = new HashMap<>();
        long now = Utils.currentTimeSeconds();
        nameParams.put("created", now);

        List<String> values = new ArrayList<>();
        values.add("(" + String.join(" ", ifields) + ")");
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

        // Create a dictionary with the structured fields
        List<Item<?>> itemsList = new ArrayList<>();
        ifields.forEach(s -> itemsList.add(StringItem.valueOf(s)));
        Parameters parameters = Parameters.valueOf(nameParams);
        InnerList innerList = InnerList.valueOf(itemsList)
                .withParams(parameters);
        Map<String, ListElement<?>> dicMap = new LinkedHashMap<>();
        dicMap.put(args.name, innerList);
        Dictionary dictionary = Dictionary.valueOf(dicMap);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HEADER_SIG_INPUT, dictionary.serialize());

        String params = String.join(";", values);
        items.add("\"@signature-params: " + params + "\"");

        String ser = String.join("\n", items);
        Object sig = signer.sign(ser.getBytes());

        return new SiginputResult(headers, sig);
    }

    public static List<Inputage> desiginput(String value) {
        Parser parser = new Parser(value);
        Dictionary dictionary = parser.parseDictionary();
        List<Inputage> siginputs = new ArrayList<>();

        Map<String, ListElement<?>> rawMap = dictionary.get();
        for (Entry<String, ListElement<?>> entry : rawMap.entrySet()) {
            Inputage siginput = new Inputage();

            // set name
            siginput.setName(entry.getKey());

            // set fields
            InnerList innerList = (InnerList) entry.getValue();
            List<String> fields = innerList.get().stream().map(item -> String.valueOf(item.get())).toList();
            siginput.setFields(fields);

            // extract parameters
            Parameters params = innerList.getParams();
            if (!params.containsKey("created")) {
                throw new IllegalArgumentException("missing required `created` field from signature input");
            }
            siginput.setCreated(params.get("created").get());

            if (params.containsKey("expires")) {
                siginput.setExpires(params.get("expires").get());
            }

            if (params.containsKey("nonce")) {
                siginput.setNonce(params.get("nonce").get());
            }

            if (params.containsKey("alg")) {
                siginput.setAlg(params.get("alg").get());
            }

            if (params.containsKey("keyid")) {
                siginput.setKeyid(params.get("keyid").get());
            }

            if (params.containsKey("context")) {
                siginput.setContext(params.get("context").get());
            }

            siginputs.add(siginput);
        }

        return siginputs;
    }

    /**
     * Parse start, end and total from HTTP Content-Range header value
     *
     * @param header HTTP Range header value
     * @param typ    type of range, e.g. "aids"
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
        private List<String> fields;
        private Object created;
        private Object expires;
        private Object nonce;
        private Object alg;
        private Object keyid;
        private Object context;
    }

    public record SiginputResult(Map<String, String> headers, Object sig) {
    }

    public record RangeInfo(int start, int end, int total) {
    }
}
