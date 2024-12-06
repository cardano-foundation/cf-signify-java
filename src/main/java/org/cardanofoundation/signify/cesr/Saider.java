package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidSizeException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;

import java.security.DigestException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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

        public Digestage(Deriver klas) {
            this(klas, null, null);
        }
    }

    private static final Map<String, Digestage> Digests = Map.of(
        Codex.MatterCodex.Blake3_256.getValue(),
        new Digestage(Saider::deriveBlake3_256)
    );

    public Saider(RawArgs rawArgs) {
        super(rawArgs);

        if (!this.isDigestible()) {
            throw new UnexpectedCodeException("Unsupported digest code = " + this.getCode());
        }
    }

    public Saider(Map<String, Object> sad) throws DigestException {
        this(new RawArgs(), sad, null, Ids.d.getValue());
    }

    public Saider(RawArgs rawArgs, Map<String, Object> sad) throws DigestException {
        this(rawArgs, sad, null, Ids.d.getValue());
    }

    public Saider(RawArgs rawArgs, Map<String, Object> sad, CoreUtil.Serials kind, String label) throws DigestException {
        super(getRawArgs(rawArgs, sad, kind, label));

        if (!this.isDigestible()) {
            throw new UnexpectedCodeException("Unsupported digest code = " + this.getCode());
        }
    }

    private static RawArgs getRawArgs(RawArgs rawArgs, Map<String, Object> sad, CoreUtil.Serials kind, String label) throws DigestException {
        if(rawArgs.getCode() == null){
            if(sad.containsKey(label) && !sad.get(label).toString().isEmpty()){
                Matter matterTemp = new Matter(sad.get(label).toString());
                rawArgs.setCode(matterTemp.getCode());
            } else {
                rawArgs.setCode(Codex.MatterCodex.Blake3_256.getValue());
            }
        }

        if(!Codex.DigiCodex.has(rawArgs.getCode())) {
            throw new UnexpectedCodeException("Unsupported digest code = " + rawArgs.getCode());
        }

        if (sad == null) {
            sad = new LinkedHashMap<>();
        }
        DeriveResult deriveResult = derive(new LinkedHashMap<>(sad), rawArgs.getCode(), kind, label);
        rawArgs.setRaw(deriveResult.raw());
        return rawArgs;
    }

    public Saider(String qb64) {
        super(qb64);

        if (!this.isDigestible()) {
            throw new UnexpectedCodeException("Unsupported digest code = " + this.getCode());
        }
    }

    private static byte[] deriveBlake3_256(byte[] ser, int digestSize, int length) throws DigestException {
        return CoreUtil.blake3_256(ser, 32);
    }

    public static DeriveResult derive(
        Map<String, Object> sad,
        String code,
        CoreUtil.Serials kind,
        String label
    ) throws DigestException {
        sad = new LinkedHashMap<>(sad);
        if (!Codex.DigiCodex.has(code) || !Digests.containsKey(code)) {
            throw new UnexpectedCodeException("Unsupported digest code = " + code);
        }

        Sizage size = Matter.sizes.get(code);
        if (size == null) {
            throw new InvalidSizeException("Unknown size for code: " + code);
        }
        String dummyValue = String.join("", Collections.nCopies(size.fs, Dummy));
        sad.put(label, dummyValue);

        if (sad.containsKey("v")) {
            Serder.ExhaleResult sizeResult = Serder.sizeify(sad, kind);
            kind = sizeResult.kind();
            sad = sizeResult.kd();
        }

        Map<String, Object> ser = new LinkedHashMap<>(sad);

        Digestage digestage = Digests.get(code);

        String cpa = serialize(ser, kind);
        byte[] raw = digestage.klas.derive(cpa.getBytes(), digestage.size, digestage.length);

        return new DeriveResult(raw, sad);
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

    public static SaidifyResult saidify(Map<String, Object> sad, String code, CoreUtil.Serials kind, String label) throws DigestException {
        if (!sad.containsKey(label)) {
            throw new NoSuchElementException("Missing id field labeled = " + label + " in sad.");
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

    public boolean verify(Map<String, Object> sad) {
        return verify(sad, false, false, null, Ids.d.getValue());
    }

    public boolean verify(Map<String, Object> sad, boolean prefixed, boolean versioned, CoreUtil.Serials kind, String label) {
        try {
            DeriveResult deriveResult = derive(sad, this.getCode(), kind, label);
            Map<String, Object> dsad = deriveResult.sad();
            Saider saider = new Saider(RawArgs.builder()
                .raw(deriveResult.raw())
                .code(this.getCode())
                .build()
            );
    
            if(!this.getQb64().equals(saider.getQb64())) {
                return false;
            }
    
            if(versioned && sad.containsKey("v") ) {
                if(!sad.get("v").toString().equals(dsad.get("v").toString())) {
                    return false;
                }
            }
    
            if(prefixed && sad.containsKey(label) && !sad.get(label).toString().equals(this.getQb64())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static SaidifyResult saidify(Map<String, Object> sad) throws DigestException {
        return saidify(sad, Codex.MatterCodex.Blake3_256.getValue(), CoreUtil.Serials.JSON, Ids.d.value);
    }

    public record SaidifyResult(Saider saider, Map<String, Object> sad) {}

    public record DeriveResult (byte[] raw, Map<String, Object> sad) {}

    @FunctionalInterface
    public interface Deriver {
        byte[] derive(byte[] ser, int digestSize, int length) throws DigestException;
    }

}
