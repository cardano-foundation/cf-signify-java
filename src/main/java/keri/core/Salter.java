package keri.core;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.interfaces.Sign;
import com.sun.jna.NativeLong;
import keri.core.args.MatterArgs;
import keri.core.args.SalterArgs;
import keri.core.args.SignerArgs;
import com.goterl.lazysodium.interfaces.PwHash.Alg;
import keri.core.exceptions.EmptyMaterialError;
import keri.core.Codex.MatterCodex;
import lombok.Getter;

import java.security.SecureRandom;

public class Salter extends Matter {
    private final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

    @Getter
    public Tier tier;

    public Salter(SalterArgs args) {
        super(initializeArgs(args));

        if (!this.getCode().equals(MatterCodex.Salt_128.getValue())) {
            throw new RuntimeException("Invalid code for Salter, only Salt_128 accepted");
        }

        this.tier = args.getTier() != null ? args.getTier() : Tier.low;
    }

    private static MatterArgs initializeArgs(SalterArgs args) {
        try {
            if (args.getRaw() == null && args.getCode() == null && args.getQb64() == null && args.getQb64b() == null && args.getQb2() == null) {
                throw new EmptyMaterialError("Empty material");
            }
        } catch (EmptyMaterialError e) {
            if (MatterCodex.Salt_128.getValue().equals(args.getCode())) {
                byte[] salt = new byte[Sign.SEEDBYTES];
                new SecureRandom().nextBytes(salt);
//                byte[] salt = lazySodium.randomBytesBuf(Sign.SEEDBYTES);
                args.setRaw(salt);
            } else {
                throw new IllegalArgumentException("Invalid code for Salter, only Salt_128 accepted");
            }
        }
        return args.toMatterArgs();
    }

    public enum Tier {
        low,
        med,
        high
    }

    private byte[] stretch(int size, String path, Tier tier, boolean temp) {
        tier = tier == null ? this.tier : tier;
        int opslimit, memlimit;

        // Harcoded values based on keripy
        if(temp) {
            opslimit = 1; //libsodium.crypto_pwhash_OPSLIMIT_MIN
            memlimit = 8192; //libsodium.crypto_pwhash_MEMLIMIT_MIN
        } else {
            switch(tier) {
                case Tier.low:
                    opslimit = 2; // libsodium.crypto_pwhash_OPSLIMIT_INTERACTIVE
                    memlimit = 67108864; // libsodium.crypto_pwhash_MEMLIMIT_INTERACTIVE
                    break;
                case Tier.med:
                    opslimit = 3; // libsodium.crypto_pwhash_OPSLIMIT_MODERATE
                    memlimit = 268435456; // libsodium.crypto_pwhash_MEMLIMIT_MODERATE
                    break;
                case Tier.high:
                    opslimit = 4; // libsodium.crypto_pwhash_OPSLIMIT_SENSITIVE
                    memlimit = 1073741824; // libsodium.crypto_pwhash_MEMLIMIT_SENSITIVE
                    break;
                default:
                    throw new RuntimeException("Unsupported security tier = " + tier + ".");
            }
        }

        return this.cryptoPwHash(size, path.getBytes(), opslimit, memlimit);
    }

    public byte[] cryptoPwHash(int size, byte[] path, long opslimit, long memlimit) {
        byte[] stretch = new byte[size];
        boolean success = lazySodium.cryptoPwHash(
            stretch,
            stretch.length,
            path,
            path.length,
            this.getRaw(),
            opslimit,
            new NativeLong(memlimit),
            Alg.PWHASH_ALG_ARGON2ID13
        );

        return success ? stretch : null;
    }

    public Signer signer(String code, boolean transferable, String path, Tier tier, boolean temp) {
        final byte[] seed = this.stretch(Matter.getRawSize(code), path, tier, temp);

        SignerArgs signerArgs = SignerArgs.builder()
            .raw(seed)
            .code(code)
            .transferable(transferable)
            .build();

        return new Signer(signerArgs);
    }

}
