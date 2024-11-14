package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.utils.KeyPair;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.cardanofoundation.signify.cesr.util.Utils.CRYPTO_BOX_SEAL_BYTES;
import static org.junit.jupiter.api.Assertions.*;

class EncrypterTest {
    LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    @Test
    @DisplayName("should encrypt stuff")
    void shouldEncryptStuff() throws SodiumException {
        // (b'\x18;0\xc4\x0f*vF\xfa\xe3\xa2Eee\x1f\x96o\xce)G\x85\xe3X\x86\xda\x04\xf0\xdc\xde\x06\xc0+')
        byte[] seed = new byte[] {
            24, 59, 48, (byte)196, 15, 42, 118, 70, (byte)250, (byte)227, (byte)162, 69, 101, 101, 31,
            (byte)150, 111, (byte)206, 41, 71, (byte)133, (byte)227, 88, (byte)134, (byte)218, 4, (byte)240, (byte)220, (byte)222, 6,
            (byte)192, 43
        };
        byte[] seedQb64b = new Matter(
            RawArgs.builder()
                .raw(seed)
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build()
        ).getQb64b();

        assertEquals(
            "ABg7MMQPKnZG-uOiRWVlH5ZvzilHheNYhtoE8NzeBsAr",
            new String(seedQb64b)
        );

        // b'6\x08d\r\xa1\xbb9\x8dp\x8d\xa0\xc0\x13J\x87r'
        byte[] salt = new byte[] {
            54, 8, 100, 13, (byte)161, (byte)187, 57, (byte)141, 112, (byte)141, (byte)160, (byte)192, 19, 74, (byte)135,
            114
        };
        Matter saltMatter = new Matter(
            RawArgs.builder()
                .raw(salt)
                .code(Codex.MatterCodex.Salt_128.getValue())
                .build()
        );
        String saltQb64 = saltMatter.getQb64();
        byte[] saltQb64b = saltMatter.getQb64b();

        assertEquals("0AA2CGQNobs5jXCNoMATSody", saltQb64);

        // b'h,#|\x8ap"\x12\xc43t2\xa6\xe1\x18\x19\xf0f2,y\xc4\xc21@\xf5@\x15.\xa2\x1a\xcf'
        byte[] cryptSeed = new byte[] {
            104, 44, 35, 124, (byte)138, 112, 34, 18, (byte)196, 51, 116, 50, (byte)166, (byte)225, 24,
            25, (byte)240, 102, 50, 44, 121, (byte)196, (byte)194, 49, 64, (byte)245, 64, 21, 46, (byte)162,
            26, (byte)207
        };
        Signer cryptsigner = new Signer(
            RawArgs.builder()
                .raw(cryptSeed)
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build(),
            true);

        KeyPair keypair = lazySodium.cryptoSignSeedKeypair(cryptSeed);

        byte[] pubKey = new byte[32];
        boolean convertPubKey = lazySodium.convertPublicKeyEd25519ToCurve25519(pubKey, keypair.getPublicKey().getAsBytes());
        if (!convertPubKey) {
            throw new SodiumException("Failed to convert public key ed25519 to Curve25519");
        }

        byte[] priKey = new byte[32];
        boolean convertPriKey = lazySodium.convertSecretKeyEd25519ToCurve25519(priKey, keypair.getSecretKey().getAsBytes());
        if (!convertPriKey) {
            throw new SodiumException("Failed to convert secret key ed25519 to Curve25519");
        }

        assertThrows(Exception.class, () -> new Encrypter(RawArgs.builder().build(), null));

        Encrypter encrypter = new Encrypter(
            RawArgs.builder()
                .raw(pubKey)
                .build()
        );
        assertEquals(Codex.MatterCodex.X25519.getValue(), encrypter.getCode());
        assertEquals(
            "CAF7Wr3XNq5hArcOuBJzaY6Nd23jgtUVI6KDfb3VngkR",
            encrypter.getQb64()
        );
        assertArrayEquals(pubKey, encrypter.getRaw());
        assertTrue(encrypter.verifySeed(cryptsigner.getQb64b()));

        Cipher cipher = encrypter.encrypt(seedQb64b);
        assertEquals(Codex.MatterCodex.X25519_Cipher_Seed.getValue(), cipher.getCode());
        byte[] uncb = new byte[cipher.getRaw().length - CRYPTO_BOX_SEAL_BYTES];
        boolean boxSealOpen = lazySodium.cryptoBoxSealOpen(
            uncb,
            cipher.getRaw(),
            cipher.getRaw().length,
            encrypter.getRaw(),
            priKey
        );
        assertTrue(boxSealOpen);
        assertArrayEquals(seedQb64b, uncb);

        cipher = encrypter.encrypt(saltQb64b);
        assertEquals(Codex.MatterCodex.X25519_Cipher_Salt.getValue(), cipher.getCode());
        uncb = new byte[cipher.getRaw().length - CRYPTO_BOX_SEAL_BYTES];
        boxSealOpen = lazySodium.cryptoBoxSealOpen(
            uncb,
            cipher.getRaw(),
            cipher.getRaw().length,
            encrypter.getRaw(),
            priKey
        );
        assertTrue(boxSealOpen);
        assertArrayEquals(saltQb64b, uncb);

        Verfer verfer = new Verfer(
            RawArgs.builder()
                .raw(keypair.getPublicKey().getAsBytes())
                .code(Codex.MatterCodex.Ed25519.getValue())
                .build()
        );

        encrypter = new Encrypter(RawArgs.builder().build(), verfer.getQb64b());
        assertEquals(Codex.MatterCodex.X25519.getValue(), encrypter.getCode());
        assertEquals(
            "CAF7Wr3XNq5hArcOuBJzaY6Nd23jgtUVI6KDfb3VngkR",
            encrypter.getQb64()
        );
        assertArrayEquals(pubKey, encrypter.getRaw());
    }
}
