package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;

import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Codex.IndexerCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;

import java.nio.ByteBuffer;

@Getter
public class Signer extends Matter {
    private final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
    private final SignerFunction sign;
    private final Verfer verfer;

    public Signer(RawArgs args, boolean transferable) {
        super(RawArgs.generateEd25519SeedRaw(args));

        if (MatterCodex.Ed25519_Seed.getValue().equals(this.getCode())) {
            this.sign = this::_ed25519;
            try {
                final KeyPair keypair = lazySodium.cryptoSignSeedKeypair(this.getRaw());
                this.verfer = new Verfer(RawArgs.builder()
                        .raw(keypair.getPublicKey().getAsBytes())
                        .code(transferable ? MatterCodex.Ed25519.getValue() : MatterCodex.Ed25519N.getValue())
                        .build());
            } catch (SodiumException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported signer code = " + this.getCode());
        }
    }

    public Object sign(byte[] ser, Integer index, boolean only, Integer ondex) throws Exception {
        return sign.sign(ser, this.getRaw(), this.getVerfer(), index, only, ondex);
    }

    private Object _ed25519(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(seed.length + verfer.getRaw().length);
        buffer.put(seed);
        buffer.put(verfer.getRaw());

        final String sig = lazySodium.cryptoSignDetached(new String(ser), Key.fromBytes(buffer.array()));
        if (index == null) {
            return new Cigar(RawArgs.builder()
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

            RawArgs rawArgs = RawArgs.builder()
                    .raw(sig.getBytes())
                    .code(code)
                    .build();

            return new Siger(rawArgs, index, ondex, verfer);
        }
    }

    @FunctionalInterface
    interface SignerFunction {
        Object sign(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws Exception;
    }
}
