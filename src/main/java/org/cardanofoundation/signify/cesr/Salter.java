package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.sun.jna.NativeLong;
import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.args.SalterArgs;
import org.cardanofoundation.signify.cesr.args.SignerArgs;
import com.goterl.lazysodium.interfaces.PwHash.Alg;
import lombok.Getter;

@Getter
public class Salter extends Matter {
    public Tier tier;
    private static final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

    public Salter(SalterArgs args) {
        super(initializeArgs(args));

        if (!Codex.MatterCodex.Salt_128.getValue().equals(this.getCode())) {
            throw new IllegalArgumentException("invalid code for Salter, only Salt_128 accepted");
        }

        this.tier = args.getTier() != null ? args.getTier() : Tier.low;
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
            switch (tier) {
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

    private static MatterArgs initializeArgs(SalterArgs salterArgs) {
        if (salterArgs.getRaw() == null && salterArgs.getQb64() == null
                && salterArgs.getQb64b() == null && salterArgs.getQb2() == null) {
            if (Codex.MatterCodex.Salt_128.getValue().equals(salterArgs.getCode())) {
                final byte[] salt = lazySodium.randomBytesBuf(16); // crypto_pwhash_SALTBYTES
                return MatterArgs.builder()
                        .raw(salt)
                        .code(salterArgs.getCode())
                        .build();
            } else {
                throw new IllegalArgumentException("invalid code for Salter, only Salt_128 accepted");
            }
        }

        return MatterArgs.builder()
                .raw(salterArgs.getRaw())
                .code(salterArgs.getCode())
                .qb64b(salterArgs.getQb64b())
                .qb64(salterArgs.getQb64())
                .qb2(salterArgs.getQb2())
                .build();
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
