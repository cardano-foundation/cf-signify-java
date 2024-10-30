package keri.core;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.interfaces.Sign;

import keri.core.Codex.MatterCodex;
import keri.core.Codex.IndexerCodex;
import keri.core.args.IndexerArgs;
import keri.core.args.MatterArgs;
import keri.core.args.SignerArgs;
import keri.core.exceptions.EmptyMaterialError;

import java.security.SecureRandom;
import java.util.Arrays;

public class Signer extends Matter {
    private static final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
    private final SignerFunction _sign;
    private final Verfer _verfer;

    public Signer(SignerArgs args) {
        super(initializeArgs(args));

        if (MatterCodex.Ed25519_Seed.getValue().equals(this.getCode())) {
            this._sign = this::_ed25519;
            byte[] publicKey = new byte[Sign.PUBLICKEYBYTES];
            byte[] secretKey = new byte[Sign.SECRETKEYBYTES];
            lazySodium.cryptoSignSeedKeypair(publicKey, secretKey, this.getRaw());

            _verfer = new Verfer(MatterArgs.builder()
                .raw(publicKey)
                .code(args.getTransferable() ? MatterCodex.Ed25519.getValue() : MatterCodex.Ed25519N.getValue())
                .build());
        } else {
            throw new IllegalArgumentException("Unsupported signer code = " + this.getCode());
        }
    }

    private static MatterArgs initializeArgs(SignerArgs args) {
        try {
            if (args.getRaw() == null && args.getCode() == null && args.getQb64() == null && args.getQb64b() == null && args.getQb2() == null) {
                throw new EmptyMaterialError("Empty material");
            }
        } catch (EmptyMaterialError e) {
            if (MatterCodex.Ed25519_Seed.getValue().equals(args.getCode())) {
                byte[] raw = new byte[Sign.SEEDBYTES];
                new SecureRandom().nextBytes(raw);
//                byte[] raw = lazySodium.randomBytesBuf(Sign.SEEDBYTES);
                args.setRaw(raw);
            } else {
                throw new IllegalArgumentException("Unsupported signer code = " + args.getCode());
            }
        }
        return args.toMatterArgs();
    }

    public Verfer getVerfer() {
        return _verfer;
    }

    public Object sign(byte[] ser, Integer index, boolean only, Integer ondex) throws Exception {
        return _sign.sign(ser, this.getRaw(), this.getVerfer(), index, only, ondex);
    }

    private Object _ed25519(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception {
        byte[] seedAndPub = Arrays.copyOf(seed, seed.length + verfer.getRaw().length);
        System.arraycopy(verfer.getRaw(), 0, seedAndPub, seed.length, verfer.getRaw().length);
        
        byte[] sig = new byte[Sign.BYTES];
        if (!lazySodium.cryptoSignDetached(sig, ser, ser.length, seedAndPub)) {
            throw new IllegalArgumentException();
        }
        if (index == null) {
            return new Cigar(MatterArgs.builder()
                .raw(sig)
                .code(MatterCodex.Ed25519_Sig.getValue())
                .build(), verfer);
        } else {
            String code;
            if (only) {
                ondex = null;
                code = (index <= 63) ? 
                    IndexerCodex.Ed25519_Crt_Sig.getValue() :
                    IndexerCodex.Ed25519_Big_Crt_Sig.getValue();
            } else {
                if (ondex == null) {
                    ondex = index;
                }
                code = (ondex.equals(index) && index <= 63) ?
                    IndexerCodex.Ed25519_Sig.getValue() :
                    IndexerCodex.Ed25519_Big_Sig.getValue();
            }

            return new Siger(
                new IndexerArgs(sig, code, index, ondex, null, null, null),
                verfer
            );
        }
    }

    @FunctionalInterface
    interface SignerFunction {
        Object sign(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception;
    }
}
