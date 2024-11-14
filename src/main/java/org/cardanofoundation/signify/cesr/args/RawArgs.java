package org.cardanofoundation.signify.cesr.args;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.*;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.DigiCodex;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Codex.NumCodex;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
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
        if (rawArgs.getCode() == null) {
            rawArgs.setCode(MatterCodex.Salt_128.getValue());
        }
        if (MatterCodex.Salt_128.getValue().equals(rawArgs.getCode())) {
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
                rawArgs.setCode(NumCodex.Short.getValue());
            } else if (_num.compareTo(number256.pow(4).subtract(BigInteger.ONE)) <= 0) {
                // make long version of code
                rawArgs.setCode(NumCodex.Long.getValue());
            } else if (_num.compareTo(number256.pow(8).subtract(BigInteger.ONE)) <= 0) {
                // make big version of code
                rawArgs.setCode(NumCodex.Big.getValue());
            } else if (_num.compareTo(number256.pow(16).subtract(BigInteger.ONE)) <= 0) {
                // make huge version of code
                rawArgs.setCode(NumCodex.Huge.getValue());
            } else {
                throw new IllegalArgumentException("Invalid num = " + num + ", too large to encode.");
            }

            rawArgs.setRaw(Utils.intToBytes(_num, Matter.getRawSize(rawArgs.getCode())));
        }

        return rawArgs;
    }

    public static RawArgs generateSaiderRaw(RawArgs args, Map<String, Object> sad, CoreUtil.Serials kind, String label) {
        label = label == null ? Saider.Ids.d.getValue() : label;
        if (args.getRaw() == null) {
            if (sad == null || !sad.containsKey(label)) {
                throw new EmptyMaterialException("Empty material");
            }

            String code = args.getCode();
            if (code == null) {
                code = MatterCodex.Blake3_256.getValue();
            }

            if (!DigiCodex.has(code)) {
                throw new UnsupportedOperationException("Unsupported digest code = " + code);
            }

            Map<String, Object> sadCopy = new HashMap<>(sad);

            Saider.DeriveResult result = Saider.derive(sadCopy, code, kind, label);
            args.setRaw(result.raw());
            args.setCode(code);
        }
        return args;
    }

    public static RawArgs generateCipherRaw(RawArgs args) {
        if (args.getRaw() != null && args.getCode() == null) {
            if (args.getRaw().length == Matter.getRawSize(MatterCodex.X25519_Cipher_Salt.getValue())) {
                args.setCode(MatterCodex.X25519_Cipher_Salt.getValue());
            } else if (args.getRaw().length == Matter.getRawSize(MatterCodex.X25519_Cipher_Seed.getValue())) {
                args.setCode(MatterCodex.X25519_Cipher_Salt.getValue());
            }
        }

        List<String> validCodes = List.of(
            MatterCodex.X25519_Cipher_Salt.getValue(),
            MatterCodex.X25519_Cipher_Seed.getValue()
        );
        if (!validCodes.contains(args.getCode())) {
            throw new UnsupportedOperationException("Unsupported Cipher code = " + args.getCode());
        }

        return args;
    }

    public static RawArgs generateEncrypterRaw(RawArgs args, byte[] verkey) throws SodiumException {
        if (args.getCode() == null) {
            args.setCode(MatterCodex.X25519.getValue());
        }
        if (args.getRaw() != null && verkey != null) {
            Verfer verfer = new Verfer(verkey);
            List<String> validCodes = List.of(
                MatterCodex.Ed25519N.getValue(),
                MatterCodex.Ed25519.getValue()
            );
            if (!validCodes.contains(verfer.getCode())) {
                throw new UnsupportedOperationException("Unsupported verkey derivation code = " + verfer.getCode());
            }
            LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
            byte[] raw = new byte[0];
            boolean success = lazySodium.convertPublicKeyEd25519ToCurve25519(raw, verfer.getRaw());
            if (!success) {
                throw new SodiumException("Failed to convert public key ed25519 to Curve25519");
            }
            args.setRaw(raw);
        }
        return args;
    }

    public static RawArgs generateDecrypterRaw(RawArgs args, byte[] seed) throws SodiumException {
        if (args.getCode() == null) {
            args.setCode(MatterCodex.X25519_Private.getValue());
        }
        if (seed != null) {
            Signer signer = new Signer(seed);
            if (!signer.getCode().equals(MatterCodex.Ed25519_Seed.getValue())) {
                throw new UnsupportedOperationException("Unsupported signing seed derivation code " + signer.getCode());
            }

            byte[] sigKey = ByteBuffer.allocate(signer.getRaw().length + signer.getVerfer().getRaw().length)
                .put(signer.getRaw())
                .put(signer.getVerfer().getRaw())
                .array();

            LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
            byte[] raw = new byte[0];
            boolean success = lazySodium.convertSecretKeyEd25519ToCurve25519(raw, sigKey);
            if (!success) {
                throw new SodiumException("Failed to convert secret key ed25519 to Curve25519");
            }
            args.setRaw(raw);
        }

        return args;
    }
}