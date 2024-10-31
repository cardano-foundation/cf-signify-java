package keri.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import keri.cesr.Codex.MatterCodex;
import keri.cesr.args.MatterArgs;

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

    private boolean _ed25519(byte[] sig, byte[] ser, byte[] key) {
        return lazySodium.cryptoSignVerifyDetached(sig, ser, ser.length, key);
    }

    @FunctionalInterface
    private interface Verifier {
        boolean verify(byte[] sig, byte[] ser, byte[] key) throws Exception;
    }
}