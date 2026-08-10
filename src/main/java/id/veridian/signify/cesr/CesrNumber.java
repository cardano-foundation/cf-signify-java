package id.veridian.signify.cesr;

import lombok.Getter;
import id.veridian.signify.cesr.Codex.NumCodex;
import id.veridian.signify.cesr.args.RawArgs;
import id.veridian.signify.cesr.exception.InvalidCodeException;
import id.veridian.signify.cesr.util.Utils;

import java.math.BigInteger;

@Getter
public class CesrNumber extends Matter {

    public CesrNumber(BigInteger num) {
        this(new RawArgs(), num, null);
    }

    public CesrNumber(String numh) {
        this(new RawArgs(), null, numh);
    }

    public CesrNumber(RawArgs args) {
        this(args, null, null);
    }

    public CesrNumber(RawArgs args, BigInteger num, String numh) {
        super(RawArgs.generateNumDexRaw(args, num, numh));

        if (!NumCodex.has(this.getCode())) {
            throw new InvalidCodeException("Invalid code " + this.getCode() + " for Number");
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
