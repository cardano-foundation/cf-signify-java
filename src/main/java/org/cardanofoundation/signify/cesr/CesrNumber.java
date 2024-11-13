package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.NumCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;

@Getter
public class CesrNumber extends Matter {

    public CesrNumber(BigInteger num) {
        this(new RawArgs(), num, null);
    }

    public CesrNumber(String numh) {
        this(new RawArgs(), null, numh);
    }

    public CesrNumber(RawArgs args, BigInteger num, String numh) {
        super(RawArgs.generateNumDexRaw(args, num, numh));

        if (!NumCodex.has(this.getCode())) {
            throw new IllegalArgumentException("Invalid code " + this.getCode() + " for Number");
        }
    }

    public BigInteger getNum() {
        return Utils.bytesToInt(this.getRaw());
    }

    public String getNumh() {
        return getNum().toString(16);
    }

    public boolean isPositive() {
        return getNum().compareTo(BigInteger.ZERO) > 0;
    }

}
