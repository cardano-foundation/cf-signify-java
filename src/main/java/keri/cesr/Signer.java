package keri.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;

import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import keri.cesr.Codex.MatterCodex;
import keri.cesr.Codex.IndexerCodex;
import keri.cesr.args.IndexerArgs;
import keri.cesr.args.MatterArgs;
import keri.cesr.args.SignerArgs;

import java.nio.ByteBuffer;

public class Signer extends Matter {
    private static final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
    private final SignerFunction _sign;
    private final Verfer _verfer;

    public Signer(SignerArgs args) {
        super(initializeArgs(args));

        if (MatterCodex.Ed25519_Seed.getValue().equals(this.getCode())) {
            this._sign = this::_ed25519;
            try {
                final KeyPair keypair = lazySodium.cryptoSignSeedKeypair(this.getRaw());
                this._verfer = new Verfer(MatterArgs.builder()
                        .raw(keypair.getPublicKey().getAsBytes())
                        .code(args.getTransferable() ? MatterCodex.Ed25519.getValue() : MatterCodex.Ed25519N.getValue())
                        .build());
            } catch (SodiumException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported signer code = " + this.getCode());
        }
    }

    private static MatterArgs initializeArgs(SignerArgs args) {
        if (args.getRaw() == null && args.getQb64() == null
                && args.getQb64b() == null && args.getQb2() == null) {
            if (MatterCodex.Ed25519_Seed.getValue().equals(args.getCode())) {
                final byte[] salt = lazySodium.randomBytesBuf(32); // crypto_sign_SEEDBYTES
                return MatterArgs.builder()
                        .raw(salt)
                        .code(args.getCode())
                        .build();
            } else {
                throw new IllegalArgumentException("Unsupported signer code = " + args.getCode());
            }
        }

        return MatterArgs.builder()
                .raw(args.getRaw())
                .code(args.getCode())
                .qb64b(args.getQb64b())
                .qb64(args.getQb64())
                .qb2(args.getQb2())
                .build();
    }

    public Verfer getVerfer() {
        return _verfer;
    }

    public Object sign(byte[] ser, Integer index, boolean only, Integer ondex) throws Exception {
        return _sign.sign(ser, this.getRaw(), this.getVerfer(), index, only, ondex);
    }

    private Object _ed25519(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(seed.length + verfer.getRaw().length);
        buffer.put(seed);
        buffer.put(verfer.getRaw());

        final String sig = lazySodium.cryptoSignDetached(new String(ser), Key.fromBytes(buffer.array()));
        if (index == null) {
            return new Cigar(MatterArgs.builder()
                    .raw(sig.getBytes())
                    .code(MatterCodex.Ed25519_Sig.getValue())
                    .build(),
                    verfer);
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
                    IndexerArgs.builder()
                            .raw(sig.getBytes())
                            .code(code)
                            .index(index)
                            .ondex(ondex)
                            .build(),
                    verfer
            );
        }
    }

    @FunctionalInterface
    interface SignerFunction {
        Object sign(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception;
    }
}
