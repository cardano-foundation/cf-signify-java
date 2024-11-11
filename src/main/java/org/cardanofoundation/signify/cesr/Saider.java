package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Saider extends Matter {
    private static final String Dummy = "#";

    @Getter
    public enum Ids {
        d("d");

        private final String value;
        Ids(String value) {
            this.value = value;
        }
    }

    private static class Digestage {
        public Deriver klas;
        public Integer size;
        public Integer length;

        public Digestage(Deriver klas, Integer size, Integer length) {
            this.klas = klas;
            this.size = size == null ? 0 : size;
            this.length = length == null ? 0 : length;
        }
    }

    private static final Map<String, Digestage> Digests = Map.of(
        Codex.MatterCodex.Blake3_256.getValue(),
        new Digestage(
            Saider::deriveBlake3_256,
            null,
            null
        )
    );

    private static byte[] deriveBlake3_256(byte[] ser, int digestSize, int length) {
        // TODO implement blake3 logic
        return new byte[0];
    }

    public Saider(RawArgs args, Map<String, Object> sad, CoreUtil.Serials kind, String label) {
        super(RawArgs.generateSaiderRaw(args, sad, kind, label));

        if (!this.isDigestible()) {
            throw new IllegalArgumentException("Unsupported digest code = " + this.getCode());
        }
    }

    public Saider(String qb64) {
        super(qb64);
    }

    public Saider(Map<String, Object> sad, String label) {
        super(sad.get(label).toString());
    }

    public static DeriveResult derive(
        Map<String, Object> sad,
        String code,
        CoreUtil.Serials kind,
        String label
    ) {
        code = code == null ? Codex.MatterCodex.Blake3_256.getValue() : code;
        label = label == null ? Ids.d.getValue() : label;
        if (!Codex.DigiCodex.has(code) || !Digests.containsKey(code)) {
            throw new IllegalArgumentException("Unsupported digest code = " + code);
        }

        Map<String, Object> sadCopy = new HashMap<>(sad);

        Sizage size = Matter.sizes.get(code);
        if (size == null) {
            throw new IllegalArgumentException("Unknown size for code: " + code);
        }
        String dummyValue = String.join("", Collections.nCopies(size.fs, Dummy));
        sadCopy.put(label, dummyValue);

        if (sadCopy.containsKey("v")) {
            Serder.ExhaleResult sizeResult = Serder.sizeify(sadCopy, kind);
            kind = sizeResult.kind();
            sadCopy = sizeResult.kd();
        }

        Map<String, Object> ser = new HashMap<>(sadCopy);
        Digestage digestage = Digests.get(code);

        byte[] cpa = serialize(ser, kind).getBytes();
        byte[] raw = deriveBlake3_256(cpa, digestage.size, digestage.length);

        return new DeriveResult(raw, sadCopy);
    }

    private static String serialize(Map<String, Object> sad, Serials kind) {
        Serials knd = Serials.JSON;
        if (sad.containsKey("v")) {
            CoreUtil.DeversifyResult deversifyResult = CoreUtil.deversify(sad.get("v").toString());
            knd = deversifyResult.kind();
        }

        if (kind == null) {
            kind = knd;
        }

        return Serder.dumps(sad, kind);
    }

    public static SaidifyResult saidify(Map<String, Object> sad, String code, CoreUtil.Serials kind, String label) {
        if (!sad.containsKey(label)) {
            throw new IllegalArgumentException("Missing id field labeled = " + label + " in sad.");
        }

        DeriveResult deriveResult = derive(sad, code, kind, label);
        Saider saider = new Saider(
            RawArgs.builder()
                .raw(deriveResult.raw())
                .code(code)
                .build(),
            null,
            kind,
            label
        );

        Map<String, Object> updatedSad = deriveResult.sad();
        updatedSad.put(label, saider.getQb64());

        return new SaidifyResult(saider, updatedSad);
    }

    public static SaidifyResult saidify(Map<String, Object> sad) {
        return saidify(sad, Codex.MatterCodex.Blake3_256.getValue(), CoreUtil.Serials.JSON, Ids.d.value);
    }

    public record SaidifyResult(Saider saider, Map<String, Object> sad) {}

    public record DeriveResult (byte[] raw, Map<String, Object> sad) {}

    @FunctionalInterface
    public interface Deriver {
        byte[] derive(byte[] ser, int digestSize, int length);
    }

}
