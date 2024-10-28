package keri.core;

import keri.core.Codex.MatterCodex;
import keri.core.args.MatterArgs;

public class Verfer extends Matter {
    private final Verifier verifier;

    public Verfer(MatterArgs args) {
        super(args);

        MatterCodex codex = MatterCodex.fromValue(this.getCode());
        switch (codex) {
            case Ed25519N:
            case Ed25519:
                this.verifier = this::_ed25519;
                break;
            default:
                throw new RuntimeException("Unsupported code = " + this.getCode() + " for verifier.");
        }
    }

    public boolean verify(byte[] sig, byte[] ser) throws Exception {
        return this.verifier.verify(sig, ser, this.getRaw());
    }

    private boolean _ed25519(byte[] sig, byte[] ser, byte[] key) throws Exception {
        // TODO: Implement ed25519 verification
        // should use lazysodium
        return false;
    }

    @FunctionalInterface
    private interface Verifier {
        boolean verify(byte[] sig, byte[] ser, byte[] key) throws Exception;
    }
}