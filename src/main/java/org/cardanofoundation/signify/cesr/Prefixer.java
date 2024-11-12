package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.util.*;

public class Prefixer extends Matter{
    private static final String Dummy = "#";
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
            throw new IllegalArgumentException("Non-incepting ilk " + ked.get("i") + "for prefix derivation");
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

    public static DeriveResult _deriveEd25519N(Map<String, Object> ked) {
        List<String> keys = Utils.toList(ked.get("k"));
        if (keys == null || keys.size() != 1) {
            throw new IllegalArgumentException(
                "Basic derivation needs exactly 1 key got " +
                    (keys == null ? 0 : keys.size()) + " keys instead"
            );
        }

        Verfer verfer;
        try {
            verfer = new Verfer(keys.getFirst());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error extracting public key: " + e.getMessage(), e);
        }

        if (!MatterCodex.Ed25519N.getValue().equals(verfer.getCode())) {
            throw new IllegalArgumentException(
                "Mismatch derivation code = " + verfer.getCode()
            );
        }

        List<String> next = ked.containsKey("n") ? Utils.toList(ked.get("n")) : new ArrayList<>();
        if (!next.isEmpty()) {
            throw new IllegalArgumentException(
                "Non-empty nxt = " + next + " for non-transferable code = " + verfer.getCode()
            );
        }

        List<String> backers = ked.containsKey("b") ? Utils.toList(ked.get("b")) : new ArrayList<>();
        if (!backers.isEmpty()) {
            throw new IllegalArgumentException(
                "Non-empty b = " + backers + " for non-transferable code = " + verfer.getCode()
            );
        }

        List<String> anchor = ked.containsKey("a") ? Utils.toList(ked.get("a")) : new ArrayList<>();
        if (!anchor.isEmpty()) {
            throw new IllegalArgumentException(
                "Non-empty a = " + anchor + " for non-transferable code = " + verfer.getCode()
            );
        }

        return new DeriveResult(verfer.getRaw(), verfer.getCode());
    }

    public static DeriveResult _deriveEd25519(Map<String, Object> ked) {
        List<String> keys = Utils.toList(ked.get("k"));
        if (keys == null || keys.size() != 1) {
            throw new IllegalArgumentException(
                "Basic derivation needs exactly 1 key got " +
                    (keys == null ? 0 : keys.size()) + " keys instead"
            );
        }

        Verfer verfer;
        try {
            verfer = new Verfer(keys.getFirst());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error extracting public key: " + e.getMessage(), e);
        }

        if (MatterCodex.Ed25519.getValue().equals(verfer.getCode())) {
            throw new IllegalArgumentException(
                "Mismatch derivation code = " + verfer.getCode()
            );
        }

        return new DeriveResult(verfer.getRaw(), verfer.getCode());
    }

    public static DeriveResult _deriveBlake3_256(Map<String, Object> ked) {
        Map<String, Object> kd = new LinkedHashMap<>(ked);

        String ilk = (String) ked.get("t");
        List<String> validIlks = Arrays.asList(
            CoreUtil.Ilks.ICP.getValue(),
            CoreUtil.Ilks.DIP.getValue(),
            CoreUtil.Ilks.VCP.getValue()
        );

        if (!validIlks.contains(ilk)) {
            throw new IllegalArgumentException("Invalid ilk = " + ilk + " to derive pre.");
        }

        Sizage size = Matter.sizes.get(MatterCodex.Blake3_256.getValue());
        if (size == null || size.fs == null) {
            throw new IllegalArgumentException(
                "Invalid size configuration for " + MatterCodex.Blake3_256.getValue()
            );
        }

        String dummyValue = String.join("", Collections.nCopies(size.fs, Dummy));
        kd.put("i", dummyValue);
        kd.put("d", ked.get("i"));

        String raw = Serder.sizeify(kd, null).raw();

        //TODO implement blake3
        byte[] dig = new byte[0];
        return new DeriveResult(dig, MatterCodex.Blake3_256.getValue());
    }

    private boolean _verifyEd25519N(Map<String, Object> ked, String pre, Boolean prefixed) {
        prefixed = prefixed != null && prefixed;
        try {
            List<String> keys = Utils.toList(ked.get("k"));
            if (keys == null || keys.size() != 1) {
                return false;
            }

            if (!keys.getFirst().equals(pre)) {
                return false;
            }

            if (prefixed && !pre.equals(ked.get("i"))) {
                return false;
            }

            List<String> next = ked.containsKey("n") ? Utils.toList(ked.get("n")) : new ArrayList<>();
            if (!next.isEmpty()) {
                // must be empty
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean _verifyEd25519(Map<String, Object> ked, String pre, Boolean prefixed) {
        prefixed = prefixed != null && prefixed;
        try {
            List<String> keys = Utils.toList(ked.get("k"));
            if (keys == null || keys.size() != 1) {
                return false;
            }

            if (!keys.getFirst().equals(pre)) {
                return false;
            }

            if (prefixed && !pre.equals(ked.get("i"))) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean _verifyBlake3_256(Map<String, Object> ked, String pre, Boolean prefixed) {
        prefixed = prefixed != null && prefixed;
        try {
            DeriveResult deriveResult = _deriveBlake3_256(ked);
            Matter crymat = new Matter(
                RawArgs.builder()
                    .raw(deriveResult.raw)
                    .code(MatterCodex.Blake3_256.getValue())
                    .build()
            );

            if (!crymat.getQb64().equals(pre)) {
                return false;
            }

            if (prefixed && !pre.equals(ked.get("i"))) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @FunctionalInterface
    private interface Verify {
        boolean verify(Map<String, Object> ked, String pre, boolean prefixed);
    }

    public record DeriveResult (byte[] raw, String code) {}
}
