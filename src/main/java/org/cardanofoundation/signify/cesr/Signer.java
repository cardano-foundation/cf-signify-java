package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;

import com.goterl.lazysodium.utils.HexMessageEncoder;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Codex.IndexerCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;

import java.nio.ByteBuffer;

@Getter
public class Signer extends Matter {
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
    private final SignerFunction sign;
    private final Verfer verfer;


    public Signer() throws SodiumException {
        this(RawArgs.builder()
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build(), 
            true);
    }

    public Signer(RawArgs args) throws SodiumException {
        this(args, true);
    }

    public Signer(RawArgs args, boolean transferable) throws SodiumException {
        super(RawArgs.generateEd25519SeedRaw(args));

        if (MatterCodex.Ed25519_Seed.getValue().equals(this.getCode())) {
            this.sign = this::_ed25519;
            final KeyPair keypair = lazySodium.cryptoSignSeedKeypair(this.getRaw());
            this.verfer = new Verfer(RawArgs.builder()
                    .raw(keypair.getPublicKey().getAsBytes())
                    .code(transferable ? MatterCodex.Ed25519.getValue() : MatterCodex.Ed25519N.getValue())
                    .build());
        } else {
            throw new UnsupportedOperationException("Unsupported signer code = " + this.getCode());
        }
    }

    public Object sign(byte[] ser) throws SodiumException {
        return sign.sign(ser, this.getRaw(), this.getVerfer(), null, false, null);
    }

    public Object sign(byte[] ser, Integer index, boolean only, Integer ondex) throws SodiumException{
        return sign.sign(ser, this.getRaw(), this.getVerfer(), index, only, ondex);
    }

    private Object _ed25519(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws SodiumException {
        ByteBuffer buffer = ByteBuffer.allocate(seed.length + verfer.getRaw().length);
        buffer.put(seed);
        buffer.put(verfer.getRaw());

        final String sigEncoded = lazySodium.cryptoSignDetached(new String(ser), Key.fromBytes(buffer.array()));
        final byte[] sig = new HexMessageEncoder().decode(sigEncoded);
        if (index == null) {
            return new Cigar(RawArgs.builder()
                    .raw(sig)
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
                    .raw(sig)
                    .code(code)
                    .build();

            return new Siger(rawArgs, index, ondex, verfer);
        }
    }

    @FunctionalInterface
    interface SignerFunction {
        Object sign(byte[] ser, byte[] seed, Verfer verfer, Integer index, boolean only, Integer ondex) throws SodiumException;
    }
}
