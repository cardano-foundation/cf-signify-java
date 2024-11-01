package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Diger is subset of Matter and is used to verify the digest of serialization
 * It uses  .raw : as digest
 * .code as digest algorithm
 */
public class Diger extends Matter {
    private final BiFunction<byte[], byte[], Boolean> _verify;

    public Diger(MatterArgs args, byte[] ser) {
        super(initializeArgs(args, ser));

        if (this.getCode().equals(MatterCodex.Blake3_256.getValue())) {
            this._verify = this::blake3_256;
        } else {
            throw new IllegalArgumentException("Unsupported code = " + this.getCode() + " for digester.");
        }
    }

    private static MatterArgs initializeArgs(MatterArgs digerArgs, byte[] ser) {
        if (digerArgs.getRaw() == null && digerArgs.getQb64() == null
                && digerArgs.getQb64b() == null && digerArgs.getQb2() == null) {
            if (digerArgs.getCode() == null) {
                digerArgs.setCode(MatterCodex.Blake3_256.getValue());
            }

            if (digerArgs.getCode().equals(MatterCodex.Blake3_256.getValue())) {
                //TODO Implement Blake3
//                const dig = Buffer.from(
//                    blake3.create({ dkLen: 32 }).update(ser).digest()
//                );
                byte[] dig = new byte[0];
                return MatterArgs.builder()
                        .raw(dig)
                        .code(digerArgs.getCode())
                        .build();
            } else {
                throw new IllegalArgumentException("Unsupported code = " + digerArgs.getCode() + " for digester.");
            }
        }

        return MatterArgs.builder()
                .raw(digerArgs.getRaw())
                .code(digerArgs.getCode())
                .qb64b(digerArgs.getQb64b())
                .qb64(digerArgs.getQb64())
                .qb2(digerArgs.getQb2())
                .build();
    }

    public boolean verify(byte[] ser) {
        return this._verify.apply(ser, this.getRaw());
    }

    public boolean compare(byte[] ser, byte[] dig, Diger diger) {
        if (dig != null) {
            if (Arrays.equals(dig, this.getQb64b())) {
                return true;
            }
            diger = new Diger(MatterArgs.builder().qb64b(dig).build(), null);
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
}
