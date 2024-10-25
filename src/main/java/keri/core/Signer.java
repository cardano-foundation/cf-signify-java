package keri.core;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.Sign;
import com.goterl.lazysodium.utils.KeyPair;
import keri.core.args.MatterArgs;
import keri.core.args.SignerArgs;
import keri.core.exceptions.EmptyMaterialError;

import java.security.SecureRandom;
import java.util.Arrays;

public class Signer extends Matter {
    private final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
    private final SignerFunction _sign;
    private final Verfer _verfer;
    private final Codex.MatterCodex mtrDex = new Codex.MatterCodex();

    public Signer(SignerArgs args) {
        super(args.toMatterArgs());
        
        try {
            if (args.getRaw() == null && args.getCode() == null && args.getQb64() == null && args.getQb64b() == null && args.getQb2() == null) {
                throw new EmptyMaterialError("Empty material, need raw, qb64, qb64b, or qb2.");
            }
        } catch (EmptyMaterialError e) {
            if (mtrDex.Ed25519_Seed.equals(args.getCode())) {
                byte[] raw = new byte[Sign.SEEDBYTES];
                new SecureRandom().nextBytes(raw);
                args.setRaw(raw);
                args.toMatterArgs().setRaw(raw);
            } else {
                throw new IllegalArgumentException("Unsupported signer code = " + args.getCode());
            }
        }

        if (mtrDex.Ed25519_Seed.equals(this.getCode())) {
            this._sign = this::_ed25519;
            Sign.Lazy signLazy = (Sign.Lazy) lazySodium;
            KeyPair keyPair;
            try {keyPair = signLazy.cryptoSignSeedKeypair(this.getRaw());}
            catch (SodiumException e) {
                throw new RuntimeException(e);
            }

            try {
            _verfer = new Verfer(
                new MatterArgs(
                    keyPair.getPublicKey().getAsBytes(),
                    args.getTransferable() ? mtrDex.Ed25519 : mtrDex.Ed25519N));}
            catch (Exception ignored) {}

        } else {
            throw new IllegalArgumentException("Unsupported signer code = " + this.getCode());
        }
    }

    public Verfer getVerfer() {
        return _verfer;
    }

    public Object sign(byte[] ser, Integer index, boolean only, Integer ondex) throws Exception {
        return _sign.sign(ser, this.getRaw(), this._verfer, index, only, ondex);
    }

    private Object _ed25519(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception {
        Sign.Lazy signLazy = (Sign.Lazy) lazySodium;
        byte[] seedAndPub = Arrays.copyOf(seed, seed.length + verfer.getRaw().length);
        System.arraycopy(verfer.getRaw(), 0, seedAndPub, seed.length, verfer.getRaw().length);
        
        byte[] sig = new byte[Sign.BYTES];
        if (!signLazy.cryptoSignDetached(sig, ser, ser.length, seedAndPub)) {
            throw new Exception("Signing failed");
        }

        if (index == null) {
            return new Cigar(new MatterArgs().setRaw(sig).setCode(Codex.MatterCodex.Ed25519_Sig), verfer);
        } else {
            String code;
            if (only) {
                ondex = null;
                code = (index <= 63) ? Codex.IndexerCodex.Ed25519_Crt_Sig : Codex.IndexerCodex.Ed25519_Big_Crt_Sig;
            } else {
                if (ondex == null) {
                    ondex = index;
                }
                code = (ondex.equals(index) && index <= 63) ? Codex.IndexerCodex.Ed25519_Sig : Codex.IndexerCodex.Ed25519_Big_Sig;
            }

            return new Siger(new SigerArgs()
                .setRaw(sig)
                .setCode(code)
                .setIndex(index)
                .setOndex(ondex), verfer);
        }
    }

    @FunctionalInterface
    interface SignerFunction {
        Object sign(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception;
    }
}
