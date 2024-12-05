package org.cardanofoundation.signify.cesr;

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
}
