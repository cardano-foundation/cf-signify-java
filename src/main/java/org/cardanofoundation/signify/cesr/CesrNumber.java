package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.Codex.NumCodex;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;


public class CesrNumber extends Matter {
    private byte[] raw;
    private String code;

    public CesrNumber(MatterArgs args, BigInteger num, String numh) {
        super(initializeArgs(args, num, numh));

        if (!NumCodex.has(this.code)) {
            throw new IllegalArgumentException("Invalid code " + code + " for Number");
        }
    }

    private static MatterArgs initializeArgs(MatterArgs args, BigInteger num, String numh) {
        BigInteger _num;
        String code;
        byte[] raw;

        if (args.getRaw() == null && args.getQb64() == null && args.getQb64b() == null && args.getQb2() == null) {
            if (num instanceof BigInteger) {
                _num = num;
            } else if (numh != null) {
                _num = new BigInteger(numh, 16);
            } else {
                _num = BigInteger.ZERO;
            }

            if (_num == null) {
                throw new IllegalArgumentException("Invalid whole number");
            }

            if (Utils.isLessThan(_num, Math.pow(256, 2) - 1)) {
                // make short version of code
                code = NumCodex.Short.getValue();
            } else if (Utils.isLessThan(_num, Math.pow(256, 4) - 1)) {
                // make long version of code
                code = NumCodex.Long.getValue();
            } else if (Utils.isLessThan(_num, Math.pow(256, 8) - 1)) {
                // make big version of code
                code = NumCodex.Big.getValue();
            } else if (Utils.isLessThan(_num, Math.pow(256, 16) - 1)) {
                // make huge version of code
                code = NumCodex.Huge.getValue();
            } else {
                throw new IllegalArgumentException("Invalid num = " + num + ", too large to encode.");
            }

            raw = Utils.intToBytes(_num, Matter.getRawSize(code));
        } else {
            raw = args.getRaw();
            code = args.getCode();
        }

        return MatterArgs.builder()
            .raw(raw)
            .code(code)
            .qb64b(args.getQb64b())
            .qb64(args.getQb64())
            .qb2(args.getQb2())
            .build();
    }

    public BigInteger getNum() {
        return Utils.bytesToInt(this.raw);
    }

    public String getNumh() {
        return getNum().toString(16);
    }

    public boolean isPositive() {
        return getNum().compareTo(BigInteger.ZERO) > 0;
    }

}
