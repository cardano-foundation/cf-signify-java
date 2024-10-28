package keri.core;

import java.util.Map;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Codex {

    @Getter
    public enum BexCodex {
        StrB64_L0("4A"), // String Base64 Only Lead Size 0
        StrB64_L1("5A"), // String Base64 Only Lead Size 1
        StrB64_L2("6A"), // String Base64 Only Lead Size 2
        StrB64_Big_L0("7AAA"), // String Base64 Only Big Lead Size 0
        StrB64_Big_L1("8AAA"), // String Base64 Only Big Lead Size 1
        StrB64_Big_L2("9AAA"); // String Base64 Only Big Lead Size 2

        private final String value;
        private static final Map<String, BexCodex> bexCodexFieldMap =
            Arrays.stream(BexCodex.values())
                .collect(Collectors.toMap(BexCodex::getValue, Function.identity()));
            
        BexCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return bexCodexFieldMap.containsKey(value);
        }
    }

   @Getter
   public enum DigiCodex {
        Blake3_256("E"), // Blake3 256 bit digest self-addressing derivation.
        Blake2b_256("F"), // Blake2b 256 bit digest self-addressing derivation.
        Blake2s_256("G"), // Blake2s 256 bit digest self-addressing derivation.
        SHA3_256("H"), // SHA3 256 bit digest self-addressing derivation.
        SHA2_256("I"), // SHA2 256 bit digest self-addressing derivation.
        Blake3_512("0D"), // Blake3 512 bit digest self-addressing derivation.
        Blake2b_512("0E"), // Blake2b 512 bit digest self-addressing derivation.
        SHA3_512("0F"), // SHA3 512 bit digest self-addressing derivation.
        SHA2_512("0G"); // SHA2 512 bit digest self-addressing derivation.

        private final String value;
        private static final Map<String, DigiCodex> digiCodexFieldMap =
            Arrays.stream(DigiCodex.values())
                .collect(Collectors.toMap(DigiCodex::getValue, Function.identity()));
            
        DigiCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return digiCodexFieldMap.containsKey(value);
        }
    }
    
    @Getter
    public enum SmallVarRawSizeCodex {
        Lead0("4"), // First Selector Character for all ls == 0 codes
        Lead1("5"), // First Selector Character for all ls == 1 codes
        Lead2("6"); // First Selector Character for all ls == 2 codes

        private final String value;
        private static final Map<String, SmallVarRawSizeCodex> smallVarRawSizeCodexFieldMap =
            Arrays.stream(SmallVarRawSizeCodex.values())
                .collect(Collectors.toMap(SmallVarRawSizeCodex::getValue, Function.identity()));
            
        SmallVarRawSizeCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return smallVarRawSizeCodexFieldMap.containsKey(value);
        }

        static SmallVarRawSizeCodex fromLsIndex(int ls) {
            return SmallVarRawSizeCodex.values()[ls];
        }
    }

    @Getter
    public enum LargeVarRawSizeCodex {
        Lead0_Big("7"), // First Selector Character for all ls == 0 codes
        Lead1_Big("8"), // First Selector Character for all ls == 1 codes
        Lead2_Big("9"); // First Selector Character for all ls == 2 codes

        private final String value;
        private static final Map<String, LargeVarRawSizeCodex> largeVarRawSizeCodexFieldMap =
            Arrays.stream(LargeVarRawSizeCodex.values())
                .collect(Collectors.toMap(LargeVarRawSizeCodex::getValue, Function.identity()));
            
        LargeVarRawSizeCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return largeVarRawSizeCodexFieldMap.containsKey(value);
        }

        static LargeVarRawSizeCodex fromLsIndex(int ls) {
            return LargeVarRawSizeCodex.values()[ls];
        }
    }

    @Getter
    public enum MatterCodex {
        Ed25519_Seed("A"), // Ed25519 256 bit random seed for private key
        Ed25519N("B"), // Ed25519 verification key non-transferable, basic derivation.
        X25519("C"), // X25519 public encryption key, converted from Ed25519 or Ed25519N.
        Ed25519("D"), // Ed25519 verification key basic derivation
        Blake3_256("E"), // Blake3 256 bit digest self-addressing derivation.
        SHA3_256("H"), // SHA3 256 bit digest self-addressing derivation.
        SHA2_256("I"), // SHA2 256 bit digest self-addressing derivation.
        ECDSA_256k1_Seed("J"), // ECDSA secp256k1 256 bit random Seed for private key
        X25519_Private("O"), // X25519 private decryption key converted from Ed25519
        X25519_Cipher_Seed("P"), // X25519 124 char b64 Cipher of 44 char qb64 Seed
        ECDSA_256r1_Seed("Q"), // ECDSA secp256r1 256 bit random Seed for private key
        Salt_128("0A"), // 128 bit random salt or 128 bit number (see Huge)
        Ed25519_Sig("0B"), // Ed25519 signature.
        ECDSA_256k1_Sig("0C"), // ECDSA secp256k1 signature.
        ECDSA_256r1_Sig("0I"), // ECDSA secp256r1 signature.
        StrB64_L0("4A"), // String Base64 Only Lead Size 0
        StrB64_L1("5A"), // String Base64 Only Lead Size 1
        StrB64_L2("6A"), // String Base64 Only Lead Size 2
        ECDSA_256k1N("1AAA"), // ECDSA secp256k1 verification key non-transferable, basic derivation.
        ECDSA_256k1("1AAB"), // ECDSA public verification or encryption key, basic derivation
        X25519_Cipher_Salt("1AAH"), // X25519 100 char b64 Cipher of 24 char qb64 Salt
        ECDSA_256r1N("1AAI"), // ECDSA secp256r1 verification key non-transferable, basic derivation.
        ECDSA_256r1("1AAJ"), // ECDSA secp256r1 verification or encryption key, basic derivation
        StrB64_Big_L0("7AAA"), // String Base64 Only Big Lead Size 0
        StrB64_Big_L1("8AAA"), // String Base64 Only Big Lead Size 1
        StrB64_Big_L2("9AAA"); // String Base64 Only Big Lead Size 2

        private final String value;
        private static final Map<String, MatterCodex> matterCodexFieldMap =
            Arrays.stream(MatterCodex.values())
                .collect(Collectors.toMap(MatterCodex::getValue, Function.identity()));
            
        MatterCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return matterCodexFieldMap.containsKey(value);
        }

        static MatterCodex fromValue(String value) {
            return matterCodexFieldMap.get(value);
        }
    }

    @Getter
    public enum NonTransCodex {
        Ed25519N("B"), // Ed25519 verification key non-transferable, basic derivation.
        ECDSA_256k1N("1AAA"), // ECDSA secp256k1 verification key non-transferable, basic derivation.
        Ed448N("1AAC"), // Ed448 non-transferable prefix public signing verification key. Basic derivation.
        ECDSA_256r1N("1AAI"); // ECDSA secp256r1 verification key non-transferable, basic derivation.

        private final String value;
        private static final Map<String, NonTransCodex> nonTransCodexFieldMap =
            Arrays.stream(NonTransCodex.values())
                .collect(Collectors.toMap(NonTransCodex::getValue, Function.identity()));
            
        NonTransCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return nonTransCodexFieldMap.containsKey(value);
        }
    }

    @Getter
    public enum NumCodex {
        Short("M"), // Short 2 byte b2 number
        Long("0H"), // Long 4 byte b2 number
        Big("N"), // Big 8 byte b2 number
        Huge("0A"); // Huge 16 byte b2 number (same as Salt_128)

        private final String value;
        private static final Map<String, NumCodex> numCodexFieldMap =
            Arrays.stream(NumCodex.values())
                .collect(Collectors.toMap(NumCodex::getValue, Function.identity()));
            
        NumCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return numCodexFieldMap.containsKey(value);
        }
    }
}