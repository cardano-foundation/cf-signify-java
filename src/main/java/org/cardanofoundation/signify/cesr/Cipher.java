package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.args.RawArgs;

public class Cipher extends Matter {
    public Cipher(RawArgs args) {
        super(RawArgs.generateCipherRaw(args));
    }

    public Cipher(String qb64) {
        super(qb64);
    }

    public Cipher(byte[] qb64b) {
        super(qb64b);
    }

    public Object decrypt(byte[] prikey, byte[] seed) throws SodiumException {
        Decrypter decrypter;
        if(prikey != null) {
            decrypter =  new Decrypter(new String(prikey));
        } else {
            decrypter = new Decrypter(new RawArgs(), seed);
        }
        return decrypter.decrypt(this.getQb64b(), null);
    }
}
