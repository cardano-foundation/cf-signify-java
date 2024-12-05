package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeException;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;

public class Seqner extends Matter {

    public Seqner(RawArgs args, BigInteger sn, String snh) {
        super(RawArgs.generateSeqnerRaw(args, sn, snh));

        if (!MatterCodex.Salt_128.getValue().equals(this.getCode())) {
            throw new InvalidCodeException("Invalid code =  " + this.getCode() + "for Seqner.");
        }
    }

    public Seqner(BigInteger sn) {
        this(new RawArgs(), sn, null);
    }

    public BigInteger getSn() {
        return Utils.bytesToInt(this.getRaw());
    }

    public String getSnh() {
        return this.getSn().toString(16);
    }
}
