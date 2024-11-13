package org.cardanofoundation.signify.cesr.args;

import com.goterl.lazysodium.LazySodiumJava;
import lombok.*;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.DigiCodex;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RawArgs {
    byte[] raw;

    String code;

    public static RawArgs generateSalt128Raw(RawArgs rawArgs) {
        if (Codex.MatterCodex.Salt_128.getValue().equals(rawArgs.getCode())) {
            if (rawArgs.getRaw() == null) {
                LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
                final byte[] salt = lazySodium.randomBytesBuf(16); // crypto_pwhash_SALTBYTES
                rawArgs.setRaw(salt);
            }
        } else {
            throw new IllegalArgumentException("invalid code for Salter, only Salt_128 accepted");
        }

        return rawArgs;
    }

    public static RawArgs generateEd25519SeedRaw(RawArgs rawArgs) {
        if (MatterCodex.Ed25519_Seed.getValue().equals(rawArgs.getCode())) {
            if (rawArgs.getRaw() == null) {
                LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
                final byte[] salt = lazySodium.randomBytesBuf(32); // crypto_pwhash_SALTBYTES
                rawArgs.setRaw(salt);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported signer code = " + rawArgs.getCode());
        }

        return rawArgs;
    }

    public static RawArgs generateBlake3256SeedRaw(RawArgs rawArgs, byte[] ser) {
        if (ser == null) {
            throw new EmptyMaterialException("Empty material");
        }
        if (rawArgs.getCode() == null) {
            rawArgs.setCode(MatterCodex.Blake3_256.getValue());
        }

        if (MatterCodex.Blake3_256.getValue().equals(rawArgs.getCode())) {
            if (rawArgs.getRaw() == null) {
                rawArgs.setRaw(CoreUtil.blake3_256(ser, 32));
            }
        } else {
            throw new UnsupportedOperationException("Unsupported code = " + rawArgs.getCode() + " for digester.");
        }

        return rawArgs;
    }

    public static RawArgs generateNumDexRaw(RawArgs rawArgs, BigInteger num, String numh) {
        BigInteger _num;

        if (rawArgs.getRaw() == null) {
            if (num != null) {
                _num = num;
            } else if (numh != null) {
                _num = new BigInteger(numh, 16);
            } else {
                _num = BigInteger.ZERO;
            }


            BigInteger number256 = BigInteger.valueOf(256);
            if (_num.compareTo(number256.pow(2).subtract(BigInteger.ONE)) <= 0) {
                // make short version of code
                rawArgs.setCode(Codex.NumCodex.Short.getValue());
            } else if (_num.compareTo(number256.pow(4).subtract(BigInteger.ONE)) <= 0) {
                // make long version of code
                rawArgs.setCode(Codex.NumCodex.Long.getValue());
            } else if (_num.compareTo(number256.pow(8).subtract(BigInteger.ONE)) <= 0) {
                // make big version of code
                rawArgs.setCode(Codex.NumCodex.Big.getValue());
            } else if (_num.compareTo(number256.pow(16).subtract(BigInteger.ONE)) <= 0) {
                // make huge version of code
                rawArgs.setCode(Codex.NumCodex.Huge.getValue());
            } else {
                throw new IllegalArgumentException("Invalid num = " + num + ", too large to encode.");
            }

            rawArgs.setRaw(Utils.intToBytes(_num, Matter.getRawSize(rawArgs.getCode())));
        }

        return rawArgs;
    }


    public static RawArgs generateSaiderRaw(RawArgs args, Map<String, Object> sad, CoreUtil.Serials kind, String label) {
        if (args.getRaw() == null) {
            if (sad == null || !sad.containsKey(label)) {
                throw new EmptyMaterialException("Empty material");
            }

            String code = args.getCode();
            if (code == null) {
                code = MatterCodex.Blake3_256.getValue();
            }

            if (!DigiCodex.has(code)) {
                throw new IllegalArgumentException("Unsupported digest code = " + code);
            }

            Map<String, Object> sadCopy = new HashMap<>(sad);

            Saider.DeriveResult result = Saider.derive(sadCopy, code, kind, label);
            args.setRaw(result.raw());
            args.setCode(code);
        }
        return args;
    }
}