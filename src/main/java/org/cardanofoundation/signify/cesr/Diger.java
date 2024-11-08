package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Diger is subset of Matter and is used to verify the digest of serialization
 * It uses .raw: as digest
 * .code as digest algorithm
 */
@Getter
public class Diger extends Matter {
    private Verify verify;

    public Diger(RawArgs args, byte[] ser) {
        super(RawArgs.generateBlake3256SeedRaw(args, ser));

        if (this.getCode().equals(MatterCodex.Blake3_256.getValue())) {
            this.verify = this::blake3_256;
        } else {
            throw new UnsupportedOperationException("Unsupported code = " + this.getCode() + " for digester.");
        }
    }

    public Diger(String qb64) {
        super(qb64);
    }

    /**
     *
     * @param ser  serialization bytes
     * @description  This method will return true if digest of bytes serialization ser matches .raw
     * using .raw as reference digest for ._verify digest algorithm determined
    by .code
     */
    public boolean verify(byte[] ser) {
        return verify.verify(ser, this.getRaw());
    }

    public boolean compare(byte[] ser, byte[] dig, Diger diger) {
        if (dig != null) {
            if (Arrays.equals(dig, this.getQb64b())) {
                return true;
            }
            diger = new Diger(new String(dig, StandardCharsets.UTF_8));
        } else if (diger != null) {
            if (Arrays.equals(diger.getQb64b(), this.getQb64b())) {
                return true;
            }
        } else {
            throw new IllegalArgumentException("Both dig and diger may not be null.");
        }

        if (diger.getCode().equals(this.getCode())) {
            return false;
        }

        return diger.verify(ser) && this.verify(ser);
    }

    private boolean blake3_256(byte[] ser, byte[] dig) {
        //TODO Implement Blake3
        byte[] digest = new byte[0];
        return Arrays.toString(digest).equals(Arrays.toString(dig));
    }

    @FunctionalInterface
    public interface Verify {
        boolean verify(byte[] ser, byte[] raw);
    }
}
