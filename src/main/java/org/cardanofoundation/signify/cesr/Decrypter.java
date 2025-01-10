package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.utils.Key;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;

import static org.cardanofoundation.signify.cesr.util.Utils.CRYPTO_BOX_SEAL_BYTES;

public class Decrypter extends Matter {
    private DecrypterFunction decrypter;
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    public Decrypter(RawArgs args, byte[] seed) throws SodiumException {
        super(RawArgs.generateDecrypterRaw(args, seed));
        setDecrypter();
    }

    public Decrypter(RawArgs args) throws SodiumException {
        this(args, null);
    }

    private void setDecrypter() {
        if (this.getCode().equals(Codex.MatterCodex.X25519_Private.getValue())) {
            this.decrypter = this::_x25519;
        } else {
            throw new UnexpectedCodeException("Unsupported decrypter code = " + this.getCode());
        }
    }

    public Object decrypt(byte[] ser, Cipher cipher, Boolean transferable) throws SodiumException {
        if (ser == null && cipher == null) {
            throw new EmptyMaterialException("Neither ser nor matter are provided.");
        }

        if (ser != null) {
            cipher = new Cipher(ser);
        }

        return decrypter.decrypt(cipher, this.getRaw(), transferable != null && transferable);
    }

    public Object decrypt(byte[] ser, Cipher cipher) throws SodiumException {
        return decrypt(ser, cipher, false);
    }

    private Object _x25519(Cipher cipher, byte[] priKey, Boolean transferable) throws SodiumException {
        Key pubKey = lazySodium.cryptoScalarMultBase(Key.fromBytes(priKey));
        byte[] plain = new byte[cipher.getRaw().length - CRYPTO_BOX_SEAL_BYTES];
        boolean success = lazySodium.cryptoBoxSealOpen(
            plain,
            cipher.getRaw(),
            cipher.getRaw().length,
            pubKey.getAsBytes(),
            priKey
        );
        if (!success) {
            throw new SodiumException("Decryption failed");
        }
        if (cipher.getCode().equals(Codex.MatterCodex.X25519_Cipher_Salt.getValue())) {
            return new Salter(plain);
        } else if (cipher.getCode().equals(Codex.MatterCodex.X25519_Cipher_Seed.getValue())) {
            return new Signer(plain, transferable != null && transferable);
        } else {
            throw new UnexpectedCodeException("Unsupported cipher text code = " + cipher.getCode());
        }
    }

    @FunctionalInterface
    private interface DecrypterFunction {
        Object decrypt(Cipher cipher, byte[] priKey, Boolean transferable) throws SodiumException;
    }
}
