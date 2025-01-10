package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;

public class Verfer extends Matter {
    private Verifier verifier;
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    public Verfer(String qb64) {
        super(qb64);
        setVerifier();
    }

    public Verfer(RawArgs args) {
        super(args);
        setVerifier();
    }

    public Verfer(byte[] qb64b) {
        super(qb64b);
        setVerifier();
    }

    private void setVerifier() {
        MatterCodex codex = MatterCodex.fromValue(this.getCode());
        switch (codex) {
            case Ed25519N:
            case Ed25519:
                this.verifier = this::_ed25519;
                break;
            default:
                throw new UnexpectedCodeException("Unsupported code = " + this.getCode() + " for verifier.");
        }
    }

    private boolean _ed25519(byte[] sig, byte[] ser, byte[] key) {
        return lazySodium.cryptoSignVerifyDetached(sig, ser, ser.length, key);
    }

    public boolean verify(byte[] sig, byte[] ser) {
        return verifier.verify(sig, ser, this.getRaw());
    }

    @FunctionalInterface
    private interface Verifier {
        boolean verify(byte[] sig, byte[] ser, byte[] key);
    }
}