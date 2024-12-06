package org.cardanofoundation.signify.core;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Siger;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.cesr.Verfer;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Httping.SiginputArgs;
import org.cardanofoundation.signify.end.Signage;

import com.goterl.lazysodium.exceptions.SodiumException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Authenticater {
    private static final List<String> DEFAULT_FIELDS = List.of(
            "@method",
            "@path",
            "signify-resource",
            Httping.HEADER_SIG_TIME.toLowerCase()
    );

    private final Verfer verfer;
    private final Signer csig;

    public Authenticater(Signer csig, Verfer verfer) {
        this.csig = csig;
        this.verfer = verfer;
    }

    public boolean verify(Map<String, String> headers, String method, String path) {
        String sigInput = headers.get(Httping.HEADER_SIG_INPUT.toLowerCase());

        final String signature = headers.get("signature");
        List<Httping.Inputage> inputs = Httping.desiginput(sigInput);
        inputs = inputs.stream().filter(input -> input.getName().equals("signify")).toList();

        if (inputs.isEmpty()) {
            return false;
        }

        inputs.forEach(input -> {
            List<String> items = new ArrayList<>();
            (Utils.toList(input.getFields())).forEach(field -> {
                if (field.startsWith("@")) {
                    if (field.equals("@method")) {
                        items.add("\"" + field + "\": " + method);
                    } else if (field.equals("@path")) {
                        items.add("\"" + field + "\": " + path);
                    }
                } else {
                    if (headers.containsKey(field)) {
                        String value = Httping.normalize(headers.get(field));
                        items.add("\"" + field + "\": " + value);
                    }
                }
            });

            List<String> values = new ArrayList<>();
            values.add("(" + String.join(" ", Utils.toList(input.getFields())) + ")");
            values.add("created=" + input.getCreated());
            if (input.getExpires() != null) {
                values.add("expires=" + input.getExpires());
            }
            if (input.getNonce() != null) {
                values.add("nonce=" + input.getNonce());
            }
            if (input.getKeyid() != null) {
                values.add("keyid=" + input.getKeyid());
            }
            if (input.getContext() != null) {
                values.add("context=" + input.getContext());
            }
            if (input.getAlg() != null) {
                values.add("alg=" + input.getAlg());
            }
            String params = String.join(";", values);
            items.add("\"@signature-params: " + params + "\"");
            String ser = String.join("\n", items);

            List<Signage> signages = Signage.designature(signature);
            Map<String, Siger> markers = (Map<String, Siger>) signages.get(0).getMarkers();
            Siger cig = markers.get(input.getName());
            if (cig == null || !this.verfer.verify(cig.getRaw(), ser.getBytes())) {
                throw new IllegalArgumentException("Signature for " + input.getKeyid() + " invalid.");
            }
        });

        return true;
    }

    public Map<String, String> sign(
        Map<String, String> headers,
        String method,
        String path,
        List<String> fields
    ) throws SodiumException {
        if (fields == null) {
            fields = DEFAULT_FIELDS;
        }

        SiginputArgs siginputArgs = new SiginputArgs();
        siginputArgs.setName("signify");
        siginputArgs.setMethod(method);
        siginputArgs.setPath(path);
        siginputArgs.setHeaders(headers);
        siginputArgs.setFields(fields);
        siginputArgs.setAlg("ed25519");
        siginputArgs.setKeyid(csig.getVerfer().getQb64());

        Httping.SiginputResult siginputResult = Httping.siginput(csig, siginputArgs);
        Map<String, String> signedHeaders = siginputResult.headers();

        headers.putAll(signedHeaders);

        final Map<String, String> markers = new HashMap<>();
        markers.put("signify", siginputResult.sig().toString());
        final Signage signage = new Signage(markers, false);
        final Map<String, String> signed = Signage.signature(List.of(signage));

        headers.putAll(signed);

        return headers;
    }
}
