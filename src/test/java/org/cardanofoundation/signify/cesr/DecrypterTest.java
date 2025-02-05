package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.utils.KeyPair;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DecrypterTest {
    LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    @Test
    @DisplayName("should decrypt stuff")
    void shouldDecryptStuff() throws LibsodiumException {
        // (b'\x18;0\xc4\x0f*vF\xfa\xe3\xa2Eee\x1f\x96o\xce)G\x85\xe3X\x86\xda\x04\xf0\xdc\xde\x06\xc0+')
        byte[] seed = new byte[]{
            24, 59, 48, (byte) 196, 15, 42, 118, 70, (byte) 250, (byte) 227, (byte) 162, 69, 101, 101, 31,
            (byte) 150, 111, (byte) 206, 41, 71, (byte) 133, (byte) 227, 88, (byte) 134, (byte) 218, 4, (byte) 240, (byte) 220, (byte) 222, 6,
            (byte) 192, 43
        };

        Signer signer = new Signer(
            RawArgs.builder()
                .raw(seed)
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build()
        );
        assertEquals(Codex.MatterCodex.Ed25519.getValue(), signer.getVerfer().getCode());
        assertTrue(signer.getVerfer().isTransferable()); // default
        String seedQb64 = signer.getQb64();
        byte[] seedQb64b = signer.getQb64b();
        assertEquals("ABg7MMQPKnZG-uOiRWVlH5ZvzilHheNYhtoE8NzeBsAr", seedQb64);

        // also works for Matter
        assertArrayEquals(
            seedQb64b,
            new Matter(
                RawArgs.builder()
                    .raw(seed)
                    .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                    .build()
            ).getQb64b()
        );

        // raw = b'6\x08d\r\xa1\xbb9\x8dp\x8d\xa0\xc0\x13J\x87r'
        byte[] raw = new byte[]{
            54, 8, 100, 13, (byte) 161, (byte) 187, 57, (byte) 141, 112, (byte) 141, (byte) 160, (byte) 192, 19, 74, (byte) 135,
            114
        };
        Salter salter = new Salter(
            RawArgs.builder()
                .raw(raw)
                .code(Codex.MatterCodex.Salt_128.getValue())
                .build()
        );
        assertEquals(Codex.MatterCodex.Salt_128.getValue(), salter.getCode());
        String saltQb64 = salter.getQb64();
        byte[] saltQb64b = salter.getQb64b();
        assertEquals("0AA2CGQNobs5jXCNoMATSody", saltQb64);

        // Also works for Matter
        assertArrayEquals(
            saltQb64b,
            new Matter(RawArgs.builder()
                .raw(raw)
                .code(Codex.MatterCodex.Salt_128.getValue())
                .build()
            ).getQb64b()
        );

        /// cryptSeed = b'h,#|\x8ap"\x12\xc43t2\xa6\xe1\x18\x19\xf0f2,y\xc4\xc21@\xf5@\x15.\xa2\x1a\xcf'
        byte[] cryptSeed = new byte[]{
            104, 44, 35, 124, (byte) 138, 112, 34, 18, (byte) 196, 51, 116, 50, (byte) 166, (byte) 225, 24,
            25, (byte) 240, 102, 50, 44, 121, (byte) 196, (byte) 194, 49, 64, (byte) 245, 64, 21, 46, (byte) 162,
            26, (byte) 207
        };
        Signer cryptsigner = new Signer(
            RawArgs.builder()
                .raw(cryptSeed)
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build(),
            true
        );

        KeyPair keypair;
        try {
            keypair = lazySodium.cryptoSignSeedKeypair(cryptSeed);
        } catch (SodiumException e) {
            throw new LibsodiumException(e);
        }
        byte[] pubKey = new byte[32];
        byte[] priKey = new byte[32];
        lazySodium.convertPublicKeyEd25519ToCurve25519(pubKey, keypair.getPublicKey().getAsBytes());
        lazySodium.convertSecretKeyEd25519ToCurve25519(priKey, keypair.getSecretKey().getAsBytes());

        // Test empty constructor
        assertThrows(Exception.class, () -> new Decrypter(RawArgs.builder().build()));

        // Create encrypter
        Encrypter encrypter = new Encrypter(
            RawArgs.builder()
                .raw(pubKey)
                .build()
        );
        assertEquals(Codex.MatterCodex.X25519.getValue(), encrypter.getCode());
        assertEquals("CAF7Wr3XNq5hArcOuBJzaY6Nd23jgtUVI6KDfb3VngkR", encrypter.getQb64());
        assertArrayEquals(pubKey, encrypter.getRaw());

        // Create cipher of seed
        Cipher seedcipher = encrypter.encrypt(seedQb64b);
        assertEquals(Codex.MatterCodex.X25519_Cipher_Seed.getValue(), seedcipher.getCode());
        // each encryption uses a nonce so not a stable representation for testing

        // Create decrypter from priKey
        Decrypter decrypter = new Decrypter(
            RawArgs.builder()
                .raw(priKey)
                .build()
        );
        assertEquals(Codex.MatterCodex.X25519_Private.getValue(), decrypter.getCode());
        assertEquals("OLCFxqMz1z1UUS0TEJnvZP_zXHcuYdQsSGBWdOZeY5VQ", decrypter.getQb64());
        assertArrayEquals(priKey, decrypter.getRaw());

        // Decrypt seed cipher using ser
        Signer designer = (Signer) decrypter.decrypt(
            seedcipher.getQb64b(),
            null,
            signer.getVerfer().isTransferable()
        );
        assertArrayEquals(designer.getQb64b(), seedQb64b);
        assertEquals(Codex.MatterCodex.Ed25519_Seed.getValue(), designer.getCode());
        assertEquals(Codex.MatterCodex.Ed25519.getValue(), designer.getVerfer().getCode());
        assertTrue(signer.getVerfer().isTransferable());

        // Decrypt seed cipher using cipher
        designer = (Signer) decrypter.decrypt(
            null,
            seedcipher,
            signer.getVerfer().isTransferable()
        );
        assertArrayEquals(designer.getQb64b(), seedQb64b);
        assertEquals(Codex.MatterCodex.Ed25519_Seed.getValue(), designer.getCode());
        assertEquals(Codex.MatterCodex.Ed25519.getValue(), designer.getVerfer().getCode());
        assertTrue(signer.getVerfer().isTransferable());

        // Create cipher of salt
        Cipher saltcipher = encrypter.encrypt(saltQb64b);
        assertEquals(Codex.MatterCodex.X25519_Cipher_Salt.getValue(), saltcipher.getCode());

        // Decrypt salt cipher using ser
        Salter desalter = (Salter) decrypter.decrypt(saltcipher.getQb64b(), null);
        assertArrayEquals(desalter.getQb64b(), saltQb64b);
        assertEquals(Codex.MatterCodex.Salt_128.getValue(), desalter.getCode());

        // Decrypt salt cipher using cipher
        desalter = (Salter) decrypter.decrypt(null, saltcipher);
        assertArrayEquals(desalter.getQb64b(), saltQb64b);
        assertEquals(Codex.MatterCodex.Salt_128.getValue(), desalter.getCode());

        // Use previously stored fully qualified seed cipher with different nonce
        // get from seedCipher above
        String cipherSeed = "PM9jOGWNYfjM_oLXJNaQ8UlFSAV5ACjsUY7J16xfzrlpc9Ve3A5WYrZ4o_NHtP5lhp78Usspl9fyFdnCdItNd5JyqZ6dt8SXOt6TOqOCs-gy0obrwFkPPqBvVkEw";
        designer = (Signer) decrypter.decrypt(
            cipherSeed.getBytes(),
            null,
            signer.getVerfer().isTransferable()
        );
        assertArrayEquals(designer.getQb64b(), seedQb64b);
        assertEquals(Codex.MatterCodex.Ed25519_Seed.getValue(), designer.getCode());
        assertEquals(Codex.MatterCodex.Ed25519.getValue(), designer.getVerfer().getCode());

        // Use previously stored fully qualified salt cipher with different nonce
        // get from saltCipher above
        String cipherSalt = "1AAHjlR2QR9J5Et67Wy-ZaVdTryN6T6ohg44r73GLRPnHw-5S3ABFkhWyIwLOI6TXUB_5CT13S8JvknxLxBaF8ANPK9FSOPD8tYu";
        desalter = (Salter) decrypter.decrypt(cipherSalt.getBytes(), null);
        assertArrayEquals(desalter.getQb64b(), saltQb64b);
        assertEquals(Codex.MatterCodex.Salt_128.getValue(), desalter.getCode());

        // Create new decrypter but use seed parameter to init priKey
        decrypter = new Decrypter(RawArgs.builder().build(), cryptsigner.getQb64b());
        assertEquals(Codex.MatterCodex.X25519_Private.getValue(), decrypter.getCode());
        assertEquals("OLCFxqMz1z1UUS0TEJnvZP_zXHcuYdQsSGBWdOZeY5VQ", decrypter.getQb64());
        assertArrayEquals(priKey, decrypter.getRaw());

        // Decrypt cipherSalt
        desalter = (Salter) decrypter.decrypt(saltcipher.getQb64b(), null);
        assertArrayEquals(desalter.getQb64b(), saltQb64b);
        assertEquals(Codex.MatterCodex.Salt_128.getValue(), desalter.getCode());
    }
}

