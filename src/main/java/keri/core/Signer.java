package keri.core;

import java.security.Signature;
import java.util.function.Function;

import keri.core.args.SignerArgs;


public class Signer extends Matter {
    private SignerFunction _sign;
    private Verfer _verfer;

    public Signer(SignerArgs args) {
        // TODO: Implement Signer constructor
        super(args.toMatterArgs());
    }

    @FunctionalInterface
    interface SignerFunction {
        Signature sign(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception;
    }
}
