package keri.core;
import java.util.Arrays;

public class Verfer extends Matter {
    private final Verifier verifier;

    public Verfer(MatterArgs args) throws Exception {
        super(args);

        if (Arrays.asList(mtrDex.Ed25519N, mtrDex.Ed25519).contains(this.getCode())) {
            this.verifier = this::_ed25519;
        } else if (Arrays.asList(mtrDex.ECDSA_256r1N, mtrDex.ECDSA_256r1).contains(this.getCode())) {
            this.verifier = this::_secp256r1;
        } else {
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

    private boolean _secp256r1(byte[] sig, byte[] ser, byte[] key) throws Exception {
        // TODO: Implement secp256r1 verification
        return false;
    }

    @FunctionalInterface
    private interface Verifier {
        boolean verify(byte[] sig, byte[] ser, byte[] key) throws Exception;
    }
}