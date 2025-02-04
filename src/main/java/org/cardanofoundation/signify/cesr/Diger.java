package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;

import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.util.Arrays;

/**
 * Diger is subset of Matter and is used to verify the digest of serialization
 * It uses .raw: as digest
 * .code as digest algorithm
 */
@Getter
public class Diger extends Matter {
    private Verify verify;

    public Diger(RawArgs args, byte[] ser) throws DigestException {
        super(RawArgs.generateBlake3256SeedRaw(args, ser));

        if (this.getCode().equals(MatterCodex.Blake3_256.getValue())) {
            this.verify = this::blake3_256;
        } else {
            throw new UnexpectedCodeException("Unsupported code = " + this.getCode() + " for digester.");
        }
    }

    public Diger(String code, byte[] ser) throws DigestException {
        this(RawArgs.builder().code(code).build(), ser);
    }

    public Diger(RawArgs args) throws DigestException {
        this(args, null);
    }

    public Diger(String qb64) {
        super(qb64);
    }

    /**
     *
     * @param ser  serialization bytes
     * @return This method will return true if digest of bytes serialization ser matches .raw
     * using .raw as reference digest for ._verify digest algorithm determined
    by .code
     */
    public boolean verify(byte[] ser) throws DigestException {
        return verify.verify(ser, this.getRaw());
    }

    public boolean compare(byte[] ser, byte[] dig, Diger diger) throws DigestException {
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
            throw new EmptyMaterialException("Both dig and diger may not be null.");
        }

        if (diger.getCode().equals(this.getCode())) {
            return false;
        }

        return diger.verify(ser) && this.verify(ser);
    }

    private boolean blake3_256(byte[] ser, byte[] dig) throws DigestException {
        byte[] digest = CoreUtil.blake3_256(ser, 32);
        return Arrays.toString(digest).equals(Arrays.toString(dig));
    }

    @FunctionalInterface
    public interface Verify {
        boolean verify(byte[] ser, byte[] raw) throws DigestException;
    }
}
