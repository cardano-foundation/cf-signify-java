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
import org.cardanofoundation.signify.core.States.HabState;
import org.cardanofoundation.signify.core.States.State;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Keeping {
    // External module interface
    public interface ExternalModuleType {
        Keeper<? extends KeeperParams> createKeeper(int pidx, KeeperParams args);
    }

    public record ExternalModule(
        String type,
        String name,
        ExternalModuleType module
    ) {}

    public record KeeperResult(List<String> verfers, List<String> digers) {}

    public record SignResult(List<String> signatures) {}

    public interface Keeper<T extends KeeperParams> {
        Manager.Algos getAlgo();
        List<Signer> getSigners();
        T getParams();
        CompletableFuture<KeeperResult> incept(boolean transferable) throws SodiumException;
        CompletableFuture<KeeperResult> rotate(
            List<String> ncodes,
            boolean transferable,
            List<State> states,
            List<State> rstates
        ) throws SodiumException;
        CompletableFuture<SignResult> sign(
            byte[] ser,
            Boolean indexed,
            List<Integer> indices,
            List<Integer> ondices
        ) throws SodiumException;
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
                Map<String, Object> kargs = (Map<String, Object>) aid.get(Algos.salty.getValue());
                return new SaltyKeeper(
                    salter,
                    (Integer) kargs.get("pidx"),
                    (Integer) kargs.get("kidx"),
                    (Tier) kargs.get("tier"),
                    (Boolean) kargs.get("transferable"),
                    (String) kargs.get("stem"),
                    null,
                    null,
                    (List<String>) kargs.get("icodes"),
                    null,
                    null,
                    (List<String>) kargs.get("ncodes"),
                    (String) kargs.get("dcode"),
                    null,
                    (String) kargs.get("sxlt")
                );
            } else if (aid.containsKey(Algos.randy.getValue())) {
                Prefixer pre = new Prefixer(aid.getPrefix());
                Map<String, Object> kargs = (Map<String, Object>) aid.get(Algos.randy.getValue());
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
                    (List<String>) kargs.get("prxs"),
                    (List<String>) kargs.get("nxts")
                );
            } else if (aid.containsKey(Algos.group.name())) {
                Map<String, Object> kargs = (Map<String, Object>) aid.get(Algos.group.name());
                return new GroupKeeper(
                    this,
                    (HabState) kargs.get("mhab"),
                    null,
                    null,
                    (List<String>) kargs.get("keys"),
                    (List<String>) kargs.get("ndigs")
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
        private int kidx;
        private final Tier tier;
        private boolean transferable;
        private final String stem;
        private final String code;
        private final int count;
        private final List<String> icodes;
        private final String ncode;
        private final int ncount;
        private List<String> ncodes;
        private final String dcode;
        private final String sxlt;
        private final String bran;
        private final SaltyCreator creator;
        private final Algos algo = Algos.salty;
        private final List<Signer> signers;

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
        public CompletableFuture<KeeperResult> incept(boolean transferable) throws SodiumException {
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
            List<String> digers = nsigners.getSigners().stream()
                .map(nsigner ->
                    new Diger(
                        RawArgs.builder()
                            .code(this.code)
                            .build(),
                        nsigner.getVerfer().getQb64b()
                    ).getQb64()
                )
                .collect(Collectors.toList());

            return CompletableFuture.completedFuture(new KeeperResult(verfers, digers));
        }

        @Override
        public CompletableFuture<KeeperResult> rotate(
            List<String> ncodes,
            boolean transferable,
            List<State> states,
            List<State> rstates
        ) throws SodiumException {
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
            List<String> digers = nsigners.getSigners().stream()
                .map(nsigner ->
                    new Diger(
                        RawArgs.builder()
                            .code(this.code)
                            .build(),
                        nsigner.getVerfer().getQb64b()
                    ).getQb64()
                )
                .collect(Collectors.toList());

            return CompletableFuture.completedFuture(new KeeperResult(verfers, digers));
        }

        @Override
        public CompletableFuture<SignResult> sign(
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

            List<String> signatures;
            if (indexed != null && indexed) {
                signatures = IntStream.range(0, signers.getSigners().size())
                    .mapToObj(j -> {
                        Signer signer = signers.getSigners().get(j);
                        int i = indices != null ? indices.get(j) : j;
                        if (i < 0) {
                            throw new InvalidValueException("Invalid signing index = " + i);
                        }
                        int o = ondices != null ? ondices.get(j) : i;
                        if (o < 0) {
                            throw new InvalidValueException("Invalid ondex = " + o);
                        }
                        try {
                            Siger siger = (Siger) signer.sign(ser, i, o == 0, o);
                            return siger.getQb64();
                        } catch (SodiumException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            } else {
                signatures = signers.getSigners().stream()
                    .map(signer -> {
                        try {
                            Cigar cigar = (Cigar) signer.sign(ser);
                            return cigar.getQb64();
                        } catch (SodiumException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            }

            return CompletableFuture.completedFuture(new SignResult(signatures));
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
        private boolean transferable;
        private final String ncode;
        private final int ncount;
        private List<String> ncodes;
        private final String dcode;
        private List<String> prxs;
        private List<String> nxts;
        private final RandyCreator creator;
        private final Algos algo = Algos.randy;
        private final List<Signer> signers;

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

            this.signers = this.prxs.stream()
                .map(prx -> {
                    try {
                        return (Signer) this.decrypter.decrypt(
                            new Cipher(prx).getQb64b(),
                            null,
                            this.transferable
                        );
                    } catch (SodiumException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
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
        public CompletableFuture<KeeperResult> incept(boolean transferable) throws SodiumException {
            this.transferable = transferable;

            Manager.Keys signers = creator.create(
                icodes,
                count,
                code,
                this.transferable
            );

            this.prxs = signers.getSigners().stream()
                .map(signer -> {
                    try {
                        return this.encrypter.encrypt(null, signer).getQb64();
                    } catch (SodiumException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

            List<String> verfers = signers.getSigners().stream()
                .map(signer -> signer.getVerfer().getQb64())
                .collect(Collectors.toList());

            Manager.Keys nsigners = creator.create(
                ncodes,
                ncount,
                ncode,
                this.transferable
            );

            this.nxts = nsigners.getSigners().stream()
                .map(signer -> {
                    try {
                        return this.encrypter.encrypt(null, signer).getQb64();
                    } catch (SodiumException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

            List<String> digers = nsigners.getSigners().stream()
                .map(nsigner ->
                    new Diger(
                        RawArgs.builder().
                            code(this.dcode)
                            .build(),
                        nsigner.getVerfer().getQb64b()
                    ).getQb64()
                )
                .collect(Collectors.toList());

            return CompletableFuture.completedFuture(new KeeperResult(verfers, digers));
        }

        @Override
        public CompletableFuture<KeeperResult> rotate(
            List<String> ncodes,
            boolean transferable,
            List<State> states,
            List<State> rstates
        ) throws SodiumException {
            this.ncodes = ncodes;
            this.transferable = transferable;
            this.prxs = this.nxts;

            List<Signer> signers = this.nxts.stream()
                .map(nxt -> {
                    try {
                        return (Signer) this.decrypter.decrypt(
                            null,
                            new Cipher(nxt),
                            this.transferable
                        );
                    } catch (SodiumException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

            List<String> verfers = signers.stream()
                .map(signer -> signer.getVerfer().getQb64())
                .collect(Collectors.toList());

            Manager.Keys nsigners = creator.create(
                this.ncodes,
                this.ncount,
                this.ncode,
                this.transferable
            );

            this.nxts = nsigners.getSigners().stream()
                .map(signer -> {
                    try {
                        return this.encrypter.encrypt(null, signer).getQb64();
                    } catch (SodiumException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

            List<String> digers = nsigners.getSigners().stream()
                .map(nsigner -> new Diger(
                    RawArgs.builder().code(this.dcode).build(),
                    nsigner.getVerfer().getQb64b()
                ).getQb64())
                .collect(Collectors.toList());

            return CompletableFuture.completedFuture(new KeeperResult(verfers, digers));
        }

        @Override
        public CompletableFuture<SignResult> sign(
            byte[] ser,
            Boolean indexed,
            List<Integer> indices,
            List<Integer> ondices
        ) throws SodiumException {
            List<Signer> signers = this.prxs.stream()
                .map(prx -> {
                    try {
                        return (Signer) this.decrypter.decrypt(
                            new Cipher(prx).getQb64b(),
                            null,
                            this.transferable
                        );
                    } catch (SodiumException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

            List<String> signatures;
            if (indexed != null && indexed) {
                signatures = IntStream.range(0, signers.size())
                    .mapToObj(j -> {
                        Signer signer = signers.get(j);
                        int i = indices != null ? indices.get(j) : j;
                        if (i < 0) {
                            throw new InvalidValueException("Invalid signing index = " + i);
                        }
                        int o = ondices != null ? ondices.get(j) : i;
                        if (o < 0) {
                            throw new InvalidValueException("Invalid ondex = " + o);
                        }
                        try {
                            return ((Siger) signer.sign(ser, i, o == 0, o)).getQb64();
                        } catch (SodiumException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            } else {
                signatures = signers.stream()
                    .map(signer -> {
                        try {
                            return ((Cigar) signer.sign(ser)).getQb64();
                        } catch (SodiumException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            }

            return CompletableFuture.completedFuture(new SignResult(signatures));
        }
    }

    @Getter
    public static class GroupKeeper implements Keeper<GroupParams> {
        private final KeyManager manager;
        private final HabState mhab;
        private List<String> gkeys;
        private List<String> gdigs;
        private final Algos algo = Algos.group;
        private final List<Signer> signers;

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
                keys = states.stream()
                    .map(state -> state.getK().getFirst())
                    .collect(Collectors.toList());
            }

            if (rstates != null) {
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
        public CompletableFuture<KeeperResult> incept(boolean transferable) {
            return CompletableFuture.completedFuture(new KeeperResult(gkeys, gdigs));
        }

        @Override
        public CompletableFuture<KeeperResult> rotate(
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

            return CompletableFuture.completedFuture(new KeeperResult(gkeys, gdigs));
        }

        @Override
        public CompletableFuture<SignResult> sign(
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
