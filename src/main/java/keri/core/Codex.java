package keri.core;

import java.util.HashMap;
import java.util.Map;

public class Codex {

    /*
     * Check if property exists in codex
     */
    public boolean has(String prop) {
        Map<String, String> map = new HashMap<>();
        for (java.lang.reflect.Field field : this.getClass().getDeclaredFields()) {
            try {
                map.put((String) field.get(this), field.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map.containsKey(prop);
    }

    static class BexCodex extends Codex {
        public final String StrB64_L0 = "4A"; // String Base64 Only Lead Size 0
        public final String StrB64_L1 = "5A"; // String Base64 Only Lead Size 1
        public final String StrB64_L2 = "6A"; // String Base64 Only Lead Size 2
        public final String StrB64_Big_L0 = "7AAA"; // String Base64 Only Big Lead Size 0
        public final String StrB64_Big_L1 = "8AAA"; // String Base64 Only Big Lead Size 1
        public final String StrB64_Big_L2 = "9AAA"; // String Base64 Only Big Lead Size 2
    }

   static class DigiCodex extends Codex {
        public final String Blake3_256 = "E"; // Blake3 256 bit digest self-addressing derivation.
        public final String Blake2b_256 = "F"; // Blake2b 256 bit digest self-addressing derivation.
        public final String Blake2s_256 = "G"; // Blake2s 256 bit digest self-addressing derivation.
        public final String SHA3_256 = "H"; // SHA3 256 bit digest self-addressing derivation.
        public final String SHA2_256 = "I"; // SHA2 256 bit digest self-addressing derivation.
        public final String Blake3_512 = "0D"; // Blake3 512 bit digest self-addressing derivation.
        public final String Blake2b_512 = "0E"; // Blake2b 512 bit digest self-addressing derivation.
        public final String SHA3_512 = "0F"; // SHA3 512 bit digest self-addressing derivation.
        public final String SHA2_512 = "0G"; // SHA2 512 bit digest self-addressing derivation.
    }
    
    static class SmallVarRawSizeCodex extends Codex {
        public final String Lead0 = "4"; // First Selector Character for all ls == 0 codes
        public final String Lead1 = "5"; // First Selector Character for all ls == 1 codes
        public final String Lead2 = "6"; // First Selector Character for all ls == 2 codes
    }

    static class LargeVarRawSizeCodex extends Codex {
        public final String Lead0_Big = "7"; // First Selector Character for all ls == 0 codes
        public final String Lead1_Big = "8"; // First Selector Character for all ls == 1 codes
        public final String Lead2_Big = "9"; // First Selector Character for all ls == 2 codes
    }

    public static class MatterCodex extends Codex {
        public final String Ed25519_Seed = "A"; // Ed25519 256 bit random seed for private key
        public final String Ed25519N = "B"; // Ed25519 verification key non-transferable, basic derivation.
        public final String X25519 = "C"; // X25519 public encryption key, converted from Ed25519 or Ed25519N.
        public final String Ed25519 = "D"; // Ed25519 verification key basic derivation
        public final String Blake3_256 = "E"; // Blake3 256 bit digest self-addressing derivation.
        public final String SHA3_256 = "H"; // SHA3 256 bit digest self-addressing derivation.
        public final String SHA2_256 = "I"; // SHA2 256 bit digest self-addressing derivation.
        public final String ECDSA_256k1_Seed = "J"; // ECDSA secp256k1 256 bit random Seed for private key
        public final String X25519_Private = "O"; // X25519 private decryption key converted from Ed25519
        public final String X25519_Cipher_Seed = "P"; // X25519 124 char b64 Cipher of 44 char qb64 Seed
        public final String ECDSA_256r1_Seed = "Q"; // ECDSA secp256r1 256 bit random Seed for private key
        public final String Salt_128 = "0A"; // 128 bit random salt or 128 bit number (see Huge)
        public final String Ed25519_Sig = "0B"; // Ed25519 signature.
        public final String ECDSA_256k1_Sig = "0C"; // ECDSA secp256k1 signature.
        public final String ECDSA_256r1_Sig = "0I"; // ECDSA secp256r1 signature.
        public final String StrB64_L0 = "4A"; // String Base64 Only Lead Size 0
        public final String StrB64_L1 = "5A"; // String Base64 Only Lead Size 1
        public final String StrB64_L2 = "6A"; // String Base64 Only Lead Size 2
        public final String ECDSA_256k1N = "1AAA"; // ECDSA secp256k1 verification key non-transferable, basic derivation.
        public final String ECDSA_256k1 = "1AAB"; // ECDSA public verification or encryption key, basic derivation
        public final String X25519_Cipher_Salt = "1AAH"; // X25519 100 char b64 Cipher of 24 char qb64 Salt
        public final String ECDSA_256r1N = "1AAI"; // ECDSA secp256r1 verification key non-transferable, basic derivation.
        public final String ECDSA_256r1 = "1AAJ"; // ECDSA secp256r1 verification or encryption key, basic derivation
        public final String StrB64_Big_L0 = "7AAA"; // String Base64 Only Big Lead Size 0
        public final String StrB64_Big_L1 = "8AAA"; // String Base64 Only Big Lead Size 1
        public final String StrB64_Big_L2 = "9AAA"; // String Base64 Only Big Lead Size 2
    }

    static class NonTransCodex extends Codex {
        public final String Ed25519N = "B"; // Ed25519 verification key non-transferable, basic derivation.
        public final String ECDSA_256k1N = "1AAA"; // ECDSA secp256k1 verification key non-transferable, basic derivation.
        public final String Ed448N = "1AAC"; // Ed448 non-transferable prefix public signing verification key. Basic derivation.
        public final String ECDSA_256r1N = "1AAI"; // ECDSA secp256r1 verification key non-transferable, basic derivation.
    }
    
    static class NumCodex extends Codex {
        public final String Short = "M"; // Short 2 byte b2 number
        public final String Long = "0H"; // Long 4 byte b2 number
        public final String Big = "N"; // Big 8 byte b2 number
        public final String Huge = "0A"; // Huge 16 byte b2 number (same as Salt_128)
    }
}