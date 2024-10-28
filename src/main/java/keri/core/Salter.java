package keri.core;

import keri.core.args.SalterArgs;
import keri.core.args.SignerArgs;

public class Salter extends Matter {

    public Tier tier;

    public Salter(SalterArgs args) {
        super(args.toMatterArgs());
        // TODO Auto-generated constructor stub
    }

    public enum Tier {
        low,
        med,
        high
    }

    public Tier getTier() {
        return this.tier;
    }

    private byte[] stretch(int size, String path, Tier tier, boolean temp) {
        int opslimit, memlimit;

        // Harcoded values based on keripy
        if(temp) {
            opslimit = 1;
            memlimit = 8192;
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
        
        // TODO: find method crypto_pwhash from libsodium java
        /*
         * // crypto_pwhash from libsodim tyscript
         * return libsodium.crypto_pwhash(
            size,
            path,
            this.raw,
            opslimit,
            memlimit,
            libsodium.crypto_pwhash_ALG_ARGON2ID13
        );
         */
        return null;
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
