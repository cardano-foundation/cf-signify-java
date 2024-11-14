package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.utils.KeyPair;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import java.util.Arrays;

import static org.cardanofoundation.signify.cesr.util.Utils.CRYPTO_BOX_SEAL_BYTES;

public class Encrypter extends Matter {
    private EncrypterFunction encrypter;
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    public Encrypter(RawArgs args, byte[] verkey) throws SodiumException {
        super(RawArgs.generateEncrypterRaw(args, verkey));
        setEncrypter();
    }

    public Encrypter(RawArgs args) throws SodiumException {
        this(args, null);
    }

    public Encrypter(String qb64) {
        super(qb64);
        setEncrypter();
    }

    private void setEncrypter() {
        if (this.getCode().equals(Codex.MatterCodex.X25519.getValue())) {
            this.encrypter = this::_x25519;
        } else {
            throw new UnsupportedOperationException("Unsupported encrypter code = " + this.getCode());
        }
    }

    public boolean verifySeed(byte[] seed) throws SodiumException {
        Signer signer = new Signer(seed);
        KeyPair keypair = lazySodium.cryptoSignSeedKeypair(signer.getRaw());

        byte[] pubKey = new byte[32];
        boolean success = lazySodium.convertPublicKeyEd25519ToCurve25519(pubKey, keypair.getPublicKey().getAsBytes());
        if (!success) {
            throw new SodiumException("Failed to convert public key ed25519 to Curve25519");
        }

        return Arrays.equals(pubKey, this.getRaw());
    }

    public Cipher encrypt(byte[] ser, Matter matter) throws SodiumException {
        if (ser == null && matter == null) {
            throw new EmptyMaterialException("Neither ser nor matter are provided.");
        }

        if (ser != null) {
            matter = new Matter(ser);
        }

        String code;
        if (matter.getCode().equals(MatterCodex.Salt_128.getValue())) {
            code = MatterCodex.X25519_Cipher_Salt.getValue();
        } else {
            code = MatterCodex.X25519_Cipher_Seed.getValue();
        }

        return encrypter.encrypt(matter.getQb64().getBytes(), this.getRaw(), code);
    }

    public Cipher encrypt(byte[] ser) throws SodiumException {
        return encrypt(ser, null);
    }

    private Cipher _x25519(byte[] ser, byte[] pubKey, String code) throws SodiumException {
        byte[] raw = new byte[ser.length + CRYPTO_BOX_SEAL_BYTES];
        boolean success = lazySodium.cryptoBoxSeal(raw, ser, ser.length, pubKey);
        if (!success) {
            throw new SodiumException("Fail to crypto box seal");
        }
        return new Cipher(
            RawArgs.builder()
                .raw(raw)
                .code(code)
                .build()
        );
    }

    @FunctionalInterface
    private interface EncrypterFunction {
        Cipher encrypt(byte[] ser, byte[] pubKey, String key) throws SodiumException;
    }
}
