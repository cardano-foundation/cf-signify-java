package org.cardanofoundation.signify.core;

import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventingTest {

    @Test
    void inceptShouldCreateInceptionEvents() throws Exception {
        byte[] seed = new byte[] {
            (byte)159, 123, (byte)168, (byte)167, (byte)168, 67, 57, (byte)150, 
            38, (byte)250, (byte)177, (byte)153, (byte)235, (byte)170,
            32, (byte)196, 27, 71, 17, (byte)196, (byte)174, 83, 65, 82, 
            (byte)201, (byte)189, 4, (byte)157, (byte)133,
            41, 126, (byte)147
        };

        Signer signer0 = new Signer(RawArgs.builder().raw(seed).build(), false); // original signing keypair non transferable
        assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer0.getCode());
        assertEquals(MatterCodex.Ed25519N.getValue(), signer0.getVerfer().getCode());
        
        List<String> keys0 = List.of(signer0.getVerfer().getQb64());
        InceptArgs inceptArgs = InceptArgs.builder()
            .keys(keys0)
            .build();
        Serder serder = Eventing.incept(inceptArgs); // default nxt is empty so abandoned
            
        assertEquals(
            "BFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH",
            serder.getKed().get("i")
        );
        assertTrue((Utils.toList(serder.getKed().get("n"))).isEmpty());
        String expectedRaw = """
            {"v":"KERI10JSON0000fd_","t":"icp",\
            "d":"EMW0zK3bagYPO6gx3w7Ua90f-I7x5kGIaI4Xeq9W8_As",\
            "i":"BFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH","s":"0",\
            "kt":"1","k":["BFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH"],\
            "nt":"0","n":[],"bt":"0","b":[],"c":[],"a":[]}""";
//        assertEquals(expectedRaw, serder.getRaw());

        Saider saider = new Saider(
            RawArgs.builder()
                .code(MatterCodex.Blake3_256.getValue())
                .build(),
            serder.getKed());
        assertTrue(saider.verify(serder.getKed()));

        // Test invalid inception parameters
        InceptArgs ndisArgs = InceptArgs.builder()
            .keys(keys0)
            .code(MatterCodex.Ed25519N.getValue())
            .ndigs(List.of("ABCDE"))
            .build();
        assertThrows(RuntimeException.class, () -> Eventing.incept(ndisArgs));

        InceptArgs witsArgs = InceptArgs.builder()
            .keys(keys0)
            .code(MatterCodex.Ed25519N.getValue())
            .wits(List.of("ABCDE"))
            .build();
        assertThrows(RuntimeException.class, () -> Eventing.incept(witsArgs));

        Object i = "ABCDE";
        InceptArgs dataArgs = InceptArgs.builder()
            .keys(keys0)
            .code(MatterCodex.Ed25519N.getValue())
            .data(List.of(i))
            .build();
        assertThrows(RuntimeException.class, () -> Eventing.incept(dataArgs));

        signer0 = new Signer(RawArgs.builder().raw(seed).build()); // original signing keypair transferable default
        assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer0.getCode());
        assertEquals(MatterCodex.Ed25519.getValue(), signer0.getVerfer().getCode());
        
        keys0 = List.of(signer0.getVerfer().getQb64());
        serder = Eventing.incept(InceptArgs.builder().keys(keys0).build());
            
        assertEquals(
            "DFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH",
            serder.getKed().get("i")
        );

        // Test with next key digest
        byte[] seed1 = new byte[] {
            (byte)131, 66, 126, 4, (byte)148, (byte)227, (byte)206, 85, 81, 121, 17, 102, 12, (byte)147, 93,
            30, (byte)191, (byte)172, 81, (byte)181, (byte)214, 89, 94, (byte)162, 69, (byte)250, 1, 53, (byte)152, 89,
            (byte)221, (byte)232
        };

        // next signing keypair transferable is default
        Signer signer1 = new Signer(RawArgs.builder().raw(seed1).build());
        assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer1.getCode());
        assertEquals(MatterCodex.Ed25519.getValue(), signer1.getVerfer().getCode());

        // compute nxt digest
        List<String> nxt1 = List.of(new Diger(new RawArgs(), signer1.getVerfer().getQb64b()).getQb64()); // default sith is 1
        assertEquals("EIf-ENw7PrM52w4H-S7NGU2qVIfraXVIlV9hEAaMHg7W", nxt1.getFirst());

        Serder serder0 = Eventing.incept(
            InceptArgs.builder()
                .keys(keys0)
                .ndigs(nxt1)
                .code(MatterCodex.Blake3_256.getValue())
                .build()
        );
        assertEquals(CoreUtil.Ilks.ICP.getValue(), serder0.getKed().get("t"));
        assertEquals("EAKCxMOuoRzREVHsHCkLilBrUXTvyenBiuM2QtV8BB0C", serder0.getKed().get("d"));
        assertEquals(serder0.getKed().get("d"), serder0.getKed().get("i"));
        assertEquals(serder0.getKed().get("s"), "0");
        assertEquals(serder0.getKed().get("kt"), "1");
        assertEquals(serder0.getKed().get("nt"), "1");
        assertEquals(Utils.toList(serder0.getKed().get("n")), nxt1);
        assertEquals(serder0.getKed().get("bt"), "0");

    }
}
