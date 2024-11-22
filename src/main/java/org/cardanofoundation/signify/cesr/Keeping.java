package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;
import org.cardanofoundation.signify.cesr.params.KeeperParams;
import org.cardanofoundation.signify.cesr.params.SaltyParams;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States.State;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Keeping {
    public interface ExternalModuleType {
        Keeper createKeeper(int pidx, KeeperParams args);
    }

    public record ExternalModule(
        String type,
        String name,
        ExternalModuleType module
    ) {}

    public record KeeperResult(List<String> first, List<String> second) {}

    public record SignResult(List<String> result) {}

    public interface Keeper<T extends KeeperParams> {
        Manager.Algos getAlgo();
        List<Signer> getSigners();
        T getParams();
        CompletableFuture<KeeperResult> incept(boolean transferable);
        CompletableFuture<KeeperResult> rotate(
            List<String> ncodes,
            boolean transferable,
            List<State> states,
            List<State> rstates
        );
        CompletableFuture<SignResult> sign(
            byte[] ser,
            boolean indexed,
            List<Integer> indices,
            List<Integer> ondices
        );
    }

    public static class KeyManager {
        private Salter salter;
        private Map<String, ExternalModuleType> modules = new HashMap<>();

        public KeyManager(Salter salter, List<ExternalModule> externalModules) {
            this.salter = salter;
            for (ExternalModule mod : externalModules) {
                modules.put(mod.type(), mod.module());
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
        private final int kidx;
        private final Tier tier;
        private final boolean transferable;
        private final String stem;
        private final String code;
        private final int count;
        private final List<String> icodes;
        private final String ncode;
        private final int ncount;
        private final List<String> ncodes;
        private final String dcode;
        private final String sxlt;
        private final String bran;
        private final Manager.SaltyCreator creator;
        private final Manager.Algos algo = Manager.Algos.salty;
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
            // # Salter is the entered passcode and used for enc/dec of salts for each AID
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
                .sxlt(this.sxlt)
                .pidx(this.pidx)
                .kidx(this.kidx)
                .stem(this.stem)
                .tier(this.tier)
                .icodes(this.icodes)
                .ncodes(this.ncodes)
                .dcode(this.dcode)
                .transferable(this.transferable)
                .build();
        }

        @Override
        public CompletableFuture<KeeperResult> incept(boolean transferable) {
            // Implementation needed
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<KeeperResult> rotate(
            List<String> ncodes, boolean transferable,
            List<State> states, List<State> rstates
        ) {
            // Implementation needed
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<SignResult> sign(
            byte[] ser, boolean indexed,
            List<Integer> indices, List<Integer> ondices
        ) {
            // Implementation needed
            return CompletableFuture.completedFuture(null);
        }
    }
}
