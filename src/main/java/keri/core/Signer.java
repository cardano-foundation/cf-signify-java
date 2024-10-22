package keri.core;

import java.security.Signature;
import java.util.function.Function;

import keri.core.Matter.MatterArgs;

public class Signer extends Matter {
    private SignerFunction _sign;
    private Verfer _verfer;



    public Signer(SignerArgs args) {
        // TODO: Implement Signer constructor
        super(args.toMatterArgs());
    }
    

    

    public class SignerArgs {
        byte[] raw;
        String code;
        byte[] qb64b;
        String qb64;
        byte[] qb2;
        Boolean transferable;

        public MatterArgs toMatterArgs() {
            return new MatterArgs(raw, code, qb64b, qb64, qb2);
        }

        public SignerArgs() {
            this.transferable = true;
            this.code = mtrDex.Ed25519_Seed;
        }
    }
    

    @FunctionalInterface
    interface SignerFunction {
        Signature sign(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception;
    }
}
