package org.cardanofoundation.signify.core;

import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.args.RotateArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.DigestException;
import java.util.List;
import java.util.Map;

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
        assertEquals(expectedRaw, serder.getRaw());

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
        assertEquals(serder0.getRaw(), "{\"v\":\"KERI10JSON00012b_\",\"t\":\"icp\",\"d\":\"EAKCxMOuoRzREVHsHCkLilBrUXTvyenBiuM2QtV8BB0C\",\"i\":\"EAKCxMOuoRzREVHsHCkLilBrUXTvyenBiuM2QtV8BB0C\",\"s\":\"0\",\"kt\":\"1\",\"k\":[\"DFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH\"],\"nt\":\"1\",\"n\":[\"EIf-ENw7PrM52w4H-S7NGU2qVIfraXVIlV9hEAaMHg7W\"],\"bt\":\"0\",\"b\":[],\"c\":[],\"a\":[]}");

        // (b'\x83B~\x04\x94\xe3\xceUQy\x11f\x0c\x93]\x1e\xbf\xacQ\xb5\xd6Y^\xa2E\xfa\x015\x98Y\xdd\xe8')
        seed1 = new byte[] {
                (byte)131, 66, 126, 4, (byte)148, (byte)227, (byte)206, 85, 81, 121, 17, 102, 12, (byte)147, 93,
                30, (byte)191, (byte)172, 81, (byte)181, (byte)214, 89, 94, (byte)162, 69, (byte)250, 1, 53, (byte)152, 89,
                (byte)221, (byte)232
        };

        signer1 = new Signer(RawArgs.builder().raw(seed1).build()); // next signing keypair transferable is default
        assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer1.getCode());
        assertEquals(MatterCodex.Ed25519.getValue(), signer1.getVerfer().getCode());

        // compute nxt digest
        nxt1 = List.of(new Diger(new RawArgs(), signer1.getVerfer().getQb64b()).getQb64()); // default sith is 1
        assertEquals("EIf-ENw7PrM52w4H-S7NGU2qVIfraXVIlV9hEAaMHg7W", nxt1.get(0));

        inceptArgs = InceptArgs.builder()
                .keys(keys0)
                .ndigs(nxt1)
                .code(MatterCodex.Blake3_256.getValue())
                .intive(true)
                .build();
        serder0 = Eventing.incept(inceptArgs);

        assertEquals(CoreUtil.Ilks.ICP.getValue(), serder0.getKed().get("t"));
        assertEquals("EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL", serder0.getKed().get("d"));
        assertEquals(serder0.getKed().get("d"), serder0.getKed().get("i"));
        assertEquals("0", serder0.getKed().get("s"));
        assertEquals(1, serder0.getKed().get("kt"));
        assertEquals(1, serder0.getKed().get("nt"));
        assertEquals(Utils.toList(serder0.getKed().get("n")), nxt1);
        assertEquals(BigInteger.ZERO, serder0.getKed().get("bt"));
        assertEquals("{\"v\":\"KERI10JSON000125_\",\"t\":\"icp\",\"d\":\"EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL\",\"i\":\"EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL\",\"s\":\"0\",\"kt\":1,\"k\":[\"DFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH\"],\"nt\":1,\"n\":[\"EIf-ENw7PrM52w4H-S7NGU2qVIfraXVIlV9hEAaMHg7W\"],\"bt\":0,\"b\":[],\"c\":[],\"a\":[]}", serder0.getRaw());

        Siger siger = (Siger) signer0.sign(serder0.getRaw().getBytes(), 0);
        assertEquals("AABB3MJGmBXxSEryNHw3YwZZLRl_6Ws4Me2WFq8PrQ6WlluSOpPqbwXuiG9RvNWZkqeW8A_0VRjokGMVRZ3m-c0I", siger.getQb64());

        String msg = new String(Eventing.messagize(serder0, List.of(siger), null, null, null, false));
        assertEquals("{\"v\":\"KERI10JSON000125_\",\"t\":\"icp\",\"d\":\"EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL\",\"i\":\"EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL\",\"s\":\"0\",\"kt\":1,\"k\":[\"DFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH\"],\"nt\":1,\"n\":[\"EIf-ENw7PrM52w4H-S7NGU2qVIfraXVIlV9hEAaMHg7W\"],\"bt\":0,\"b\":[],\"c\":[],\"a\":[]}-AABAABB3MJGmBXxSEryNHw3YwZZLRl_6Ws4Me2WFq8PrQ6WlluSOpPqbwXuiG9RvNWZkqeW8A_0VRjokGMVRZ3m-c0I", msg);

        List<Object> seal = List.of("SealEvent", Map.of("i", "EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL", "s", "0", "d", "EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL"));
        String msgseal = new String(Eventing.messagize(serder0, List.of(siger), seal, null, null, false));
        assertEquals("{\"v\":\"KERI10JSON000125_\",\"t\":\"icp\",\"d\":\"EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL\",\"i\":\"EIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL\",\"s\":\"0\",\"kt\":1,\"k\":[\"DFs8BBx86uytIM0D2BhsE5rrqVIT8ef8mflpNceHo4XH\"],\"nt\":1,\"n\":[\"EIf-ENw7PrM52w4H-S7NGU2qVIfraXVIlV9hEAaMHg7W\"],\"bt\":0,\"b\":[],\"c\":[],\"a\":[]}-FABEIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL0AAAAAAAAAAAAAAAAAAAAAAAEIflL4H4134zYoRM6ls6Q086RLC_BhfNFh5uk-WxvhsL-AABAABB3MJGmBXxSEryNHw3YwZZLRl_6Ws4Me2WFq8PrQ6WlluSOpPqbwXuiG9RvNWZkqeW8A_0VRjokGMVRZ3m-c0I", msgseal);
    }


    @Test
    void rotateShouldCreateRotationEventWithHexSequenceNumber() throws Exception {
        Signer signer0 = new Signer();
        Signer signer1 = new Signer();
        List<String> keys0 = List.of(signer0.getVerfer().getQb64());
        List<String> ndigs = List.of(new Diger(new RawArgs(), signer1.getVerfer().getQb64b()).getQb64());
        Serder serder = Eventing.incept(InceptArgs.builder().keys(keys0).ndigs(ndigs).build());

        assertEquals("1", createRotation(1, keys0, serder));
        assertEquals("a", createRotation(10, keys0, serder));
        assertEquals("e", createRotation(14, keys0, serder));
        assertEquals("ff", createRotation(255, keys0, serder));
    }

    private String createRotation(int sn, List<String> keys, Serder serder ) throws DigestException {
        return (String) Eventing.rotate(
                RotateArgs.builder()
                        .keys(keys)
                        .pre((String) serder.getKed().get("i"))
                        .ndigs((List<String>) serder.getKed().get("n"))
                        .sn(sn)
                        .isith(Sith.fromInteger(1))
                        .nsith(Sith.fromInteger(1))
                        .build()
        ).getKed().get("s");
    }
}
