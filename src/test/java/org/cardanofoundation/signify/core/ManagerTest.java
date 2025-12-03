package org.cardanofoundation.signify.core;

import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.generated.keria.model.Identifier;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.RandyKeyState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import static org.junit.jupiter.api.Assertions.*;

import java.security.DigestException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class ManagerTest {

    String ser = "{\"vs\":\"KERI10JSON0000fb_\",\"pre\":\"EvEnZMhz52iTrJU8qKwtDxzmypyosgG" +
            "70m6LIjkiCdoI\",\"sn\":\"0\",\"ilk\":\"icp\",\"sith\":\"1\",\"keys\":[\"DSuhyBcP" +
            "ZEZLK-fcw5tzHn2N46wRCG_ZOoeKtWTOunRA\"],\"nxt\":\"EPYuj8mq_PYYsoBKkz" +
            "X1kxSPGYBWaIya3slgCOyOtlqU\",\"toad\":\"0\",\"wits\":[],\"cnfg\":[]}-AABA" +
            "ApYcYd1cppVg7Inh2YCslWKhUwh59TrPpIoqWxN2A38NCbTljvmBPBjSGIFDBNOv" +
            "VjHpdZlty3Hgk6ilF8pVpAQ";

    @Test
    @DisplayName("should create sets of random signers")
    void testRandyCreator() throws LibsodiumException {
        Manager.RandyCreator randy = new Manager.RandyCreator();

        // test default arguments
        Manager.Keys keys = randy.create();
        assertEquals(1, keys.getSigners().size());
        assertEquals(44, keys.getSigners().getFirst().getQb64().length());
        assertEquals(MatterCodex.Ed25519_Seed.getValue(), keys.getSigners().getFirst().getCode());
        assertTrue(keys.getSigners().getFirst().isTransferable());

        // Create 5 with default code
        keys = randy.create(null, 5);
        assertEquals(5, keys.getSigners().size());
        keys.getSigners().forEach(signer -> {
            assertEquals(44, signer.getQb64().length());
            assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer.getCode());
            assertTrue(signer.isTransferable());
        });

        // Create 3 with specified codes (the only one we support)
        List<String> codes = Arrays.asList(
            MatterCodex.Ed25519_Seed.getValue(),
            MatterCodex.Ed25519_Seed.getValue(),
            MatterCodex.Ed25519_Seed.getValue()
        );
        keys = randy.create(codes);
        assertEquals(3, keys.getSigners().size());
        keys.getSigners().forEach(signer -> {
            assertEquals(44, signer.getQb64().length());
            assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer.getCode());
            assertTrue(signer.isTransferable());
        });
    }

    @Test
    @DisplayName("should create sets of salty signers")
    void testSaltyCreator() throws LibsodiumException {
        Manager manager = new Manager();
        Manager.SaltyCreator salty = new Manager.SaltyCreator();

        // Test default arguments
        assertEquals(salty.salter.getCode(), MatterCodex.Salt_128.getValue());
        assertEquals(salty.salt(), salty.salter.getQb64());
        assertEquals(salty.stem(), "");
        assertEquals(salty.tier(), salty.salter.getTier());

        var keys = salty.create();
        assertEquals(keys.getSigners().size(), 1);
        assertEquals(keys.getSigners().getFirst().getQb64().length(), 44);
        assertEquals(keys.getSigners().getFirst().getCode(), MatterCodex.Ed25519_Seed.getValue());
        assertTrue(keys.getSigners().getFirst().isTransferable());

        keys = salty.create(null, 2, MatterCodex.Ed25519_Seed.getValue(), false, 0, 0, 0, false);
        assertEquals(keys.getSigners().size(), 2);
        keys.getSigners().forEach(signer -> {
            assertEquals(44, signer.getQb64().length());
            assertEquals(MatterCodex.Ed25519_Seed.getValue(), signer.getCode());
            assertEquals(MatterCodex.Ed25519N.getValue(), signer.getVerfer().getCode());
        });

        final String raw = "0123456789abcdef";

        final RawArgs rawArgs = RawArgs.builder()
                .code(MatterCodex.Salt_128.getValue())
                .raw(raw.getBytes())
                .build();

        final Salter salter = new Salter(rawArgs);
        final String salt = salter.getQb64();
        assertEquals(salter.getQb64(), "0AAwMTIzNDU2Nzg5YWJjZGVm");
        salty = new Manager.SaltyCreator(salt, null, null);
        assertEquals(salty.salter.getCode(), MatterCodex.Salt_128.getValue());
        assertArrayEquals(salty.salter.getRaw(), raw.getBytes());
        assertEquals(salty.salter.getQb64(), salt);
        assertEquals(salty.salt(), salty.salter.getQb64());
        assertEquals(salty.stem(), "");
        assertEquals(salty.tier(), salty.salter.getTier());

        keys = salty.create();
        assertEquals(keys.getSigners().size(), 1);
        assertEquals(keys.getSigners().getFirst().getCode(), MatterCodex.Ed25519_Seed.getValue());
        assertEquals(keys.getSigners().getFirst().getQb64(), "AO0hmkIVsjCoJY1oUe3-QqHlMBVIhFX1tQfN_8SPKiNF");
        assertEquals(keys.getSigners().getFirst().getVerfer().getCode(), MatterCodex.Ed25519.getValue());
        assertEquals(keys.getSigners().getFirst().getVerfer().getQb64(), "DHHneREQ1eZyQNc5nEsQYx1FqFVL1OTXmvmatTE77Cfe");

        keys = salty.create(null, 1, MatterCodex.Ed25519_Seed.getValue(), false, 0, 0, 0, true);
        assertEquals(keys.getSigners().size(), 1);
        assertEquals(keys.getSigners().getFirst().getCode(), MatterCodex.Ed25519_Seed.getValue());
        assertEquals(keys.getSigners().getFirst().getQb64(), "AOVkNmL_dZ5pjvp-_nS5EJbs0xe32MODcOUOym-0aCBL");
        assertEquals(keys.getSigners().getFirst().getVerfer().getCode(), MatterCodex.Ed25519N.getValue());
        assertEquals(keys.getSigners().getFirst().getVerfer().getQb64(), "BB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
    }

    @Test
    @DisplayName("should create Randy or Salty creator")
    void testManagerCreator() {
        Manager manager = new Manager();

        final String raw = "0123456789abcdef";
        final RawArgs rawArgs = RawArgs.builder()
                .code(MatterCodex.Salt_128.getValue())
                .raw(raw.getBytes())
                .build();

        final Salter salter = new Salter(rawArgs);
        final String salt = salter.getQb64();

        Manager.Creator creator = new Manager.Creatory(Manager.Algos.salty).make(salt);
        assertInstanceOf(Manager.SaltyCreator.class, creator);
        assertEquals(((Manager.SaltyCreator) creator).salter.getQb64(), salt);

        creator = new Manager.Creatory().make(salt);
        assertInstanceOf(Manager.SaltyCreator.class, creator);
        assertEquals(((Manager.SaltyCreator) creator).salter.getQb64(), salt);

        creator = new Manager.Creatory(Manager.Algos.randy).make(salt);
        assertInstanceOf(Manager.RandyCreator.class, creator);
    }

    @Test
    @DisplayName("should manage key pairs for identifiers")
    void testManager_shouldManageKeyPairsForIdentifiers() throws DigestException, LibsodiumException {
        String raw = "0123456789abcdef";
        Salter salter = new Salter(RawArgs.builder().raw(raw.getBytes()).build());
        String salt = salter.getQb64();
        assertEquals(salt, "0AAwMTIzNDU2Nzg5YWJjZGVm");
        String stem = "red";

        // Create a randy Manager without encryption should raise an exception
        assertThrows(Exception.class, () -> {
            Manager.ManagerArgs args = Manager.ManagerArgs.builder()
                    .algo(Manager.Algos.randy)
                    .build();
            Manager manager = new Manager(args);
        });

        // cryptseed0 = b('h,#|\x8ap"\x12\xc43t2\xa6\xe1\x18\x19\xf0f2,y\xc4\xc21@\xf5@\x15.\xa2\x1a\xcf')
        byte[] cryptseed0 = new byte[]{
                104, 44, 35, 124, -118, 112, 34, 18, -60, 51, 116, 50, -90, -31, 24,
                25, -16, 102, 50, 44, 121, -60, -62, 49, 64, -11, 64, 21, 46, -94,
                26, -49
        };

        Signer cryptsigner0 = new Signer(RawArgs.builder()
                .raw(cryptseed0)
                .code(MatterCodex.Ed25519_Seed.getValue())
                .build(), false);

        String seed0 = cryptsigner0.getQb64();
        byte[] seed0b = cryptsigner0.getQb64b();
        String aeid0 = cryptsigner0.getVerfer().getQb64();

        assertEquals(aeid0, "BCa7mK96FwxkU0TdF54Yqg3qBDXUWpOhQ_Mtr7E77yZB");
        Decrypter decrypter0 = new Decrypter(new RawArgs(), seed0b);
        Encrypter encrypter0 = new Encrypter(new RawArgs(), aeid0.getBytes());

        assertTrue(encrypter0.verifySeed(seed0b));

        // cryptseed1 = (b"\x89\xfe{\xd9'\xa7\xb3\x89#\x19\xbec\xee\xed\xc0\xf9\x97\xd0\x8f9\x1dyNI"
        //                b'I\x98\xbd\xa4\xf6\xfe\xbb
        byte[] cryptseed1 = new byte[]{
                (byte) 137, (byte) 254, 123, (byte) 217, 39, (byte) 167, (byte) 179, (byte) 137, 35, 25, (byte) 190, 99, (byte) 238, (byte) 237,
                (byte) 192, (byte) 249, (byte) 151, (byte) 208, (byte) 143, 57, 29, 121, 78, 73, 73, (byte) 152, (byte) 189, (byte) 164,
                (byte) 246, (byte) 254, (byte) 187, 3
        };
        Signer cryptsigner1 = new Signer(RawArgs.builder()
                .raw(cryptseed1)
                .code(MatterCodex.Ed25519_Seed.getValue())
                .build(), false);

        String seed1 = cryptsigner1.getQb64();
        byte[] seed1b = cryptsigner1.getQb64b();
        String aeid1 = cryptsigner1.getVerfer().getQb64();
        assertEquals(aeid1, "BEcOrMrG_7r_NWaLl6h8UJapwIfQWIkjrIPXkCZm2fFM");
        Encrypter encrypter1 = new Encrypter(new RawArgs(), aeid1.getBytes());
        assertTrue(encrypter1.verifySeed(seed1b));

        Manager.ManagerArgs managerArgs = Manager.ManagerArgs.builder()
                .seed(seed0)
                .salter(salter)
                .aeid(aeid0)
                .build();
        Manager manager = new Manager(managerArgs);
        assertEquals(manager.getEncrypter().getQb64(), encrypter0.getQb64());
        assertEquals(manager.getDecrypter().getQb64(), decrypter0.getQb64());
        assertEquals(manager.getSeed(), seed0);
        assertEquals(manager.getAeid(), aeid0);
        assertEquals(manager.getAlgo(), Manager.Algos.salty);
        assertEquals(manager.getSalt(), salt);
        assertEquals(manager.getPidx(), 0);
        assertEquals(manager.getTier(), Salter.Tier.low.name());

        Cipher saltCipher0 = new Cipher(manager.getKs().getGbls("salt"));
        assertEquals(((Matter) saltCipher0.decrypt(null, seed0b)).getQb64(), salt);

        Manager.ManagerInceptArgs managerInceptArgs = Manager.ManagerInceptArgs.builder()
                .salt(salt)
                .temp(true)
                .build();
        Manager.ManagerInceptResult managerInceptResult = manager.incept(managerInceptArgs);

        List<Verfer> verfers = managerInceptResult.verfers();
        List<Diger> digers = managerInceptResult.digers();
        assertEquals(verfers.size(), 1);
        assertEquals(digers.size(), 1);
        assertEquals(manager.getPidx(), 1);

        String spre = verfers.getFirst().getQb64();
        assertEquals(spre, "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");

        Manager.PrePrm pp = manager.getKs().getPrms(spre);
        assertEquals(pp.pidx, 0);
        assertEquals(pp.algo, Manager.Algos.salty);
        assertEquals(((Matter) manager.getDecrypter().decrypt(pp.salt.getBytes(), null)).getQb64(), salt);
        assertEquals(pp.stem, "");
        assertEquals(pp.tier, Salter.Tier.low.name());

        Manager.PreSit ps = manager.getKs().getSits(spre);
        assertEquals(ps.old.pubs, Collections.emptyList());
        assertEquals(ps.new_.pubs.size(), 1);
        assertEquals(ps.new_.pubs.getFirst(), "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");

        assertEquals(ps.new_.ridx, 0);
        assertEquals(ps.new_.kidx, 0);
        assertEquals(ps.nxt.pubs.size(), 1);
        assertEquals(ps.nxt.pubs.getFirst(), "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
        assertEquals(ps.nxt.ridx, 1);
        assertEquals(ps.nxt.kidx, 1);

        List<String> keys = verfers.stream().map(Verfer::getQb64).toList();
        assertEquals(keys, ps.new_.pubs);

        Manager.PubSet pl = manager.getKs().getPubs(Manager.riKey(spre, ps.new_.ridx));
        assertEquals(pl.pubs, ps.new_.pubs);
        pl = manager.getKs().getPubs(Manager.riKey(spre, ps.nxt.ridx));
        assertEquals(pl.pubs, ps.nxt.pubs);

        List<String> digs = digers.stream().map(Diger::getQb64).toList();
        assertEquals(digs.getFirst(), "ENmcKrctbztF36MttN7seUYJqH2IMnkavBgGLR6Mj2-B");

        String oldspre = spre;
        spre = "DCu5o5cxzv1lgMqxMVG3IcCNK4lpFfpMM-9rfkY3XVUc";
        manager.move(oldspre, spre);

        pl = manager.getKs().getPubs(Manager.riKey(spre, ps.new_.ridx));
        assertEquals(pl.pubs, ps.new_.pubs);
        pl = manager.getKs().getPubs(Manager.riKey(spre, ps.nxt.ridx));
        assertEquals(pl.pubs, ps.nxt.pubs);

        byte[] serb = ser.getBytes();
        Manager.SignArgs signArgs = Manager.SignArgs.builder()
                .ser(serb)
                .pubs(ps.new_.pubs)
                .build();
        List<Object> psigers = (List<Object>) manager.sign(signArgs);
        assertEquals(psigers.size(), 1);
        assertInstanceOf(Siger.class, psigers.getFirst());

        signArgs = Manager.SignArgs.builder()
                .ser(serb)
                .verfers(verfers)
                .build();
        List<Object> vsigers = (List<Object>) manager.sign(signArgs);
        List<String> psigs = psigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();
        List<String> vsigs = vsigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();

        assertEquals(psigs, vsigs);
        assertEquals(psigs.getFirst(), "AACRPqO6vdXm1oSSa82rmVVHikf7NdN4JXjOWEk30Ub5JHChL0bW6DzJfA-7VlgLm_B1XR0Z61FweP87bBQpVawI");

        // Test sign with indices
        List<Integer> indices = List.of(3);
        signArgs = Manager.SignArgs.builder()
                .ser(serb)
                .pubs(ps.new_.pubs)
                .indices(indices)
                .build();
        psigers = (List<Object>) manager.sign(signArgs);
        assertEquals(psigers.size(), 1);
        assertInstanceOf(Siger.class, psigers.getFirst());
        assertEquals(((Siger) psigers.getFirst()).getIndex(), indices.getFirst());
        psigs = psigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();
        assertEquals(psigs.getFirst(), "ADCRPqO6vdXm1oSSa82rmVVHikf7NdN4JXjOWEk30Ub5JHChL0bW6DzJfA-7VlgLm_B1XR0Z61FweP87bBQpVawI");

        signArgs = Manager.SignArgs.builder()
                .ser(serb)
                .verfers(verfers)
                .indices(indices)
                .build();
        vsigers = (List<Object>) manager.sign(signArgs);
        assertEquals(vsigers.size(), 1);
        assertInstanceOf(Siger.class, vsigers.getFirst());
        assertEquals(((Siger) vsigers.getFirst()).getIndex(), indices.getFirst());
        vsigs = vsigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();
        assertEquals(psigs, vsigs);

        signArgs = Manager.SignArgs.builder()
                .ser(serb)
                .pubs(ps.new_.pubs)
                .indexed(false)
                .build();
        List<Object> pcigars = (List<Object>) manager.sign(signArgs);
        assertEquals(pcigars.size(), 1);
        assertInstanceOf(Cigar.class, pcigars.getFirst());

        signArgs = Manager.SignArgs.builder()
                .ser(serb)
                .verfers(verfers)
                .indexed(false)
                .build();
        List<Object> vcigars = (List<Object>) manager.sign(signArgs);
        assertEquals(vcigars.size(), 1);

        List<String> pcigs = pcigars.stream().map(sig -> ((Cigar) sig).getQb64()).toList();
        List<String> vcigs = vcigars.stream().map(sig -> ((Cigar) sig).getQb64()).toList();

        assertEquals(pcigs, vcigs);
        assertEquals(pcigs.getFirst(), "0BCRPqO6vdXm1oSSa82rmVVHikf7NdN4JXjOWEk30Ub5JHChL0bW6DzJfA-7VlgLm_B1XR0Z61FweP87bBQpVawI");

        List<String> oldpubs = verfers.stream().map(Verfer::getQb64).toList();

        Manager.RotateArgs rotateArgs =
                Manager.RotateArgs.builder()
                        .pre(spre)
                        .build();
        Manager.ManagerRotateResult hashes = manager.rotate(rotateArgs);
        verfers = hashes.verfers();
        digers = hashes.digers();
        assertEquals(verfers.size(), 1);
        assertEquals(digers.size(), 1);

        pp = manager.getKs().getPrms(spre);
        assertEquals(pp.pidx, 0);
        assertEquals(pp.algo, Manager.Algos.salty);
        assertEquals(((Matter) manager.getDecrypter().decrypt(pp.salt.getBytes(), null)).getQb64(), salt);
        assertEquals(pp.stem, "");
        assertEquals(pp.tier, Salter.Tier.low.name());

        ps = manager.getKs().getSits(spre);
        assertEquals(ps.old.pubs.getFirst(), "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
        assertEquals(ps.new_.pubs.size(), 1);
        assertEquals(ps.new_.pubs.getFirst(), "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
        assertEquals(ps.new_.ridx, 1);
        assertEquals(ps.new_.kidx, 1);
        assertEquals(ps.nxt.pubs.size(), 1);
        assertEquals(ps.nxt.pubs.getFirst(), "DHHneREQ1eZyQNc5nEsQYx1FqFVL1OTXmvmatTE77Cfe");
        assertEquals(ps.nxt.ridx, 2);
        assertEquals(ps.nxt.kidx, 2);

        keys = verfers.stream().map(Verfer::getQb64).toList();
        assertEquals(keys, ps.new_.pubs);

        digs = digers.stream().map(Diger::getQb64).toList();
        assertEquals(digs.getFirst(), "ECl1Env_5PQHqVMpHgoqg9H9mT7ENtk0Q499cmMT6Fvz");

        assertEquals(oldpubs, ps.old.pubs);

        oldpubs = verfers.stream().map(Verfer::getQb64).toList();
        List<String> deadpubs = ps.old.pubs;

        rotateArgs = Manager.RotateArgs.builder()
                .pre(spre)
                .build();
        manager.rotate(rotateArgs);
        pp = manager.getKs().getPrms(spre);
        assertEquals(pp.pidx, 0);

        ps = manager.getKs().getSits(spre);
        assertEquals(oldpubs, ps.old.pubs);

        for (String pub : deadpubs) {
            assertNull(manager.getKs().getPris(pub, decrypter0));
        }
        pl = manager.getKs().getPubs(Manager.riKey(spre, ps.new_.ridx));
        assertEquals(pl.pubs, ps.new_.pubs);

        pl = manager.getKs().getPubs(Manager.riKey(spre, ps.nxt.ridx));
        assertEquals(pl.pubs, ps.nxt.pubs);

        rotateArgs = Manager.RotateArgs.builder()
                .pre(spre)
                .ncount(0)
                .build();
        hashes = manager.rotate(rotateArgs);
        digers = hashes.digers();

        pp = manager.getKs().getPrms(spre);
        assertEquals(pp.pidx, 0);

        ps = manager.getKs().getSits(spre);
        assertEquals(ps.nxt.pubs.size(), 0);
        assertEquals(digers.size(), 0);

        String finalSpre = spre;
        assertThrows(Exception.class, () -> {
            manager.rotate(Manager.RotateArgs.builder().pre(finalSpre).build());
        });

        // randy algo support
        managerInceptArgs = Manager.ManagerInceptArgs.builder()
                .algo(Manager.Algos.randy)
                .build();
        managerInceptResult = manager.incept(managerInceptArgs);
        verfers = managerInceptResult.verfers();
        digers = managerInceptResult.digers();

        assertEquals(verfers.size(), 1);
        assertEquals(digers.size(), 1);
        assertEquals(manager.getPidx(), 2);
        String rpre = verfers.getFirst().getQb64();

        pp = manager.getKs().getPrms(rpre);
        assertEquals(pp.pidx, 1);
        assertEquals(pp.algo, Manager.Algos.randy);
        assertEquals(pp.salt, "");
        assertEquals(pp.stem, "");
        assertEquals(pp.tier, "");

        ps = manager.getKs().getSits(rpre);
        assertEquals(ps.old.pubs, Collections.emptyList());
        assertEquals(ps.new_.pubs.size(), 1);
        assertEquals(ps.new_.pubs.getFirst(), rpre);
        assertEquals(ps.new_.ridx, 0);
        assertEquals(ps.new_.kidx, 0);
        assertEquals(ps.nxt.pubs.size(), 1);
        assertEquals(ps.nxt.ridx, 1);
        assertEquals(ps.nxt.kidx, 1);

        keys = verfers.stream().map(Verfer::getQb64).toList();
        for (String key : keys) {
            assertNotEquals(null, manager.getKs().getPris(key, decrypter0));
        }

        String oldrpre = rpre;
        rpre = "DMqxMVG3IcCNK4lpFfCu5o5cxzv1lgpMM-9rfkY3XVUc";
        manager.move(oldrpre, rpre);

        oldpubs = verfers.stream().map(Verfer::getQb64).toList();
        rotateArgs = Manager.RotateArgs.builder()
                .pre(rpre)
                .build();
        manager.rotate(rotateArgs);

        pp = manager.getKs().getPrms(rpre);
        assertEquals(pp.pidx, 1);
        ps = manager.getKs().getSits(rpre);
        assertEquals(oldpubs, ps.old.pubs);

        // randy algo incept with null nxt
        managerInceptArgs = Manager.ManagerInceptArgs.builder()
                .algo(Manager.Algos.randy)
                .ncount(0)
                .build();
        managerInceptResult = manager.incept(managerInceptArgs);
        verfers = managerInceptResult.verfers();
        digers = managerInceptResult.digers();

        assertEquals(manager.getPidx(), 3);
        rpre = verfers.getFirst().getQb64();

        pp = manager.getKs().getPrms(rpre);
        assertEquals(pp.pidx, 2);
        ps = manager.getKs().getSits(rpre);
        assertEquals(ps.old.pubs, Collections.emptyList());
        assertEquals(digers, Collections.emptyList());

        // attempt to rotate after null
        String tmprpe = rpre;
        assertThrows(Exception.class, () -> {
            manager.rotate(Manager.RotateArgs.builder().pre(tmprpe).build());
        });

        // salty algorithm incept with stem
        managerInceptArgs = Manager.ManagerInceptArgs.builder()
                .stem(stem)
                .salt(salt)
                .temp(true)
                .build();
        managerInceptResult = manager.incept(managerInceptArgs);
        verfers = managerInceptResult.verfers();
        digers = managerInceptResult.digers();

        assertEquals(verfers.size(), 1);
        assertEquals(digers.size(), 1);
        assertEquals(manager.getPidx(), 4);

        spre = verfers.getFirst().getQb64();
        assertEquals("DOtu4gX3oc4feusD8wWIykLhjkpiJHXEe29eJ2b_1CyM", spre);

        pp = manager.getKs().getPrms(spre);
        assertEquals(pp.pidx, 3);
        assertEquals(pp.algo, Manager.Algos.salty);
        assertEquals(((Matter) manager.getDecrypter().decrypt(pp.salt.getBytes(), null)).getQb64(), salt);
        assertEquals(pp.stem, stem);
        assertEquals(pp.tier, Salter.Tier.low.name());

        ps = manager.getKs().getSits(spre);
        assertEquals(ps.old.pubs, Collections.emptyList());
        assertEquals(ps.new_.pubs.size(), 1);
        assertEquals(ps.new_.pubs.getFirst(), spre);
        assertEquals(ps.new_.ridx, 0);
        assertEquals(ps.new_.kidx, 0);
        assertEquals(ps.nxt.pubs.size(), 1);
        assertEquals(ps.nxt.pubs.getFirst(), "DBzZ6vejSNAZpXv1SDRnIF_P1UqcW5d2pu2U-v-uhXvE");
        assertEquals(ps.nxt.ridx, 1);
        assertEquals(ps.nxt.kidx, 1);

        keys = verfers.stream().map(Verfer::getQb64).toList();
        assertEquals(keys, ps.new_.pubs);

        digs = digers.stream().map(Diger::getQb64).toList();
        assertEquals(digs.getFirst(), "EIGjhyyBRcqCkPE9bmkph7morew0wW0ak-rQ-dHCH-M2");

        managerInceptArgs = Manager.ManagerInceptArgs.builder()
                .ncount(0)
                .salt(salt)
                .stem("wit0")
                .transferable(false)
                .temp(true)
                .build();
        managerInceptResult = manager.incept(managerInceptArgs);
        verfers = managerInceptResult.verfers();
        digers = managerInceptResult.digers();

        String witpre0 = verfers.getFirst().getQb64();
        assertEquals(witpre0, "BOTNI4RzN706NecNdqTlGEcMSTWiFUvesEqmxWR_op8n");
        assertEquals(verfers.getFirst().getCode(), MatterCodex.Ed25519N.getValue());
        assertNotNull(digers);

        managerInceptArgs = Manager.ManagerInceptArgs.builder()
                .ncount(0)
                .salt(salt)
                .stem("wit1")
                .transferable(false)
                .temp(true)
                .build();
        managerInceptResult = manager.incept(managerInceptArgs);
        verfers = managerInceptResult.verfers();
        digers = managerInceptResult.digers();

        String witpre1 = verfers.getFirst().getQb64();
        assertEquals(witpre1, "BAB_5xNXH4hoxDCtAHPFPDedZ6YwTo8mbdw_v0AOHOMt");
        assertEquals(verfers.getFirst().getCode(), MatterCodex.Ed25519N.getValue());
        assertNotNull(digers);

        assertNotEquals(witpre0, witpre1);
    }

    @Test
    @DisplayName("should support only Salty/Encrypted, Salty/Unencrypted and Randy/Encrypted")
    void testManager_shouldSupportOnlySaltyEncryptedSaltyUnencryptedRandyEncrypted() throws DigestException, LibsodiumException {
        // Support Salty/Unencrypted - pass only stretched passcode as Salt.
        String passcode = "0123456789abcdefghijk";
        Salter salter = new Salter(RawArgs.builder().raw(passcode.getBytes()).build());
        String salt = salter.getQb64();

        Manager manager = new Manager(Manager.ManagerArgs.builder().salter(salter).build());
        assertNull(manager.getEncrypter());

        Manager.ManagerInceptArgs managerInceptArgs = Manager.ManagerInceptArgs.builder()
                .salt(salt)
                .temp(true)
                .build();
        Manager.ManagerInceptResult managerInceptResult = manager.incept(managerInceptArgs);
        List<Verfer> verfers = managerInceptResult.verfers();
        List<Diger> digers = managerInceptResult.digers();

        assertEquals(verfers.size(), 1);
        assertEquals(digers.size(), 1);
        assertEquals(manager.getPidx(), 1);

        String spre = verfers.getFirst().getQb64();
        assertEquals(spre, "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
        Manager.PreSit ps = manager.getKs().getSits(spre);

        List<String> keys = verfers.stream().map(Verfer::getQb64).toList();
        assertEquals(keys, ps.new_.pubs);

        Manager.PubSet pl = manager.getKs().getPubs(Manager.riKey(spre, ps.new_.ridx));
        assertEquals(pl.pubs, ps.new_.pubs);
        pl = manager.getKs().getPubs(Manager.riKey(spre, ps.nxt.ridx));
        assertEquals(pl.pubs, ps.nxt.pubs);

        Manager.PubPath ppt = manager.getKs().getPths(ps.new_.pubs.getFirst());
        assertEquals(ppt.path, "0");
        assertEquals(ppt.code, "A");
        assertEquals(ppt.tier, "low");
        assertTrue(ppt.temp);

        List<String> digs = digers.stream().map(Diger::getQb64).toList();
        assertEquals(digs.getFirst(), "ENmcKrctbztF36MttN7seUYJqH2IMnkavBgGLR6Mj2-B");

        byte[] serb = ser.getBytes();
        List<Object> psigers = (List<Object>) manager.sign(Manager.SignArgs.builder().ser(serb).pubs(ps.new_.pubs).build());

        assertEquals(psigers.size(), 1);
        assertInstanceOf(Siger.class, psigers.get(0));
        List<Object> vsigers = (List<Object>) manager.sign(Manager.SignArgs.builder().ser(serb).verfers(verfers).build());
        List<String> psigs = psigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();
        List<String> vsigs = vsigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();
        assertEquals(psigs, vsigs);
        assertEquals(psigs.getFirst(), "AACRPqO6vdXm1oSSa82rmVVHikf7NdN4JXjOWEk30Ub5JHChL0bW6DzJfA-7VlgLm_B1XR0Z61FweP87bBQpVawI");

        String oldspre = spre;
        spre = "DCu5o5cxzv1lgMqxMVG3IcCNK4lpFfpMM-9rfkY3XVUc";
        manager.move(oldspre, spre);
        List<String> oldpubs = verfers.stream().map(Verfer::getQb64).toList();

        Manager.RotateArgs rotateArgs = Manager.RotateArgs.builder().pre(spre).build();
        Manager.ManagerRotateResult hashes = manager.rotate(rotateArgs);
        verfers = hashes.verfers();
        digers = hashes.digers();

        assertEquals(verfers.size(), 1);
        assertEquals(digers.size(), 1);

        Manager.PrePrm pp = manager.getKs().getPrms(spre);
        assertEquals(pp.pidx, 0);
        assertEquals(pp.algo, Manager.Algos.salty);
        assertEquals(pp.salt, "");
        assertEquals(pp.stem, "");
        assertEquals(pp.tier, Salter.Tier.low.name());

        ps = manager.getKs().getSits(spre);
        assertEquals(ps.old.pubs.getFirst(), "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
        assertEquals(ps.new_.pubs.size(), 1);
        assertEquals(ps.new_.pubs.getFirst(), "DB-fH5uto5o5XHZjNN3_W3PdT4MIyTCmQWDzMxMZV2kI");
        assertEquals(ps.new_.ridx, 1);
        assertEquals(ps.new_.kidx, 1);
        assertEquals(ps.nxt.pubs.size(), 1);
        assertEquals(ps.nxt.pubs.getFirst(), "DHHneREQ1eZyQNc5nEsQYx1FqFVL1OTXmvmatTE77Cfe");
        assertEquals(ps.nxt.ridx, 2);
        assertEquals(ps.nxt.kidx, 2);

        keys = verfers.stream().map(Verfer::getQb64).toList();
        assertEquals(keys, ps.new_.pubs);

        digs = digers.stream().map(Diger::getQb64).toList();
        assertEquals(digs.getFirst(), "ECl1Env_5PQHqVMpHgoqg9H9mT7ENtk0Q499cmMT6Fvz");

        assertEquals(oldpubs, ps.old.pubs);

        ppt = manager.getKs().getPths(ps.new_.pubs.getFirst());
        assertEquals(ppt.path, "0");
        assertEquals(ppt.code, "A");
        assertEquals(ppt.tier, "low");
        assertTrue(ppt.temp);

        psigers = (List<Object>) manager.sign(Manager.SignArgs.builder().ser(serb).pubs(ps.new_.pubs).build());
        assertEquals(psigers.size(), 1);
        assertInstanceOf(Siger.class, psigers.getFirst());
        vsigers = (List<Object>) manager.sign(Manager.SignArgs.builder().ser(serb).verfers(verfers).build());
        psigs = psigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();
        vsigs = vsigers.stream().map(sig -> ((Siger) sig).getQb64()).toList();
        assertEquals(psigs, vsigs);
        assertEquals(psigs.getFirst(), "AACRPqO6vdXm1oSSa82rmVVHikf7NdN4JXjOWEk30Ub5JHChL0bW6DzJfA-7VlgLm_B1XR0Z61FweP87bBQpVawI");
    }

    @Test
    @DisplayName("Should support creating and getting randy keeper")
    void testManager_ShouldSupportCreatingAndGettingRandyKeeper() throws DigestException, LibsodiumException {
        String passcode = "0123456789abcdefghijk";
        Salter salter = new Salter(RawArgs.builder().raw(passcode.getBytes()).build());
        Keeping.KeyManager manager = new Keeping.KeyManager(salter, Collections.emptyList());

        Keeping.RandyKeeper keeper0 = (Keeping.RandyKeeper) manager.create(Manager.Algos.randy, 0, new HashMap<>());
        Keeping.KeeperResult keeperResult = keeper0.incept(false);
        List<String> keys = keeperResult.verfers();
        Prefixer prefixes = new Prefixer(keys.getFirst());

        RandyKeyState randyKeyState = new RandyKeyState();
        randyKeyState.setNxts(keeper0.getParams().getNxts());
        randyKeyState.setPrxs(keeper0.getParams().getPrxs());

        Identifier identifier = new Identifier();
        identifier.setPrefix(prefixes.getQb64());
        identifier.setName("");
        identifier.setState(new KeyStateRecord());
        identifier.setRandy(randyKeyState);
        identifier.setTransferable(true);
        identifier.setWindexes(Collections.emptyList());
        Keeping.Keeper<?> keeper1 = manager.get(identifier);

        assertInstanceOf(Keeping.RandyKeeper.class, keeper1);
    }
}
