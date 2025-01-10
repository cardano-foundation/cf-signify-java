package org.cardanofoundation.signify.cesr;

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

        public static boolean has(String value) {
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

        public static boolean has(String value) {
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

    @Getter
    public enum IndexerCodex {
        Ed25519_Sig("A"),             // Ed25519 sig appears same in both lists if any
        Ed25519_Crt_Sig("B"),         // Ed25519 sig appears in current list only
        ECDSA_256k1_Sig("C"),         // ECDSA secp256k1 sig appears same in both lists if any
        ECDSA_256k1_Crt_Sig("D"),     // ECDSA secp256k1 sig appears in current list
        ECDSA_256r1_Sig("E"),         // ECDSA secp256r1 sig appears same in both lists if any
        ECDSA_256r1_Crt_Sig("F"),     // ECDSA secp256r1 sig appears in current list
        Ed448_Sig("0A"),              // Ed448 signature appears in both lists
        Ed448_Crt_Sig("0B"),          // Ed448 signature appears in current list only
        Ed25519_Big_Sig("2A"),        // Ed25519 sig appears in both lists
        Ed25519_Big_Crt_Sig("2B"),    // Ed25519 sig appears in current list only
        ECDSA_256k1_Big_Sig("2C"),    // ECDSA secp256k1 sig appears in both lists
        ECDSA_256k1_Big_Crt_Sig("2D"), // ECDSA secp256k1 sig appears in current list only
        ECDSA_256r1_Big_Sig("2E"),    // ECDSA secp256r1 sig appears in both lists
        ECDSA_256r1_Big_Crt_Sig("2F"), // ECDSA secp256r1 sig appears in current list only
        Ed448_Big_Sig("3A"),          // Ed448 signature appears in both lists
        Ed448_Big_Crt_Sig("3B");      // Ed448 signature appears in current list only

        private final String value;
        private static final Map<String, IndexerCodex> indexerCodexFieldMap =
            Arrays.stream(IndexerCodex.values())
                .collect(Collectors.toMap(IndexerCodex::getValue, Function.identity()));

        IndexerCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return indexerCodexFieldMap.containsKey(value);
        }
    }

    @Getter
    public enum IndexedSigCodex {
        Ed25519_Sig("A"),             // Ed25519 sig appears same in both lists if any
        Ed25519_Crt_Sig("B"),         // Ed25519 sig appears in current list only
        ECDSA_256k1_Sig("C"),         // ECDSA secp256k1 sig appears same in both lists if any
        ECDSA_256k1_Crt_Sig("D"),     // ECDSA secp256k1 sig appears in current list
        ECDSA_256r1_Sig("E"),         // ECDSA secp256r1 sig appears same in both lists if any
        ECDSA_256r1_Crt_Sig("F"),     // ECDSA secp256r1 sig appears in current list
        Ed448_Sig("0A"),              // Ed448 signature appears in both lists
        Ed448_Crt_Sig("0B"),          // Ed448 signature appears in current list only
        Ed25519_Big_Sig("2A"),        // Ed25519 sig appears in both lists
        Ed25519_Big_Crt_Sig("2B"),    // Ed25519 sig appears in current list only
        ECDSA_256k1_Big_Sig("2C"),    // ECDSA secp256k1 sig appears in both lists
        ECDSA_256k1_Big_Crt_Sig("2D"), // ECDSA secp256k1 sig appears in current list only
        ECDSA_256r1_Big_Sig("2E"),    // ECDSA secp256r1 sig appears in both lists
        ECDSA_256r1_Big_Crt_Sig("2F"), // ECDSA secp256r1 sig appears in current list only
        Ed448_Big_Sig("3A"),          // Ed448 signature appears in both lists
        Ed448_Big_Crt_Sig("3B");      // Ed448 signature appears in current list only

        private final String value;
        private static final Map<String, IndexedSigCodex> indexedSigCodexFieldMap =
            Arrays.stream(IndexedSigCodex.values())
                .collect(Collectors.toMap(IndexedSigCodex::getValue, Function.identity()));

        IndexedSigCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return indexedSigCodexFieldMap.containsKey(value);
        }
    }

    @Getter
    public enum IndexedCurrentSigCodex {
        Ed25519_Crt_Sig("B"),         // Ed25519 sig appears in current list only
        ECDSA_256k1_Crt_Sig("D"),     // ECDSA secp256k1 sig appears in current list only
        ECDSA_256r1_Crt_Sig("F"),     // ECDSA secp256r1 sig appears in current list
        Ed448_Crt_Sig("0B"),          // Ed448 signature appears in current list only
        Ed25519_Big_Crt_Sig("2B"),    // Ed25519 sig appears in current list only
        ECDSA_256k1_Big_Crt_Sig("2D"), // ECDSA secp256k1 sig appears in current list only
        ECDSA_256r1_Big_Crt_Sig("2F"), // ECDSA secp256r1 sig appears in current list only
        Ed448_Big_Crt_Sig("3B");      // Ed448 signature appears in current list only

        private final String value;
        private static final Map<String, IndexedCurrentSigCodex> indexedCurrentSigCodexFieldMap =
            Arrays.stream(IndexedCurrentSigCodex.values())
                .collect(Collectors.toMap(IndexedCurrentSigCodex::getValue, Function.identity()));

        IndexedCurrentSigCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return indexedCurrentSigCodexFieldMap.containsKey(value);
        }
    }

    @Getter
    public enum IndexedBothSigCodex {
        Ed25519_Sig("A"),          // Ed25519 sig appears same in both lists if any
        ECDSA_256k1_Sig("C"),      // ECDSA secp256k1 sig appears same in both lists if any
        Ed448_Sig("0A"),           // Ed448 signature appears in both lists
        Ed25519_Big_Sig("2A"),     // Ed25519 sig appears in both lists
        ECDSA_256k1_Big_Sig("2C"), // ECDSA secp256k1 sig appears in both lists
        Ed448_Big_Sig("3A");       // Ed448 signature appears in both lists

        private final String value;
        private static final Map<String, IndexedBothSigCodex> indexedBothSigCodexFieldMap =
            Arrays.stream(IndexedBothSigCodex.values())
                .collect(Collectors.toMap(IndexedBothSigCodex::getValue, Function.identity()));

        IndexedBothSigCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return indexedBothSigCodexFieldMap.containsKey(value);
        }
    }

    @Getter
    public enum CounterCodex {
        ControllerIdxSigs("-A"),            // Qualified Base64 Indexed Signature
        WitnessIdxSigs("-B"),               // Qualified Base64 Indexed Signature
        NonTransReceiptCouples("-C"),       // Composed Base64 Couple, pre+cig
        TransReceiptQuadruples("-D"),       // Composed Base64 Quadruple, pre+snu+dig+sig
        FirstSeenReplayCouples("-E"),       // Composed Base64 Couple, fnu+dts
        TransIdxSigGroups("-F"),            // Composed Base64 Group, pre+snu+dig+ControllerIdxSigs group
        SealSourceCouples("-G"),            // Composed Base64 couple, snu+dig of given delegators or issuers event
        TransLastIdxSigGroups("-H"),        // Composed Base64 Group, pre+ControllerIdxSigs group
        SealSourceTriples("-I"),            // Composed Base64 triple, pre+snu+dig of anchoring source event
        SadPathSig("-J"),                   // Composed Base64 Group path+TransIdxSigGroup of SAID of content
        SadPathSigGroup("-K"),              // Composed Base64 Group, root(path)+SaidPathCouples
        PathedMaterialQuadlets("-L"),       // Composed Grouped Pathed Material Quadlet (4 char each)
        AttachedMaterialQuadlets("-V"),     // Composed Grouped Attached Material Quadlet (4 char each)
        BigAttachedMaterialQuadlets("-0V"), // Composed Grouped Attached Material Quadlet (4 char each)
        KERIProtocolStack("--AAA");         // KERI ACDC Protocol Stack CESR Version

        private final String value;
        private static final Map<String, CounterCodex> counterCodexFieldMap =
            Arrays.stream(CounterCodex.values())
                .collect(Collectors.toMap(CounterCodex::getValue, Function.identity()));

        CounterCodex(String value) {
            this.value = value;
        }

        static boolean has(String value) {
            return counterCodexFieldMap.containsKey(value);
        }
    }
}