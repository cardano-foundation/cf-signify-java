package org.cardanofoundation.signify.end;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class SignageTest {
    List<Siger> sigers = new ArrayList<>();
    List<Cigar> cigars = new ArrayList<>();
    byte[] text;
    String pre;
    String digest;

    @BeforeEach
    void setUp() throws SodiumException {
        final String name = "Hilga";
        final Signer signer0 = createSigner(name + "00");
        final Signer signer1 = createSigner(name + "01");
        final Signer signer2 = createSigner(name + "02");
        final List<Signer> signers = List.of(signer0, signer1, signer2);

        text = "{\"seid\":\"BA89hKezugU2LFKiFVbitoHAxXqJh6HQ8Rn9tH7fxd68\",\"name\":\"wit0\",\"dts\":\"2021-01-01T00:00:00.000000+00:00\",\"scheme\":\"http\",\"host\":\"localhost\",\"port\":8080,\"path\":\"/witness\"}".getBytes();
        for (int i = 0; i < signers.size(); i++) {
            Signer signer = signers.get(i);
            final Cigar cigar = (Cigar) signer.sign(text);
            final Siger siger = (Siger) signer.sign(text, i, false, null);
            sigers.add(siger);
            cigars.add(cigar);
        }

        pre = "EGqHykT1gVyuWxsVW6LUUsz_KtLJGYMi_SrohInwvjC-";
        digest = pre;
    }

    @Test
    @DisplayName("When indexed signatures")
    void testWhenIndexedSignatures() {

        // Can create signature header
        String expectedHeader = String.join(";",
                "indexed=\"?1\"",
                "0=\"AACsufRGYI-sRvS2c0rsOueSoSRtrjODaf48DYLJbLvvD8aHe7b2sWGebZ-y9ichhsxMF3Hhn-3LYSKIrnmH3oIN\"",
                "1=\"ABDs7m2-h5l7vpjYtbFXtksicpZK5Oclm43EOkE2xoQOfr08doj73VrlKZOKNfJmRumD3tfaiFFgVZqPgiHuFVoA\"",
                "2=\"ACDVOy2LvGgFINUneL4iwA55ypJR6vDpLLbdleEsiANmFazwZARypJMiw9vu2Iu0oL7XCUiUT4JncU8P3HdIp40F\""
        );

        Signage signage = new Signage(sigers);
        final Map<String, String> header = Signage.signature(List.of(signage));
        assertTrue(header.containsKey("Signature"));
        assertEquals(expectedHeader, header.get("Signature"));

        // Can parse signature header
        final List<Signage> signages = Signage.designature(expectedHeader);
        assertEquals(1, signages.size());
        signage = signages.get(0);

        assertInstanceOf(Map.class, signage.getMarkers());
        assertEquals(3, ((Map<String, Object>) signage.getMarkers()).size());
        ((Map<String, Object>) signage.getMarkers()).forEach((tag, marker) -> {
            assertInstanceOf(Siger.class, marker);
            int idx = Integer.parseInt(tag);
            Siger siger = sigers.get(idx);
            assertEquals(siger.getQb64(), ((Siger) marker).getQb64());
            assertEquals(idx, siger.getIndex());
        });
    }

    @Test
    @DisplayName("When named signatures")
    void testWhenNamedSignatures() {
        // Can create signature header
        String expectedHeader = String.join(";",
                "indexed=\"?1\"",
                "siger0=\"AACsufRGYI-sRvS2c0rsOueSoSRtrjODaf48DYLJbLvvD8aHe7b2sWGebZ-y9ichhsxMF3Hhn-3LYSKIrnmH3oIN\"",
                "siger1=\"ABDs7m2-h5l7vpjYtbFXtksicpZK5Oclm43EOkE2xoQOfr08doj73VrlKZOKNfJmRumD3tfaiFFgVZqPgiHuFVoA\"",
                "siger2=\"ACDVOy2LvGgFINUneL4iwA55ypJR6vDpLLbdleEsiANmFazwZARypJMiw9vu2Iu0oL7XCUiUT4JncU8P3HdIp40F\""
        );
        final Map<String, Siger> markers = new LinkedHashMap<>();
        IntStream.range(0, sigers.size()).forEach(i -> markers.put("siger" + i, sigers.get(i)));

        Signage signage = new Signage(markers);
        final Map<String, String> header = Signage.signature(List.of(signage));
        assertTrue(header.containsKey("Signature"));
        assertEquals(expectedHeader, header.get("Signature"));

        // Can parse signature header
        final List<Signage> signages = Signage.designature(expectedHeader);
        assertEquals(1, signages.size());
        signage = signages.get(0);

        assertInstanceOf(Map.class, signage.getMarkers());
        assertEquals(3, ((Map<String, Object>) signage.getMarkers()).size());
        ((Map<String, Object>) signage.getMarkers()).forEach((tag, marker) -> {
            Siger siger = markers.get(tag);

            assertInstanceOf(Siger.class, marker);
            assertNotNull(siger);
            assertEquals(siger.getQb64(), ((Siger) marker).getQb64());
        });
    }

    @Test
    @DisplayName("When indexed CESR signatures")
    void testWhenIndexedCESRSignatures() {
        String expectedHeader = String.join(";",
                "indexed=\"?1\"",
                "signer=\"EGqHykT1gVyuWxsVW6LUUsz_KtLJGYMi_SrohInwvjC-\"",
                "ordinal=\"0\"",
                "digest=\"EGqHykT1gVyuWxsVW6LUUsz_KtLJGYMi_SrohInwvjC-\"",
                "kind=\"CESR\"",
                "0=\"AACsufRGYI-sRvS2c0rsOueSoSRtrjODaf48DYLJbLvvD8aHe7b2sWGebZ-y9ichhsxMF3Hhn-3LYSKIrnmH3oIN\"",
                "1=\"ABDs7m2-h5l7vpjYtbFXtksicpZK5Oclm43EOkE2xoQOfr08doj73VrlKZOKNfJmRumD3tfaiFFgVZqPgiHuFVoA\"",
                "2=\"ACDVOy2LvGgFINUneL4iwA55ypJR6vDpLLbdleEsiANmFazwZARypJMiw9vu2Iu0oL7XCUiUT4JncU8P3HdIp40F\""
        );

        // Can create signature header

        Signage signage = new Signage(sigers, true, pre, "0", digest, "CESR");
        final Map<String, String> header = Signage.signature(List.of(signage));
        assertTrue(header.containsKey("Signature"));
        assertEquals(expectedHeader, header.get("Signature"));

        // Can parse signature header
        final List<Signage> signages = Signage.designature(expectedHeader);
        assertEquals(1, signages.size());
        signage = signages.get(0);
        assertTrue(signage.getIndexed());
        assertEquals(pre, signage.getSigner());
        assertEquals("0", signage.getOrdinal());
        assertEquals(digest, signage.getDigest());
        assertEquals("CESR", signage.getKind());

        assertInstanceOf(Map.class, signage.getMarkers());
        assertEquals(3, ((Map<String, Object>) signage.getMarkers()).size());
        ((Map<String, Object>) signage.getMarkers()).forEach((tag, marker) -> {
            assertInstanceOf(Siger.class, marker);
            int idx = Integer.parseInt(tag);
            Siger siger = sigers.get(idx);
            assertEquals(siger.getQb64(), ((Siger) marker).getQb64());
            assertEquals(idx, siger.getIndex());
        });
    }

    @Test
    @DisplayName("When non-indexed CESR signatures")
    void testWhenNonIndexedCESRSignatures() {
        String expectedHeader = String.join(";",
                "indexed=\"?0\"",
                "DAi2TaRNVtGmV8eSUvqHIBzTzIgrQi57vKzw5Svmy7jw=\"0BCsufRGYI-sRvS2c0rsOueSoSRtrjODaf48DYLJbLvvD8aHe7b2sWGebZ-y9ichhsxMF3Hhn-3LYSKIrnmH3oIN\"",
                "DNK2KFnL0jUGlmvZHRse7HwNGVdtkM-ORvTZfFw7mDbt=\"0BDs7m2-h5l7vpjYtbFXtksicpZK5Oclm43EOkE2xoQOfr08doj73VrlKZOKNfJmRumD3tfaiFFgVZqPgiHuFVoA\"",
                "DDvIoIYqeuXJ4Zb8e2luWfjPTg4FeIzfHzIO8lC56WjD=\"0BDVOy2LvGgFINUneL4iwA55ypJR6vDpLLbdleEsiANmFazwZARypJMiw9vu2Iu0oL7XCUiUT4JncU8P3HdIp40F\""
        );

        // Can create signature header
        Signage signage = new Signage(cigars);
        final Map<String, String> header = Signage.signature(List.of(signage));
        assertTrue(header.containsKey("Signature"));
        assertEquals(expectedHeader, header.get("Signature"));

        // Can parse signature header   
        final List<Signage> signages = Signage.designature(expectedHeader);
        assertEquals(1, signages.size());
        signage = signages.get(0);
        assertFalse(signage.getIndexed());
        assertInstanceOf(Map.class, signage.getMarkers());
        assertEquals(3, ((Map<String, Object>) signage.getMarkers()).size());

        ((Map<String, Object>) signage.getMarkers()).forEach((tag, marker) -> {
            assertInstanceOf(Cigar.class, marker);
            Cigar cigar = cigars.stream()
                    .filter(c -> c.getVerfer() != null && c.getVerfer().getQb64().equals(tag))
                    .findFirst()
                    .orElse(null);

            assertNotNull(cigar);
            assertEquals(cigar.getQb64(), ((Cigar) marker).getQb64());
            assertEquals(tag, cigar.getVerfer().getQb64());
        });
    }

    @Test
    @DisplayName("Combined headers")
    void testCombinedHeaders() {
        String expectedHeader = String.join(";",
                "indexed=\"?1\"",
                "signer=\"EGqHykT1gVyuWxsVW6LUUsz_KtLJGYMi_SrohInwvjC-\"",
                "kind=\"CESR\"",
                "0=\"AACsufRGYI-sRvS2c0rsOueSoSRtrjODaf48DYLJbLvvD8aHe7b2sWGebZ-y9ichhsxMF3Hhn-3LYSKIrnmH3oIN\"",
                "1=\"ABDs7m2-h5l7vpjYtbFXtksicpZK5Oclm43EOkE2xoQOfr08doj73VrlKZOKNfJmRumD3tfaiFFgVZqPgiHuFVoA\"",
                "2=\"ACDVOy2LvGgFINUneL4iwA55ypJR6vDpLLbdleEsiANmFazwZARypJMiw9vu2Iu0oL7XCUiUT4JncU8P3HdIp40F\",indexed=\"?0\"",
                "signer=\"EGqHykT1gVyuWxsVW6LUUsz_KtLJGYMi_SrohInwvjC-\"",
                "kind=\"CESR\"",
                "DAi2TaRNVtGmV8eSUvqHIBzTzIgrQi57vKzw5Svmy7jw=\"0BCsufRGYI-sRvS2c0rsOueSoSRtrjODaf48DYLJbLvvD8aHe7b2sWGebZ-y9ichhsxMF3Hhn-3LYSKIrnmH3oIN\"",
                "DNK2KFnL0jUGlmvZHRse7HwNGVdtkM-ORvTZfFw7mDbt=\"0BDs7m2-h5l7vpjYtbFXtksicpZK5Oclm43EOkE2xoQOfr08doj73VrlKZOKNfJmRumD3tfaiFFgVZqPgiHuFVoA\"",
                "DDvIoIYqeuXJ4Zb8e2luWfjPTg4FeIzfHzIO8lC56WjD=\"0BDVOy2LvGgFINUneL4iwA55ypJR6vDpLLbdleEsiANmFazwZARypJMiw9vu2Iu0oL7XCUiUT4JncU8P3HdIp40F\""
        );

        // Can create signature header
        Signage signage0 = new Signage(sigers, true, pre, null, null, "CESR");
        Signage signage1 = new Signage(cigars, false, pre, null, null, "CESR");
        final Map<String, String> header = Signage.signature(List.of(signage0, signage1));
        assertTrue(header.containsKey("Signature"));
        assertEquals(expectedHeader, header.get("Signature"));

        // Can parse signature header
        final List<Signage> signages = Signage.designature(expectedHeader);
        assertEquals(2, signages.size());
        signage0 = signages.get(0);

        assertTrue(signage0.getIndexed());
        assertEquals(pre, signage0.getSigner());
        assertEquals("CESR", signage0.getKind());
        assertInstanceOf(Map.class, signage0.getMarkers());
        assertEquals(3, ((Map<String, Object>) signage0.getMarkers()).size());
        ((Map<String, Object>) signage0.getMarkers()).forEach((tag, marker) -> {
            assertInstanceOf(Siger.class, marker);
            int idx = Integer.parseInt(tag);
            Siger siger = sigers.get(idx);
            assertEquals(siger.getQb64(), ((Siger) marker).getQb64());
            assertEquals(idx, siger.getIndex());
        });

        signage1 = signages.get(1);
        assertFalse(signage1.getIndexed());
        assertEquals(pre, signage1.getSigner());
        assertEquals("CESR", signage1.getKind());
        assertInstanceOf(Map.class, signage1.getMarkers());
        assertEquals(3, ((Map<String, Object>) signage1.getMarkers()).size());
        ((Map<String, Object>) signage1.getMarkers()).forEach((tag, marker) -> {
            assertInstanceOf(Cigar.class, marker);
            Cigar cigar = cigars.stream()
                    .filter(c -> c.getVerfer() != null && c.getVerfer().getQb64().equals(tag))
                    .findFirst()
                    .orElse(null);

            assertNotNull(cigar);
            assertEquals(cigar.getQb64(), ((Cigar) marker).getQb64());
            assertEquals(tag, cigar.getVerfer().getQb64());
        });
    }

    private Signer createSigner(String name) throws SodiumException {
        final boolean temp = true;
        final byte[] raw = "0123456789abcdef".getBytes();
        final Salter salter = new Salter(RawArgs.builder().raw(raw).build());
        return salter.signer(Codex.MatterCodex.Ed25519_Seed.getValue(),
                true,
                name,
                Salter.Tier.low,
                temp);
    }
}