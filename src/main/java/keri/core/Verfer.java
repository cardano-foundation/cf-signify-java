package keri.core;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import keri.core.Codex.MatterCodex;
import keri.core.args.MatterArgs;

public class Verfer extends Matter {
    private final Verifier verifier;
    private final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

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
        try {
            return lazySodium.cryptoSignVerifyDetached(sig, ser, ser.length, key);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @FunctionalInterface
    private interface Verifier {
        boolean verify(byte[] sig, byte[] ser, byte[] key) throws Exception;
    }
}