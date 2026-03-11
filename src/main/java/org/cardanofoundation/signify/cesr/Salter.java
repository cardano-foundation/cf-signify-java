package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.sun.jna.NativeLong;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import com.goterl.lazysodium.interfaces.PwHash.Alg;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.generated.keria.model.Tier;

public class Salter extends Matter {
    @Getter
    public Tier tier;

    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    public Salter() {
        this(RawArgs.builder()
                .code(Codex.MatterCodex.Salt_128.getValue())
                .build(), 
            Tier.LOW);
    }

    public Salter(RawArgs args) {
        this(args, Tier.LOW);
    }

    public Salter(String qb64) {
        this(qb64, Tier.LOW);
    }

    public Salter(RawArgs args, Tier tier) {
        super(RawArgs.generateSalt128Raw(args));
        this.tier = tier == null ? Tier.LOW : tier;
    }

    public Salter(String qb64, Tier tier) {
        super(qb64);
        this.tier = tier == null ? Tier.LOW : tier;
    }

    public Salter(byte[] qb64b) {
        super(qb64b);
    }

    public byte[] stretch() throws LibsodiumException {
        return stretch(32);
    }

    public byte[] stretch(int size) throws LibsodiumException {
        return stretch(size, "");
    }

    public byte[] stretch(int size, String path) throws LibsodiumException {
        return stretch(size, path, null);
    }

    public byte[] stretch(int size, String path, Tier tier) throws LibsodiumException {
        return stretch(size, path, tier, false);
    }

    public byte[] stretch(int size, String path, Tier tier, boolean temp) throws LibsodiumException {
        tier = tier == null ? this.tier : tier;
        int opslimit, memlimit;

        // Hardcoded values based on keripy
        if (temp) {
            opslimit = 1; //libsodium.crypto_pwhash_OPSLIMIT_MIN
            memlimit = 8192; //libsodium.crypto_pwhash_MEMLIMIT_MIN
        } else {
            memlimit = switch (tier) {
                case Tier.LOW -> {
                    opslimit = 2; // libsodium.crypto_pwhash_OPSLIMIT_INTERACTIVE
                    yield 67108864;
                }
                case Tier.MED -> {
                    opslimit = 3; // libsodium.crypto_pwhash_OPSLIMIT_MODERATE
                    yield 268435456;
                }
                case Tier.HIGH -> {
                    opslimit = 4; // libsodium.crypto_pwhash_OPSLIMIT_SENSITIVE
                    yield 1073741824;
                }
            };
        }

        return this.cryptoPwHash(size, path.getBytes(), opslimit, memlimit);
    }

    private byte[] cryptoPwHash(int size, byte[] path, long opslimit, long memlimit) throws LibsodiumException {
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
            throw new LibsodiumException("Failed to stretch salt using given path");
        }

        return stretch;
    }

    public Signer signer() throws LibsodiumException {
        return signer(Codex.MatterCodex.Ed25519_Seed.getValue());
    }

    public Signer signer(String code) throws LibsodiumException {
        return signer(code, true);
    }

    public Signer signer(String code, Boolean transferable) throws LibsodiumException {
        return signer(code, transferable, "");
    }

    public Signer signer(String code, Boolean transferable, String path) throws LibsodiumException {
        return signer(code, transferable, path, null);
    }

    public Signer signer(String code, Boolean transferable, String path, Tier tier) throws LibsodiumException {
        return signer(code, transferable, path, tier, false);
    }

    public Signer signer(
        String code,
        Boolean transferable,
        String path,
        Tier tier,
        Boolean temp
    ) throws LibsodiumException {
        transferable = transferable == null || transferable;
        temp = temp != null && temp;
        path = path == null ? "" : path;
        final byte[] seed = this.stretch(Matter.getRawSize(code), path, tier, temp);
        RawArgs rawArgs = RawArgs.builder()
                .raw(seed)
                .code(code)
                .build();
        return new Signer(rawArgs, transferable);
    }

}
