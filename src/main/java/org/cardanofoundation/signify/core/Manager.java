package org.cardanofoundation.signify.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidSizeException;

public class Manager {

    public enum Algos {
        randy,
        salty,
        group,
    }

    static class PubLot {
        List<String> pubs = new ArrayList<>(); // list qb64 public keys.
        int ridx = 0; // index of rotation (est event) that uses public key set
        int kidx = 0; // index of key in sequence of public keys
        String dt = ""; // datetime ISO8601 when key set created
    }

    static class PreSit {
        PubLot old = new PubLot(); //previous publot
        PubLot new_ = new PubLot(); //newly current publot
        PubLot nxt = new PubLot(); //next public publot
    }

    static class PrePrm {
        int pidx = 0; // prefix index for this keypair sequence
        Algos algo = Algos.salty; // salty default uses indices and salt to create new key pairs
        String salt = ""; // empty salt used for salty algo
        String stem = ""; // default unique path stem for salty algo
        String tier = ""; // security tier for stretch index salty algo
    }

    static class PubSet {
        List<String> pubs = new ArrayList<>(); // list qb64 public keys.
    }

    static class PubPath {
        String path = "";
        String code = "";
        String tier = Tier.high.name();
        boolean temp = false;
    }

    @Getter
    public static class Keys {
        private List<Signer> signers;
        private List<String> paths;

        public Keys(List<Signer> signers, List<String> paths) {
            this.signers = signers;

            if (paths != null && signers.size() != paths.size()) {
                throw new InvalidSizeException("If paths are provided, they must be the same length as signers");
            }

            this.paths = paths;
        }

    }

    interface Creator {
        Keys create(List<String> codes, int count, String code, boolean transferable, int pidx, int ridx, int kidx, boolean temp) throws SodiumException;

        String salt();

        String stem();

        Tier tier();
    }

    public static class RandyCreator implements Creator {
        @Override
        public Keys create(
                List<String> codes,
                int count,
                String code,
                boolean transferable,
                int pidx,
                int ridx,
                int kidx,
                boolean temp) throws SodiumException {
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

        public Keys create() throws SodiumException {
            return create(null, 1, MatterCodex.Ed25519_Seed.getValue(), true, 0, 0, 0, false);
        }

        public Keys create(List<String> codes, int count) throws SodiumException {
            return create(codes, count, MatterCodex.Ed25519_Seed.getValue(), true, 0, 0, 0, false);
        }

        public Keys create(List<String> codes) throws SodiumException {
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

        public Salter salter;
        private String stem;

        public SaltyCreator() {
            RawArgs rawArgs = RawArgs.builder()
                    .code(MatterCodex.Salt_128.getValue())
                    .build();
            this.salter = new Salter(rawArgs, Tier.low);
            this.stem = "";
        }

        public SaltyCreator(String salt, Tier tier, String stem) {
            this.salter = new Salter(salt, tier);
            this.stem = stem == null ? "" : stem;
        }

        @Override
        public Keys create(List<String> codes, int count, String code, boolean transferable, int pidx, int ridx, int kidx, boolean temp) throws SodiumException {
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

        public Keys create() throws SodiumException {
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
}
