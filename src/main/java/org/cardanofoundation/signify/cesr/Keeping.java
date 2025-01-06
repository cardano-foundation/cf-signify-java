package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.params.GroupParams;
import org.cardanofoundation.signify.cesr.params.KeeperParams;
import org.cardanofoundation.signify.cesr.params.RandyParams;
import org.cardanofoundation.signify.cesr.params.SaltyParams;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.Manager.RandyCreator;
import org.cardanofoundation.signify.core.Manager.SaltyCreator;
import org.cardanofoundation.signify.core.Manager.Algos;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.core.States.HabState;
import org.cardanofoundation.signify.core.States.State;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import java.security.DigestException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.cardanofoundation.signify.core.States.convertToStates;

public class Keeping {
    // External module interface
    public interface ExternalModuleType {
        Keeper<? extends KeeperParams> createKeeper(int pidx, KeeperParams args);
    }

    public interface Keeper<T extends KeeperParams> {
        Manager.Algos getAlgo();

        List<Signer> getSigners();

        T getParams();

        KeeperResult incept(boolean transferable) throws SodiumException, DigestException;

        KeeperResult rotate(
                List<String> ncodes,
                boolean transferable,
                List<State> states,
                List<State> rstates
        ) throws SodiumException, DigestException;

        SignResult sign(
                byte[] ser,
                Boolean indexed,
                List<Integer> indices,
                List<Integer> ondices
        ) throws SodiumException;

        default SignResult sign(byte[] ser) throws SodiumException {
            return sign(ser, true, null, null);
        }
    }

    public record ExternalModule(
            String type,
            String name,
            ExternalModuleType module
    ) {
    }

    public record KeeperResult(List<String> verfers, List<String> digers) {
    }

    public record SignResult(List<String> signatures) {
    }

    public static class KeyManager {
        private final Salter salter;
        private final Map<String, ExternalModuleType> modules = new HashMap<>();

        public KeyManager(Salter salter, List<ExternalModule> externalModules) {
            this.salter = salter;
            for (ExternalModule mod : externalModules) {
                modules.put(mod.type(), mod.module());
            }
        }

        public Keeper<? extends KeeperParams> create(Algos algo, int pidx, Map<String, Object> kargs) throws SodiumException {
            return switch (algo) {
                case salty -> new SaltyKeeper(
                        salter,
                        pidx,
                        (Integer) kargs.get("kidx"),
                        (Tier) kargs.get("tier"),
                        (Boolean) kargs.get("transferable"),
                        (String) kargs.get("stem"),
                        (String) kargs.get("code"),
                        (Integer) kargs.get("count"),
                        (List<String>) kargs.get("icodes"),
                        (String) kargs.get("ncode"),
                        (Integer) kargs.get("ncount"),
                        (List<String>) kargs.get("ncodes"),
                        (String) kargs.get("dcode"),
                        (String) kargs.get("bran"),
                        (String) kargs.get("sxlt")
                );
                case randy -> new RandyKeeper(
                        salter,
                        (String) kargs.get("code"),
                        (Integer) kargs.get("count"),
                        (List<String>) kargs.get("icodes"),
                        (Boolean) kargs.get("transferable"),
                        (String) kargs.get("ncode"),
                        (Integer) kargs.get("ncount"),
                        (List<String>) kargs.get("ncodes"),
                        (String) kargs.get("dcode"),
                        (List<String>) kargs.get("prxs"),
                        (List<String>) kargs.get("nxts")
                );
                case group -> new GroupKeeper(
                        this,
                        (HabState) kargs.get("mhab"),
                        (List<State>) kargs.get("states"),
                        (List<State>) kargs.get("rstates"),
                        (List<String>) kargs.get("keys"),
                        (List<String>) kargs.get("ndigs")
                );
                default -> throw new UnsupportedOperationException("Unknown algo");
            };
        }

        public Keeper<? extends KeeperParams> get(HabState aid) throws SodiumException {
            if (aid.containsKey(Algos.salty.getValue())) {
                States.SaltyState kargs = (States.SaltyState) aid.get(Algos.salty.getValue());
                return new SaltyKeeper(
                        salter,
                        kargs.getPidx(),
                        kargs.getKidx(),
                        kargs.getTier(),
                        kargs.isTransferable(),
                        kargs.getStem(),
                        null,
                        null,
                        kargs.getIcodes(),
                        null,
                        null,
                        kargs.getNcodes(),
                        kargs.getDcode(),
                        null,
                        kargs.getSxlt()
                );
            } else if (aid.containsKey(Algos.randy.getValue())) {
                Prefixer pre = new Prefixer(aid.getPrefix());
                States.RandyState kargs = (States.RandyState) aid.get(Algos.randy.getValue());
                return new RandyKeeper(
                        salter,
                        null,
                        null,
                        null,
                        pre.isTransferable(),
                        null,
                        null,
                        List.of(),
                        null,
                        kargs.getPrxs(),
                        kargs.getNxts()
                );
            } else if (aid.containsKey(Algos.group.name())) {
                States.GroupState kargs = (States.GroupState) aid.get(Algos.group.name());
                return new GroupKeeper(
                        this,
                        kargs.getMhab(),
                        null,
                        null,
                        kargs.getKeys(),
                        kargs.getNdigs()
                );
            } else {
                throw new UnsupportedOperationException("Algo not allowed yet");
            }
        }
    }

    @Getter
    public static class SaltyKeeper implements Keeper<SaltyParams> {
        private final String aeid;
        private final Encrypter encrypter;
        private final Decrypter decrypter;
        private final Salter salter;
        private final int pidx;
        private final Tier tier;
        private final String stem;
        private final String code;
        private final int count;
        private final List<String> icodes;
        private final String ncode;
        private final int ncount;
        private final String dcode;
        private final String sxlt;
        private final String bran;
        private final SaltyCreator creator;
        private final Algos algo = Algos.salty;
        private final List<Signer> signers;
        private int kidx;
        private boolean transferable;
        private List<String> ncodes;

        public SaltyKeeper(
                Salter salter,
                Integer pidx,
                Integer kidx,
                Tier tier,
                Boolean transferable,
                String stem,
                String code,
                Integer count,
                List<String> icodes,
                String ncode,
                Integer ncount,
                List<String> ncodes,
                String dcode,
                String bran,
                String sxlt
        ) throws SodiumException {
            this.salter = salter;
            this.pidx = pidx;
            this.kidx = kidx != null ? kidx : 0;
            this.tier = tier != null ? tier : Tier.low;
            this.transferable = transferable;
            this.code = code != null ? code : MatterCodex.Ed25519_Seed.getValue();
            this.count = count != null ? count : 1;
            this.ncode = ncode != null ? ncode : MatterCodex.Ed25519_Seed.getValue();
            this.ncount = ncount != null ? ncount : 1;
            this.dcode = dcode != null ? dcode : MatterCodex.Blake3_256.getValue();
            this.stem = stem != null ? stem : "signify:aid";

            Signer signer = this.salter.signer(this.code, this.transferable, null, this.tier, null);
            this.aeid = signer.getVerfer().getQb64();

            this.encrypter = new Encrypter(RawArgs.builder().build(), this.aeid.getBytes());
            this.decrypter = new Decrypter(RawArgs.builder().build(), signer.getQb64b());

            this.icodes = icodes != null ? icodes :
                    IntStream.range(0, this.count)
                            .mapToObj(i -> this.code)
                            .collect(Collectors.toList());

            this.ncodes = ncodes != null ? ncodes :
                    IntStream.range(0, this.ncount)
                            .mapToObj(i -> this.ncode)
                            .collect(Collectors.toList());

            if (bran != null) {
                this.bran = MatterCodex.Salt_128.getValue() + "A" + bran.substring(0, 21);
                this.creator = new Manager.SaltyCreator(this.bran, this.tier, this.stem);
                this.sxlt = this.encrypter.encrypt(this.creator.salt().getBytes(), null).getQb64();
            } else if (sxlt == null) {
                this.bran = null;
                this.creator = new Manager.SaltyCreator(null, this.tier, this.stem);
                this.sxlt = this.encrypter.encrypt(this.creator.salt().getBytes(), null).getQb64();
            } else {
                this.bran = null;
                this.sxlt = sxlt;
                Cipher ciph = new Cipher(this.sxlt);
                Object decrypted = this.decrypter.decrypt(null, ciph, null);

                if (ciph.getCode().equals(MatterCodex.X25519_Cipher_Salt.getValue())) {
                    this.creator = new Manager.SaltyCreator(
                            ((Salter) decrypted).getQb64(),
                            tier,
                            this.stem
                    );
                } else if (ciph.getCode().equals(MatterCodex.X25519_Cipher_Seed.getValue())) {
                    this.creator = new Manager.SaltyCreator(
                            ((Signer) decrypted).getQb64(),
                            tier,
                            this.stem
                    );
                } else {
                    throw new UnexpectedCodeException("Unsupported cipher text code = " + ciph.getCode());
                }
            }

            this.signers = this.creator.create(
                    this.icodes,
                    this.ncount,
                    this.ncode,
                    this.transferable,
                    this.pidx,
                    0,
                    this.kidx,
                    false
            ).getSigners();
        }

        @Override
        public SaltyParams getParams() {
            return SaltyParams.builder()
                    .sxlt(sxlt)
                    .pidx(pidx)
                    .kidx(kidx)
                    .stem(stem)
                    .tier(tier)
                    .icodes(icodes)
                    .ncodes(ncodes)
                    .dcode(dcode)
                    .transferable(transferable)
                    .build();
        }

        @Override
        public KeeperResult incept(boolean transferable) throws SodiumException, DigestException {
            this.transferable = transferable;
            this.kidx = 0;

            Manager.Keys signers = creator.create(
                    icodes,
                    count,
                    code,
                    this.transferable,
                    pidx,
                    0,
                    kidx,
                    false
            );
            List<String> verfers = signers.getSigners().stream()
                    .map(signer -> signer.getVerfer().getQb64())
                    .collect(Collectors.toList());

            Manager.Keys nsigners = creator.create(
                    ncodes,
                    ncount,
                    ncode,
                    this.transferable,
                    pidx,
                    0,
                    icodes.size(),
                    false
            );
            List<String> digers = new ArrayList<>();
            for (Signer nsigner : nsigners.getSigners()) {
                digers.add(new Diger(this.dcode, nsigner.getVerfer().getQb64b()).getQb64());
            }
            return new KeeperResult(verfers, digers);
        }

        @Override
        public KeeperResult rotate(
                List<String> ncodes,
                boolean transferable,
                List<State> states,
                List<State> rstates
        ) throws SodiumException, DigestException {
            this.ncodes = ncodes;
            this.transferable = transferable;

            Manager.Keys signers = creator.create(
                    this.ncodes,
                    ncount,
                    ncode,
                    this.transferable,
                    pidx,
                    0,
                    kidx + this.icodes.size(),
                    false
            );
            List<String> verfers = signers.getSigners().stream()
                    .map(signer -> signer.getVerfer().getQb64())
                    .collect(Collectors.toList());

            this.kidx = this.kidx + this.icodes.size();
            Manager.Keys nsigners = creator.create(
                    this.ncodes,
                    ncount,
                    ncode,
                    this.transferable,
                    pidx,
                    0,
                    this.kidx + this.icodes.size(),
                    false
            );
            List<String> digers = new ArrayList<>();
            for (Signer nsigner : nsigners.getSigners()) {
                digers.add(new Diger(this.dcode, nsigner.getVerfer().getQb64b()).getQb64());
            }

            return new KeeperResult(verfers, digers);
        }

        @Override
        public SignResult sign(
                byte[] ser,
                Boolean indexed,
                List<Integer> indices,
                List<Integer> ondices
        ) throws SodiumException {
            Manager.Keys signers = creator.create(
                    icodes,
                    ncount,
                    ncode,
                    transferable,
                    pidx,
                    0,
                    kidx,
                    false
            );

            List<String> signatures = new ArrayList<>();
            if (Boolean.TRUE.equals(indexed)) {
                for (int j = 0; j < signers.getSigners().size(); j++) {
                    int i;
                    Signer signer = signers.getSigners().get(j);
                    if (indices != null && !indices.isEmpty()) {
                        i = indices.get(j);

                        if (i < 0) {
                            throw new InvalidValueException("Invalid signing index = " + i + ", not whole number");
                        }

                    } else {
                        i = j;
                    }

                    Integer o;
                    if (ondices != null && !ondices.isEmpty()) {
                        o = ondices.get(j);
                        if (o != null && o < 0) {
                            throw new InvalidValueException("Invalid other signing index = " + o + ", not None or not a whole number.");
                        }
                    } else {
                        o = i;
                    }

                    Siger siger = (Siger) signer.sign(ser, i, o == null, 0);
                    signatures.add(siger.getQb64());
                }
            } else {
                for (Signer signer : signers.getSigners()) {
                    Cigar cigar = (Cigar) signer.sign(ser);
                    signatures.add(cigar.getQb64());
                }
            }

            return new SignResult(signatures);
        }
    }

    @Getter
    public static class RandyKeeper implements Keeper<RandyParams> {
        private final String aeid;
        private final Encrypter encrypter;
        private final Decrypter decrypter;
        private final Salter salter;
        private final String code;
        private final int count;
        private final List<String> icodes;
        private final String ncode;
        private final int ncount;
        private final String dcode;
        private final RandyCreator creator;
        private final Algos algo = Algos.randy;
        private final List<Signer> signers;
        private boolean transferable;
        private List<String> ncodes;
        private List<String> prxs;
        private List<String> nxts;

        public RandyKeeper(
                Salter salter,
                String code,
                Integer count,
                List<String> icodes,
                Boolean transferable,
                String ncode,
                Integer ncount,
                List<String> ncodes,
                String dcode,
                List<String> prxs,
                List<String> nxts
        ) throws SodiumException {
            this.salter = salter;
            this.code = code != null ? code : MatterCodex.Ed25519_Seed.getValue();
            this.count = count != null ? count : 1;
            this.transferable = transferable != null ? transferable : false;
            this.ncode = ncode != null ? ncode : MatterCodex.Ed25519_Seed.getValue();
            this.ncount = ncount != null ? ncount : 1;
            this.dcode = dcode != null ? dcode : MatterCodex.Blake3_256.getValue();

            this.icodes = icodes != null ? icodes :
                    IntStream.range(0, this.count)
                            .mapToObj(i -> this.code)
                            .collect(Collectors.toList());

            this.ncodes = ncodes != null ? ncodes :
                    IntStream.range(0, this.ncount)
                            .mapToObj(i -> this.ncode)
                            .collect(Collectors.toList());

            Signer signer = this.salter.signer(this.code, this.transferable, null, null, null);
            this.aeid = signer.getVerfer().getQb64();

            this.encrypter = new Encrypter(RawArgs.builder().build(), this.aeid.getBytes());
            this.decrypter = new Decrypter(RawArgs.builder().build(), signer.getQb64b());

            this.nxts = nxts != null ? nxts : new ArrayList<>();
            this.prxs = prxs != null ? prxs : new ArrayList<>();

            this.creator = new RandyCreator();

            this.signers = new ArrayList<>();
            for (String prx : this.prxs) {
                this.signers.add((Signer) this.decrypter.decrypt(
                        new Cipher(prx).getQb64b(),
                        null,
                        this.transferable
                ));
            }
        }

        @Override
        public RandyParams getParams() {
            return RandyParams.builder()
                    .nxts(nxts)
                    .prxs(prxs)
                    .transferable(transferable)
                    .build();
        }

        @Override
        public KeeperResult incept(boolean transferable) throws SodiumException, DigestException {
            this.transferable = transferable;

            Manager.Keys signers = creator.create(
                    icodes,
                    count,
                    code,
                    this.transferable
            );

            this.prxs = new ArrayList<>();
            List<String> verfers = new ArrayList<>();

            for (Signer signer : signers.getSigners()) {
                this.prxs.add(this.encrypter.encrypt(null, signer).getQb64());
                verfers.add(signer.getVerfer().getQb64());
            }

            Manager.Keys nsigners = creator.create(
                    ncodes,
                    ncount,
                    ncode,
                    this.transferable
            );

            this.nxts = new ArrayList<>();
            List<String> digers = new ArrayList<>();

            for (Signer nsigner : nsigners.getSigners()) {
                this.nxts.add(this.encrypter.encrypt(null, nsigner).getQb64());
                digers.add(new Diger(dcode, nsigner.getVerfer().getQb64b()).getQb64());
            }

            return new KeeperResult(verfers, digers);
        }

        @Override
        public KeeperResult rotate(
                List<String> ncodes,
                boolean transferable,
                List<State> states,
                List<State> rstates
        ) throws SodiumException, DigestException {
            this.ncodes = ncodes;
            this.transferable = transferable;
            this.prxs = this.nxts;

            List<String> verfers = new ArrayList<>();

            for (String ntx : this.nxts) {
                Signer signer = (Signer) this.decrypter.decrypt(
                        null,
                        new Cipher(ntx),
                        this.transferable
                );
                verfers.add(signer.getVerfer().getQb64());
            }

            Manager.Keys nsigners = creator.create(
                    this.ncodes,
                    this.ncount,
                    this.ncode,
                    this.transferable
            );

            this.nxts = new ArrayList<>();
            List<String> digers = new ArrayList<>();
            for (Signer nsigner : nsigners.getSigners()) {
                this.nxts.add(this.encrypter.encrypt(null, nsigner).getQb64());
                digers.add(new Diger(dcode, nsigner.getVerfer().getQb64b()).getQb64());
            }

            return new KeeperResult(verfers, digers);
        }

        @Override
        public SignResult sign(
                byte[] ser,
                Boolean indexed,
                List<Integer> indices,
                List<Integer> ondices
        ) throws SodiumException {
            List<Signer> signers = new ArrayList<>();
            for (String prx : this.prxs) {
                Signer signer = (Signer) this.decrypter.decrypt(
                        new Cipher(prx).getQb64b(),
                        null,
                        this.transferable
                );
                signers.add(signer);
            }

            List<String> signatures = new ArrayList<>();
            if (Boolean.TRUE.equals(indexed)) {
                for (int j = 0; j < signers.size(); j++) {
                    int i;
                    Signer signer = signers.get(j);
                    if (indices != null && !indices.isEmpty()) {
                        i = indices.get(j);

                        if (i < 0) {
                            throw new InvalidValueException("Invalid signing index = " + i + ", not whole number");
                        }

                    } else {
                        i = j;
                    }

                    Integer o;
                    if (ondices != null && !ondices.isEmpty()) {
                        o = ondices.get(j);
                        if (o != null && o < 0) {
                            throw new InvalidValueException("Invalid other signing index = " + o + ", not None or not a whole number.");
                        }
                    } else {
                        o = i;
                    }

                    Siger siger = (Siger) signer.sign(ser, i, o == null, 0);
                    signatures.add(siger.getQb64());
                }

            } else {
                for (Signer signer : signers) {
                    signatures.add(((Cigar) signer.sign(ser)).getQb64());
                }
            }

            return new SignResult(signatures);
        }
    }

    @Getter
    public static class GroupKeeper implements Keeper<GroupParams> {
        private final KeyManager manager;
        private final HabState mhab;
        private final Algos algo = Algos.group;
        private final List<Signer> signers;
        private List<String> gkeys;
        private List<String> gdigs;

        public GroupKeeper(
                KeyManager manager,
                HabState mhab,
                List<State> states,
                List<State> rstates,
                List<String> keys,
                List<String> ndigs
        ) {
            this.manager = manager;
            this.mhab = mhab;
            this.signers = new ArrayList<>();

            if (states != null) {
                states = convertToStates(states);
                keys = states.stream()
                        .map(state -> state.getK().getFirst())
                        .collect(Collectors.toList());
            }

            if (rstates != null) {
                rstates = convertToStates(rstates);
                ndigs = rstates.stream()
                        .map(state -> state.getN().getFirst())
                        .collect(Collectors.toList());
            }

            this.gkeys = keys != null ? keys : new ArrayList<>();
            this.gdigs = ndigs != null ? ndigs : new ArrayList<>();
        }

        @Override
        public GroupParams getParams() {
            return GroupParams.builder()
                    .mhab(mhab)
                    .build();
        }

        @Override
        public KeeperResult incept(boolean transferable) {
            return new KeeperResult(gkeys, gdigs);
        }

        @Override
        public KeeperResult rotate(
                List<String> ncodes,
                boolean transferable,
                List<State> states,
                List<State> rstates
        ) {
            this.gkeys = states.stream()
                    .map(state -> state.getK().getFirst())
                    .collect(Collectors.toList());

            this.gdigs = rstates.stream()
                    .map(state -> state.getN().getFirst())
                    .collect(Collectors.toList());

            return new KeeperResult(gkeys, gdigs);
        }

        @Override
        public SignResult sign(
                byte[] ser,
                Boolean indexed,
                List<Integer> indices,
                List<Integer> ondices
        ) throws SodiumException {
            if (mhab.getState() == null) {
                throw new IllegalStateException("No state in mhab");
            }

            String key = mhab.getState().getK().getFirst();
            String ndig = mhab.getState().getN().getFirst();

            int csi = gkeys.indexOf(key);
            int pni = gdigs.indexOf(ndig);

            Keeper<?> mkeeper = manager.get(mhab);
            return mkeeper.sign(ser, indexed != null ? indexed : true,
                    Collections.singletonList(csi), Collections.singletonList(pni));
        }
    }
}
