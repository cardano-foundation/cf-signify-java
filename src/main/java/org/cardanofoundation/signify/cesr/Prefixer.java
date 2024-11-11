package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import java.util.Map;

public class Prefixer extends Matter{
    private final String Dummy = "#";
    private Verify _verify;

    public Prefixer(RawArgs args, Map<String, Object> ked) {
        super(RawArgs.generatePrefixerRaw(args, ked));

        if (MatterCodex.Ed25519N.getValue().equals(this.getCode())) {
            this._verify = this::_verifyEd25519N;
        } else if (MatterCodex.Ed25519.getValue().equals(this.getCode())) {
            this._verify = this::_verifyEd25519;
        } else if (MatterCodex.Blake3_256.getValue().equals(this.getCode())) {
            this._verify = this::_verifyBlake3_256;
        } else {
            throw new IllegalArgumentException("Unsupported code = " + this.getCode() + " for prefixer.");
        }
    }

    public Prefixer(String qb64) {
        super(qb64);
    }

    public Prefixer(Map<String, Object> ked) {
        super(ked.get("i").toString());
    }

    public static DeriveResult derive(Map<String, Object> ked, String code) {
        if (ked.get("i") != CoreUtil.Ilks.ICP.getValue()) {
            throw new IllegalArgumentException("Non-incepting ilk " + ked.get("i") + " for prefix derivation");
        }
        if (MatterCodex.Ed25519N.getValue().equals(code)) {
            return _deriveEd25519N(ked);
        } else if (MatterCodex.Ed25519.getValue().equals(code)) {
            return _deriveEd25519(ked);
        } else if (MatterCodex.Blake3_256.getValue().equals(code)) {
            return _deriveBlake3_256(ked);
        } else {
            throw new IllegalArgumentException("Unsupported code = " + code + " for prefixer.");
        }
    }

    public boolean verify(Map<String, Object> ked, Boolean prefixed) {
        if (ked.get("i") != CoreUtil.Ilks.ICP.getValue()) {
            throw new IllegalArgumentException("Non-incepting ilk " + ked.get("i") + " for prefix derivation");
        }
        prefixed = prefixed != null && prefixed;
        return _verify.verify(ked, this.getQb64(), prefixed);
    }

    // TODO implement derive and verify logic
    public static DeriveResult _deriveEd25519N(Map<String, Object> ked) {
        return new DeriveResult(null, null);
    }

    public static DeriveResult _deriveEd25519(Map<String, Object> ked) {
        return new DeriveResult(null, null);
    }

    public static DeriveResult _deriveBlake3_256(Map<String, Object> ked) {
        return new DeriveResult(null, null);
    }

    private boolean _verifyEd25519N(Map<String, Object> ked, String pre, boolean prefixed) {
        return true;
    }

    private boolean _verifyEd25519(Map<String, Object> ked, String pre, boolean prefixed) {
        return true;
    }

    private boolean _verifyBlake3_256(Map<String, Object> ked, String pre, boolean prefixed) {
        return true;
    }

    @FunctionalInterface
    private interface Verify {
        boolean verify(Map<String, Object> ked, String pre, boolean prefixed);
    }

    public record DeriveResult (byte[] raw, String code) {}
}
