package org.cardanofoundation.signify.core;

import java.security.DigestException;
import java.util.*;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidSizeException;

@Getter
public class Manager {
    private String seed;
    private String salt;
    private Encrypter encrypter;
    private Decrypter decrypter;
    private KeyStore ks;

    public Manager() {
    }

    public Manager(ManagerArgs args) throws LibsodiumException {
        this.ks = args.ks == null ? new Keeper() : args.ks;
        this.seed = args.seed;

        args.aeid = args.aeid == null ? null : args.aeid;
        args.pidx = args.pidx == null ? 0 : args.pidx;
        args.algo = args.algo == null ? Algos.salty : args.algo;

        String salt = args.salter != null ? args.salter.getQb64() : null;
        args.tier = args.tier == null ? Tier.low.name() : args.tier;

        if (this.getPidx() == null) {
            this.setPidx(args.pidx);
        }

        if (this.getAlgo() == null) {
            this.setAlgo(args.algo);
        }

        if (this.getSalt() == null) {
            this.setSalt(salt);
        }

        if (this.getTier() == null) {
            this.setTier(args.tier);
        }

        if (this.getAeid() == null) {
            this.updateAeid(args.aeid, this.seed);
        }
    }

    public static Manager openManager(String passcode, String salt) throws LibsodiumException {
        if (passcode.length() < 21) {
            throw new IllegalArgumentException("Bran (passcode seed material) too short.");
        }

        String bran = MatterCodex.Salt_128.getValue() + "A" + passcode.substring(0, 21); // qb64 salt for seed
        Signer signer = new Salter(bran).signer(
                MatterCodex.Ed25519_Seed.getValue(),
                false,
                "",
                null,
                false
        );

        String seed = signer.getQb64();
        String aeid = signer.getVerfer().getQb64(); // lest it remove encryption

        Algos algo;

        Salter salter = salt != null ? new Salter(salt) : null;
        if (salter != null) {
            algo = Algos.salty;
        } else {
            algo = Algos.randy;
        }

        return new Manager(ManagerArgs.builder()
                .seed(seed)
                .aeid(aeid)
                .algo(algo)
                .salter(salter)
                .build());
    }

    public String getAeid() {
        return this.ks.getGbls("aeid");
    }

    public Integer getPidx() {
        String pidx = this.ks.getGbls("pidx");
        if (pidx != null) {
            return Integer.valueOf(pidx, 16);
        }
        return null;
    }

    public void setPidx(Integer pidx) {
        this.ks.pinGbls("pidx", Integer.toString(pidx, 16));
    }

    public String getSalt() throws LibsodiumException {
        if (this.decrypter == null) {
            return this.salt;
        } else {
            String salt = this.ks.getGbls("salt");
            return ((Matter) this.decrypter.decrypt(salt.getBytes(), null)).getQb64();
        }
    }

    public void setSalt(String salt) throws LibsodiumException {
        if (this.encrypter == null) {
            this.salt = salt;
        } else {
            this.ks.pinGbls("salt", this.encrypter.encrypt(salt.getBytes()).getQb64());
        }
    }

    public String getTier() {
        return this.ks.getGbls("tier");
    }

    public void setTier(String tier) {
        this.ks.pinGbls("tier", tier);
    }

    public Algos getAlgo() {
        String a = this.ks.getGbls("algo");
        return Algos.fromString(a);
    }

    public void setAlgo(Algos algo) {
        this.ks.pinGbls("algo", algo.getValue());
    }

    private void updateAeid(String aeid, String seed) throws LibsodiumException {
        if (this.getAeid() != null) {
            byte[] seedBytes = this.getSeed().getBytes();
            if (this.seed == null || !this.encrypter.verifySeed(seedBytes)) {
                throw new IllegalArgumentException("Last seed missing or provided last seed " +
                        "not associated with last aeid=" + this.getAeid());
            }
        }

        if (aeid != null && !aeid.isEmpty()) {
            if (!aeid.equals(this.getAeid())) {
                this.encrypter = new Encrypter(new RawArgs(), aeid.getBytes());
                if (seed == null || !this.encrypter.verifySeed(seed.getBytes())) {
                    throw new IllegalArgumentException("Last seed missing or provided last seed " +
                            "not associated with last aeid=" + aeid);
                }
            }
        } else if (this.getAlgo() == Algos.randy) {
            // Unlike KERIpy, we don't support unencrypted secrets
            throw new IllegalArgumentException("Invalid Manager configuration, encryption must be used with Randy key creation.");
        } else {
            this.encrypter = null;
        }

        String salt = this.getSalt();
        if (salt != null) {
            this.setSalt(salt);
        }

        if (this.decrypter != null) {
            for (Map.Entry<String, PrePrm> entry : this.ks.prmsElements()) {
                String keys = entry.getKey();
                PrePrm data = entry.getValue();

                if (data.salt != null) {
                    Object salter = this.decrypter.decrypt(data.salt.getBytes(), null);
                    data.salt =
                            this.encrypter == null ? ((Matter) salter).getQb64()
                                    : this.encrypter.encrypt(null, (Matter) salter).getQb64();

                    this.ks.pinPrms(keys, data);
                }
            }

            for (Map.Entry<String, Signer> entry : this.ks.prisElements(this.decrypter)) {
                String pubKey = entry.getKey();
                Signer signer = entry.getValue();

                this.ks.pinPris(pubKey, signer, this.encrypter);
            }
        }

        this.ks.pinGbls("aeid", aeid); // set aeid in db
        this.seed = seed; // set .seed in memory

        // update .decrypter
        this.decrypter =
                seed != null ? new Decrypter(new RawArgs(), seed.getBytes()) : null;

    }

    public ManagerInceptResult incept(ManagerInceptArgs args) throws DigestException, LibsodiumException {
        if (args.rooted && args.algo == null) {
            args.algo = this.getAlgo();
        }

        if (args.rooted && args.salt == null) {
            args.salt = this.getSalt();
        }

        if (args.rooted && args.tier == null) {
            args.tier = this.getTier();
        }

        int pidx = this.getPidx();
        int ridx = 0;
        int kidx = 0;

        Creator creator = new Creatory(args.algo).make(args.salt, Tier.fromString(args.tier), args.stem);

        if (args.icodes == null) {
            if (args.icount < 0) {
                throw new IllegalArgumentException("Invalid icount=" + args.icount + " must be >= 0.");
            }

            args.icodes = new ArrayList<>(Collections.nCopies(args.icount, args.icode));
        }

        Keys ikeys = creator.create(
                args.icodes,
                0,
                MatterCodex.Ed25519_Seed.getValue(),
                args.transferable,
                pidx,
                ridx,
                kidx,
                args.temp
        );

        List<Verfer> verfers = ikeys.getSigners().stream().map(Signer::getVerfer).toList();

        if (args.ncodes == null) {
            if (args.ncount < 0) {
                throw new IllegalArgumentException("Invalid ncount=" + args.ncount + " must be >= 0.");
            }

            args.ncodes = new ArrayList<>(Collections.nCopies(args.ncount, args.ncode));
        }

        Keys nkeys = creator.create(
                args.ncodes,
                0,
                MatterCodex.Ed25519_Seed.getValue(),
                args.transferable,
                pidx,
                ridx + 1,
                kidx + args.icodes.size(),
                args.temp
        );

        List<Diger> digers = new ArrayList<>();
        for (Signer signer : nkeys.getSigners()) {
            Diger diger = new Diger(args.dcode, signer.getVerfer().getQb64b());
            digers.add(diger);
        }

        PrePrm pp = new PrePrm();
        pp.pidx = pidx;
        pp.algo = args.algo;
        pp.salt =
                creator.salt().isEmpty() || this.encrypter == null
                        ? ""
                        : this.encrypter.encrypt(creator.salt().getBytes()).getQb64();

        pp.stem = creator.stem();
        pp.tier = creator.tier() != null ? creator.tier().name() : "";

        String dt = new Date().toString();
        PubLot nw = new PubLot();
        nw.pubs = verfers.stream().map(Verfer::getQb64).toList();
        nw.ridx = ridx;
        nw.kidx = kidx;
        nw.dt = dt;

        PubLot nt = new PubLot();
        nt.pubs = nkeys.getSigners().stream().map(Signer::getVerfer).map(Verfer::getQb64).toList();
        nt.ridx = ridx + 1;
        nt.kidx = kidx + args.icodes.size();
        nt.dt = dt;

        PreSit ps = new PreSit();
        ps.new_ = nw;
        ps.nxt = nt;

        String pre = verfers.getFirst().getQb64();
        if (!this.ks.putPres(pre, verfers.getFirst().getQb64b())) {
            throw new IllegalArgumentException("Already incepted pre=" + pre);
        }

        if (!this.ks.putPrms(pre, pp)) {
            throw new IllegalArgumentException("Already incepted prm for pre=" + pre);
        }

        this.setPidx(pidx + 1);

        if(!this.ks.putSits(pre, ps)) {
            throw new IllegalArgumentException("Already incepted sit for pre=" + pre);
        }

        if (this.encrypter != null) {
            // Only store encrypted keys if we have an encrypter, otherwise regenerate
            for (Signer signer : ikeys.getSigners()) {
                this.ks.pinPris(signer.getVerfer().getQb64(), signer, this.encrypter);
            }

            for (Signer signer : nkeys.getSigners()) {
                this.ks.pinPris(signer.getVerfer().getQb64(), signer, this.encrypter);
            }
        } else if (this.encrypter == null && ikeys.paths != null && nkeys.paths != null) {
            for (int i = 0; i < ikeys.paths.size(); i++) {
                String path = ikeys.paths.get(i);
                Signer signer = ikeys.signers.get(i);
                PubPath ppt = new PubPath();
                ppt.path = path;
                ppt.code = args.icodes.get(i);
                ppt.tier = pp.tier;
                ppt.temp = args.temp;
                this.ks.putPths(signer.getVerfer().getQb64(), ppt);
            }

            for (int i = 0; i < nkeys.paths.size(); i++) {
                String path = nkeys.paths.get(i);
                Signer signer = nkeys.signers.get(i);
                PubPath ppt = new PubPath();
                ppt.path = path;
                ppt.code = args.ncodes.get(i);
                ppt.tier = pp.tier;
                ppt.temp = args.temp;
                this.ks.putPths(signer.getVerfer().getQb64(), ppt);
            }
        } else {
            throw new IllegalArgumentException("invalid configuration, randy keys without encryption");
        }

        PubSet pubSet = new PubSet();
        pubSet.pubs = ps.new_.pubs;
        this.ks.putPubs(riKey(pre, ridx), pubSet);

        PubSet nxtPubSet = new PubSet();
        nxtPubSet.pubs = ps.nxt.pubs;
        this.ks.putPubs(riKey(pre, ridx + 1), nxtPubSet);

        return new ManagerInceptResult(verfers, digers);
    }

    void move(String old, String gnu) {
        if (old.equals(gnu)) {
            return;
        }

        if (this.ks.getPres(old) == null) {
            throw new IllegalArgumentException("Nonexistent old pre=" + old + ", nothing to assign.");
        }

        if (this.ks.getPres(gnu) != null) {
            throw new IllegalArgumentException("Preexistent new pre=" + gnu + ", may not clobber.");
        }

        PrePrm oldprm = this.ks.getPrms(old);
        if (oldprm == null) {
            throw new IllegalArgumentException("Nonexistent old prm for pre=" + old + ", nothing to move.");
        }

        if (this.ks.getPrms(gnu) != null) {
            throw new IllegalArgumentException("Preexistent new prm for pre=" + gnu + ", may not clobber.");
        }

        PreSit oldsit = this.ks.getSits(old);
        if (oldsit == null) {
            throw new IllegalArgumentException("Nonexistent old sit for pre=" + old + ", nothing to move.");
        }

        if (this.ks.getSits(gnu) != null) {
            throw new IllegalArgumentException("Preexistent new sit for pre=" + gnu + ", may not clobber.");
        }

        if (!this.ks.putPrms(gnu, oldprm)) {
            throw new IllegalArgumentException("Failed moving prm from old pre=" + old + " to new pre=" + gnu);
        } else {
            this.ks.remPrms(old);
        }

        if (!this.ks.putSits(gnu, oldsit)) {
            throw new IllegalArgumentException("Failed moving sit from old pre=" + old + " to new pre=" + gnu);
        } else {
            this.ks.remSits(old);
        }

        int i = 0;
        while (true) {
            PubSet pl = this.ks.getPubs(riKey(old, i));
            if (pl == null) {
                break;
            }

            if (!this.ks.putPubs(riKey(gnu, i), pl)) {
                throw new IllegalArgumentException("Failed moving pubs at pre=" + old + "ri=" + i + "to new pre=" + gnu);
            }

            i++;
        }

        if (!this.ks.pinPres(old, gnu.getBytes())) {
            throw new IllegalArgumentException("Failed assiging new pre=" + gnu + " to old pre=" + old);
        }

        if (!this.ks.putPres(gnu, old.getBytes())) {
            throw new IllegalArgumentException("Failed assiging new pre=" + gnu);
        }
    }

    public ManagerRotateResult rotate(RotateArgs args) throws DigestException, LibsodiumException {
        PrePrm pp = this.ks.getPrms(args.pre);
        if (pp == null) {
            throw new IllegalArgumentException("Attempt to rotate nonexistent pre=" + args.pre);
        }

        PreSit ps = this.ks.getSits(args.pre);
        if (ps == null) {
            throw new IllegalArgumentException("Attempt to rotate nonexistent pre=" + args.pre);
        }

        if (ps.nxt.pubs == null || ps.nxt.pubs.isEmpty()) {
            throw new IllegalArgumentException("Attempt to rotate nontransferable pre=" + args.pre);
        }

        PubLot old = ps.old;
        ps.old = ps.new_;
        ps.new_ = ps.nxt;

        if (this.getAeid() != null && this.decrypter == null) {
            throw new RuntimeException("Unauthorized decryption attempt.  Aeid but no decrypter.");
        }

        List<Verfer> verfers = new ArrayList<>();
        for (String pub : ps.new_.pubs) {
            if (this.decrypter != null) {
                Signer signer = this.ks.getPris(pub, this.decrypter);
                if (signer == null) {
                    throw new IllegalArgumentException("Missing prikey in db for pubkey=" + pub);
                }
                verfers.add(signer.getVerfer());
            } else {
                // Should we regenerate from salt here since this.decryptor is undefined
                verfers.add(new Verfer(pub));
            }
        }

        String salt = pp.salt;
        if (salt != null && !salt.isEmpty()) {
            if (this.decrypter == null) {
                // If you provded a Salt for an AID but don't have encryption, pitch a fit
                throw new RuntimeException("Invalid configuration: AID salt with no encryption");
            }
            salt = ((Matter) this.decrypter.decrypt(salt.getBytes(), null)).getQb64();
        } else {
            salt = this.getSalt();
        }

        Creator creator = new Creatory(pp.algo).make(salt, Tier.fromString(pp.tier), pp.stem);

        if (args.ncodes == null) {
            if (args.ncount < 0) {
                throw new IllegalArgumentException("Invalid count=" + args.ncount + " must be >= 0.");
            }
            args.ncodes = new ArrayList<>(Collections.nCopies(args.ncount, args.ncode));
        }

        int pidx = pp.pidx;
        int ridx = ps.new_.ridx + 1;
        int kidx = ps.nxt.kidx + ps.new_.pubs.size();

        Keys keys = creator.create(
                args.ncodes,
                0,
                "",
                args.transferable,
                pidx,
                ridx,
                kidx,
                args.temp
        );

        List<Diger> digers = new ArrayList<>();
        for (Signer signer : keys.getSigners()) {
            Diger diger = new Diger(args.dcode, signer.getVerfer().getQb64b());
            digers.add(diger);
        }

        String dt = new Date().toString();
        ps.nxt = new PubLot();
        ps.nxt.pubs = keys.getSigners().stream().map(Signer::getVerfer).map(Verfer::getQb64).toList();
        ps.nxt.ridx = ridx;
        ps.nxt.kidx = kidx;
        ps.nxt.dt = dt;

        if (!this.ks.pinSits(args.pre, ps)) {
            throw new IllegalArgumentException("Problem updating pubsit db for pre=" + args.pre);
        }

        if (this.encrypter != null) {
            // Only store encrypted keys if we have an encrypter, otherwise regenerate
            for (Signer signer : keys.getSigners()) {
                this.ks.putPris(signer.getVerfer().getQb64(), signer, this.encrypter);
            }
        } else if (this.encrypter == null && keys.paths != null) {
            for (int i = 0; i < keys.paths.size(); i++) {
                String path = keys.paths.get(i);
                Signer signer = keys.signers.get(i);
                PubPath ppt = new PubPath();
                ppt.path = path;
                ppt.tier = pp.tier;
                ppt.temp = args.temp;
                this.ks.putPths(signer.getVerfer().getQb64(), ppt);
            }
        } else {
            throw new IllegalArgumentException("invalid configuration, randy keys without encryption");
        }

        PubSet newPs = new PubSet();
        newPs.pubs = ps.nxt.pubs;
        this.ks.putPubs(riKey(args.pre, ps.nxt.ridx), newPs);

        if (args.erase) {
            old.pubs.forEach(pub -> this.ks.remPris(pub));
        }

        return new ManagerRotateResult(verfers, digers);
    }

    public Object sign(SignArgs args) throws LibsodiumException {
        List<Signer> signers = new ArrayList<>();
        if (args.pubs == null && args.verfers == null) {
            throw new IllegalArgumentException("pubs or verfers required");
        }

        if (args.pubs != null) {
            if (this.getAeid() != null && this.decrypter == null) {
                throw new RuntimeException("Unauthorized decryption attempt. Aeid but no decrypter.");
            }

            for (String pub : args.pubs) {
                //If no decrypter then get SaltyState and regenerate prikey
                if (this.decrypter != null) {
                    Signer signer = this.ks.getPris(pub, this.decrypter);
                    if (signer == null) {
                        throw new IllegalArgumentException("Missing prikey in db for pubkey=" + pub);
                    }
                    signers.add(signer);
                } else {
                    Verfer verfer = new Verfer(pub);
                    PubPath ppt = this.ks.getPths(pub);
                    if (ppt == null) {
                        throw new IllegalArgumentException("Missing prikey in db for pubkey=" + pub);
                    }
                    Salter salter = new Salter(this.getSalt());
                    Signer signer = salter.signer(
                            ppt.code,
                            verfer.isTransferable(),
                            ppt.path,
                            Tier.fromString(ppt.tier),
                            ppt.temp
                    );

                    signers.add(signer);
                }

            }
        } else {
            for (Verfer verfer : args.verfers) {
                if (this.decrypter != null) {
                    Signer signer = this.ks.getPris(verfer.getQb64(), this.decrypter);
                    if (signer == null) {
                        throw new IllegalArgumentException("Missing prikey in db for pubkey=" + verfer.getQb64());
                    }
                    signers.add(signer);
                } else {
                    PubPath ppt = this.ks.getPths(verfer.getQb64());
                    if (ppt == null) {
                        throw new IllegalArgumentException("Missing prikey in db for pubkey=" + verfer.getQb64());
                    }
                    Salter salter = new Salter(this.getSalt());
                    Signer signer = salter.signer(
                            ppt.code,
                            verfer.isTransferable(),
                            ppt.path,
                            Tier.fromString(ppt.tier),
                            ppt.temp
                    );
                    signers.add(signer);
                }
            }
        }

        if (args.indices != null && args.indices.size() != signers.size()) {
            throw new IllegalArgumentException("Mismatch indices length=" + args.indices.size() +
                    "and resultant signers length=" + signers.size());
        }

        if (args.ondices != null && args.ondices.size() != signers.size()) {
            throw new IllegalArgumentException("Mismatch ondices length=" + args.ondices.size() +
                    "and resultant signers length=" + signers.size());
        }

        if (args.indexed) {
            List<Siger> sigers = new ArrayList<>();
            for (int idx = 0; idx < signers.size(); idx++) {
                Signer signer = signers.get(idx);
                int i;
                if (args.indices != null) {
                    i = args.indices.get(idx);

                    if (i < 0) {
                        throw new IllegalArgumentException("Invalid signing index = " + i + ", not whole number.");
                    }
                } else {
                    i = idx;
                }

                Integer o;
                if (args.ondices != null) {
                    o = args.ondices.get(idx);
                    if (o <= 0) {
                        throw new IllegalArgumentException("Invalid other signing index = " + o + ", not None or not whole number.");
                    }
                } else {
                    o = i;
                }
                boolean only = o == null;
                Siger siger = (Siger) signer.sign(args.ser, i, only, o);
                sigers.add(siger);
            }
            return sigers;
        } else {
            List<Cigar> cigars = new ArrayList<>();
            for (Signer signer : signers) {
                Cigar cigar = (Cigar) signer.sign(args.ser);
                cigars.add(cigar);
            }
            return cigars;
        }
    }

    public static String riKey(String pre, int ridx) {
        return pre + "." + String.format("%032x", ridx);
    }

    @Getter
    public enum Algos {
        randy("randy"),
        salty("salty"),
        group("group");

        private final String value;

        Algos(String value) {
            this.value = value;
        }

        public static Algos fromString(String text) {
            for (Algos b : Algos.values()) {
                if (b.value.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    interface Creator {
        Keys create(List<String> codes, Integer count, String code, boolean transferable, int pidx, int ridx, int kidx, boolean temp) throws LibsodiumException;

        String salt();

        String stem();

        Tier tier();
    }

    public interface KeyStore {
        String getGbls(String key);

        void pinGbls(String key, String val);

        List<Map.Entry<String, PrePrm>> prmsElements();

        PrePrm getPrms(String keys);

        void pinPrms(String keys, PrePrm data);

        boolean putPrms(String keys, PrePrm data);

        boolean remPrms(String keys);

        List<Map.Entry<String, Signer>> prisElements(Decrypter decrypter) throws LibsodiumException;

        Signer getPris(String keys, Decrypter decrypter) throws LibsodiumException;

        void pinPris(String keys, Signer data, Encrypter encrypter) throws LibsodiumException;

        boolean putPris(String pubKey, Signer signer, Encrypter encrypter) throws LibsodiumException;

        void remPris(String pubKey);

        PubPath getPths(String pubKey);

        boolean putPths(String pubKey, PubPath val);

        boolean pinPths(String pubKey, PubPath val);

        byte[] getPres(String pre);

        boolean putPres(String pre, byte[] val);

        boolean pinPres(String pre, byte[] val);

        PreSit getSits(String keys);

        boolean putSits(String pre, PreSit val);

        boolean pinSits(String pre, PreSit val);

        boolean remSits(String keys);

        PubSet getPubs(String keys);

        boolean putPubs(String keys, PubSet data);
    }

    @Getter
    @Setter
    @Builder
    public static class SignArgs {
        byte[] ser;
        List<String> pubs;
        List<Verfer> verfers;
        @Builder.Default
        boolean indexed = true;
        List<Integer> indices;
        List<Integer> ondices;
    }

    @Getter
    @Setter
    @Builder
    public static class RotateArgs {
        private String pre;
        private List<String> ncodes;
        @Builder.Default
        private int ncount = 1;
        @Builder.Default
        private String ncode = MatterCodex.Ed25519_Seed.getValue();
        @Builder.Default
        private String dcode = MatterCodex.Blake3_256.getValue();
        @Builder.Default
        private boolean transferable = true;
        @Builder.Default
        private boolean temp = false;
        @Builder.Default
        private boolean erase = true;
    }

    @Getter
    @Setter
    @Builder
    public static class ManagerInceptArgs {
        private List<String> icodes;
        @Builder.Default
        private int icount = 1;
        @Builder.Default
        private String icode = MatterCodex.Ed25519_Seed.getValue();
        private List<String> ncodes;
        @Builder.Default
        private int ncount = 1;
        @Builder.Default
        private String ncode = MatterCodex.Ed25519_Seed.getValue();
        @Builder.Default
        private String dcode = MatterCodex.Blake3_256.getValue();
        private Algos algo;
        private String salt;
        private String stem;
        private String tier;
        @Builder.Default
        private boolean rooted = true;
        @Builder.Default
        private boolean transferable = true;
        @Builder.Default
        private boolean temp = false;
    }

    public record ManagerInceptResult(List<Verfer> verfers, List<Diger> digers) {
    }

    public record ManagerRotateResult(List<Verfer> verfers, List<Diger> digers) {
    }

    @Builder
    @Setter
    @Getter
    public static class ManagerArgs {
        KeyStore ks;
        String seed;
        String aeid;
        Integer pidx;
        Algos algo;
        Salter salter;
        String tier;
    }

    public static class PubLot {
        List<String> pubs = new ArrayList<>(); // list qb64 public keys.
        int ridx = 0; // index of rotation (est event) that uses public key set
        int kidx = 0; // index of key in sequence of public keys
        String dt = ""; // datetime ISO8601 when key set created
    }

    public static class PreSit {
        PubLot old = new PubLot(); //previous publot
        PubLot new_ = new PubLot(); //newly current publot
        PubLot nxt = new PubLot(); //next public publot
    }

    public static class PrePrm {
        int pidx = 0; // prefix index for this keypair sequence
        Algos algo = Algos.salty; // salty default uses indices and salt to create new key pairs
        String salt = ""; // empty salt used for salty algo
        String stem = ""; // default unique path stem for salty algo
        String tier = ""; // security tier for stretch index salty algo
    }

    public static class PubSet {
        List<String> pubs = new ArrayList<>(); // list qb64 public keys.
    }

    public static class PubPath {
        String path = "";
        String code = "";
        String tier = Tier.high.name();
        boolean temp = false;
    }

    @Getter
    public static class Keys {
        private final List<Signer> signers;
        private final List<String> paths;

        public Keys(List<Signer> signers, List<String> paths) {
            this.signers = signers;

            if (paths != null && signers.size() != paths.size()) {
                throw new InvalidSizeException("If paths are provided, they must be the same length as signers");
            }

            this.paths = paths;
        }

    }

    public static class RandyCreator implements Creator {
        @Override
        public Keys create(
            List<String> codes,
            Integer count,
            String code,
            boolean transferable,
            int pidx,
            int ridx,
            int kidx,
            boolean temp
        ) throws LibsodiumException {
            List<Signer> signers = new ArrayList<>();

            if (codes == null) {
                code = (code != null) ? code : MatterCodex.Ed25519_Seed.getValue();
                codes = Collections.nCopies(count, code);
            }

            for (String c : codes) {
                RawArgs rawArgs = RawArgs.builder()
                        .code(c)
                        .build();

                Signer signer = new Signer(rawArgs, transferable);
                signers.add(signer);
            }

            return new Keys(signers, null);
        }

        public Keys create() throws LibsodiumException {
            return create(null, 1, MatterCodex.Ed25519_Seed.getValue(), true, 0, 0, 0, false);
        }

        public Keys create(List<String> codes, Integer count, String code, boolean transferable) throws LibsodiumException {
            return create(codes, count, code, transferable, 0, 0, 0, false);
        }

        public Keys create(List<String> codes, Integer count) throws LibsodiumException {
            return create(codes, count, MatterCodex.Ed25519_Seed.getValue(), true, 0, 0, 0, false);
        }

        public Keys create(List<String> codes) throws LibsodiumException {
            return create(codes, 1, MatterCodex.Ed25519_Seed.getValue(), true, 0, 0, 0, false);
        }

        @Override
        public String salt() {
            return "";
        }

        @Override
        public String stem() {
            return "";
        }

        @Override
        public Tier tier() {
            return null;
        }
    }

    public static class SaltyCreator implements Creator {

        private final String stem;
        public Salter salter;

        public SaltyCreator() {
            RawArgs rawArgs = RawArgs.builder()
                    .code(MatterCodex.Salt_128.getValue())
                    .build();
            this.salter = new Salter(rawArgs, Tier.low);
            this.stem = "";
        }

        public SaltyCreator(String salt, Tier tier, String stem) {
            if (salt == null) {
                RawArgs rawArgs = RawArgs.builder()
                        .code(MatterCodex.Salt_128.getValue())
                        .build();
                this.salter = new Salter(rawArgs, tier);
            } else {
                this.salter = new Salter(salt, tier);
            }

            this.stem = stem == null ? "" : stem;
        }

        @Override
        public Keys create(
            List<String> codes,
            Integer count,
            String code,
            boolean transferable,
            int pidx,
            int ridx,
            int kidx,
            boolean temp
        ) throws LibsodiumException {
            List<Signer> signers = new ArrayList<>();
            List<String> paths = new ArrayList<>();

            if (codes == null || codes.isEmpty()) {
                codes = Collections.nCopies(count, code);
            }

            for (int idx = 0; idx < codes.size(); idx++) {
                String code_ = codes.get(idx);

                // Previous definition of path
                // let path = this.stem + pidx.toString(16) + ridx.toString(16) + (kidx+idx).toString(16)
                String path = this.stem.isEmpty()
                        ? Integer.toString(pidx, 16)
                        : this.stem + Integer.toString(ridx, 16) + Integer.toString(kidx + idx, 16);
                paths.add(path);

                Signer signer = this.salter.signer(code_, transferable, path, this.tier(), temp);
                signers.add(signer);
            }

            return new Keys(signers, paths);
        }

        public Keys create() throws LibsodiumException {
            return create(null, 1, MatterCodex.Ed25519_Seed.getValue(), true, 0, 0, 0, false);
        }

        @Override
        public String salt() {
            return this.salter.getQb64();
        }

        @Override
        public String stem() {
            return this.stem;
        }

        @Override
        public Tier tier() {
            return this.salter.getTier();
        }
    }

    public static class Creatory {
        private final MakeCreator makeCreator;

        public Creatory() {
            this(Algos.salty);
        }

        public Creatory(Algos algo) {
            switch (algo) {
                case randy:
                    this.makeCreator = this::makeRandy;
                    break;
                case salty:
                    this.makeCreator = this::makeSalty;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported algo: " + algo);
            }
        }

        private Creator makeRandy(Object... objects) {
            return new RandyCreator();
        }

        Creator make(Object... args) {
            String salt = null, stem = null;
            Tier tier = null;
            if (args.length >= 1) {
                salt = (String) args[0];
            }
            if (args.length >= 2) {
                tier = (Tier) args[1];
            }
            if (args.length >= 3) {
                stem = (String) args[2];
            }
            return this.makeCreator.make(salt, tier, stem);
        }

        Creator makeRandy() {
            return new RandyCreator();
        }

        Creator makeSalty(String salt, Tier tier, String stem) {
            return new SaltyCreator(salt, tier, stem);
        }

        @FunctionalInterface
        interface MakeCreator {
            Creator make(String salt, Tier tier, String stem);
        }
    }

    public static class Keeper implements KeyStore {
        private final Map<String, String> gbls;
        private final Map<String, byte[]> pris;
        private final Map<String, PubPath> pths;
        private final Map<String, byte[]> pres;
        private final Map<String, PrePrm> prms;
        private final Map<String, PreSit> sits;
        private final Map<String, PubSet> pubs;

        public Keeper() {
            this.gbls = new HashMap<>();
            this.pris = new HashMap<>();
            this.pths = new HashMap<>();
            this.pres = new HashMap<>();
            this.prms = new HashMap<>();
            this.sits = new HashMap<>();
            this.pubs = new HashMap<>();
        }

        public String getGbls(String key) {
            return gbls.get(key);
        }

        public void pinGbls(String key, String val) {
            gbls.put(key, val);
        }

        @Override
        public List<Map.Entry<String, PrePrm>> prmsElements() {
            return new ArrayList<>(prms.entrySet());
        }

        public PrePrm getPrms(String keys) {
            return prms.get(keys);
        }

        public void pinPrms(String keys, PrePrm data) {
            prms.put(keys, data);
        }

        public boolean putPrms(String keys, PrePrm data) {
            if (prms.containsKey(keys)) {
                return false;
            }
            prms.put(keys, data);
            return true;
        }

        public boolean remPrms(String keys) {
            return prms.remove(keys) != null;
        }

        @Override
        public List<Map.Entry<String, Signer>> prisElements(Decrypter decrypter) throws LibsodiumException {
            List<Map.Entry<String, Signer>> entries = new ArrayList<>();
            for (Map.Entry<String, byte[]> entry : pris.entrySet()) {
                Verfer verfer = new Verfer(entry.getKey());
                Signer signer = (Signer) decrypter.decrypt(entry.getValue(), null, verfer.isTransferable());
                entries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), signer));
            }
            return entries;
        }

        public void pinPris(String pubKey, Signer signer, Encrypter encrypter) throws LibsodiumException {
            Cipher cipher = encrypter.encrypt(null, signer);
            pris.put(pubKey, cipher.getQb64b());
        }

        public boolean putPris(String pubKey, Signer signer, Encrypter encrypter) throws LibsodiumException {
            if (pris.containsKey(pubKey)) {
                return false;
            }
            Cipher cipher = encrypter.encrypt(null, signer);
            pris.put(pubKey, cipher.getQb64b());
            return true;
        }

        public Signer getPris(String pubKey, Decrypter decrypter) throws LibsodiumException {
            byte[] val = pris.get(pubKey);
            if (val == null) {
                return null;
            }
            Verfer verfer = new Verfer(pubKey);
            return (Signer) decrypter.decrypt(val, null, verfer.isTransferable());
        }

        public boolean pinPths(String pubKey, PubPath val) {
            pths.put(pubKey, val);
            return true;
        }

        public boolean putPths(String pubKey, PubPath val) {
            if (pths.containsKey(pubKey)) {
                return false;
            }
            pths.put(pubKey, val);
            return true;
        }

        public PubPath getPths(String pubKey) {
            return pths.get(pubKey);
        }

        public void remPris(String pubKey) {
            pris.remove(pubKey);
        }

        public byte[] getPres(String pre) {
            return pres.get(pre);
        }

        public boolean pinPres(String pre, byte[] val) {
            pres.put(pre, val);
            return true;
        }

        public boolean putPres(String pre, byte[] val) {
            if (pres.containsKey(pre)) {
                return false;
            }
            pres.put(pre, val);
            return true;
        }

        public PreSit getSits(String keys) {
            return sits.get(keys);
        }

        public boolean putSits(String pre, PreSit val) {
            if (sits.containsKey(pre)) {
                return false;
            }
            sits.put(pre, val);
            return true;
        }

        public boolean pinSits(String pre, PreSit val) {
            sits.put(pre, val);
            return true;
        }

        public boolean remSits(String keys) {
            return sits.remove(keys) != null;
        }

        public PubSet getPubs(String keys) {
            return pubs.get(keys);
        }

        public boolean putPubs(String keys, PubSet data) {
            if (pubs.containsKey(keys)) {
                return false;
            }
            pubs.put(keys, data);
            return true;
        }
    }
}
