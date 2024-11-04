package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.sun.jna.NativeLong;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import com.goterl.lazysodium.interfaces.PwHash.Alg;
import lombok.Getter;

public class Salter extends Matter {
    @Getter
    public Tier tier;

    private final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

    public Salter(RawArgs args) {
        this(args, Tier.low);
    }

    public Salter(String qb64) {
        this(qb64, Tier.low);
    }

    public Salter(RawArgs args, Tier tier) {
        super(RawArgs.generateSalt128Raw(args));
    }

    public Salter(String qb64, Tier tier) {
        super(qb64);
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
        if (temp) {
            opslimit = 1; //libsodium.crypto_pwhash_OPSLIMIT_MIN
            memlimit = 8192; //libsodium.crypto_pwhash_MEMLIMIT_MIN
        } else {
            memlimit = switch (tier) {
                case Tier.low -> {
                    opslimit = 2; // libsodium.crypto_pwhash_OPSLIMIT_INTERACTIVE
                    yield 67108864;
                }
                case Tier.med -> {
                    opslimit = 3; // libsodium.crypto_pwhash_OPSLIMIT_MODERATE
                    yield 268435456;
                }
                case Tier.high -> {
                    opslimit = 4; // libsodium.crypto_pwhash_OPSLIMIT_SENSITIVE
                    yield 1073741824;
                }
            };
        }

        return this.cryptoPwHash(size, path.getBytes(), opslimit, memlimit);
    }

    private byte[] cryptoPwHash(int size, byte[] path, long opslimit, long memlimit) {
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

        if (!success) {
            throw new RuntimeException("CryptoPwHash failed");
        }

        return stretch;
    }

    public Signer signer(String code, boolean transferable, String path, Tier tier, boolean temp) {
        final byte[] seed = this.stretch(Matter.getRawSize(code), path, tier, temp);
        RawArgs rawArgs = RawArgs.builder()
                .raw(seed)
                .code(code)
                .build();
        return new Signer(rawArgs, transferable);
    }

}
