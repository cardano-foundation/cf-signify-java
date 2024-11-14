package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

public class Encrypter extends Matter {
    private EncrypterFunction encrypter;
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    public Encrypter(RawArgs args, byte[] verkey) throws SodiumException {
        super(RawArgs.generateEncrypterRaw(args, verkey));
        setEncrypter();
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

    private Cipher _x25519(byte[] ser, byte[] pubKey, String code) throws SodiumException {
        byte[] raw = new byte[0];
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
