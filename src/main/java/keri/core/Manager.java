package keri.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import keri.core.Salter.Tier;

public class Manager {

    enum Algos {
        randy,
        salty,
        group,
        extern
    }

    class PubLot {
        List<String> pubs = new ArrayList<>(); // list qb64 public keys.
        int ridx = 0; // index of rotation (est event) that uses public key set
        int kidx = 0; // index of key in sequence of public keys
        String dt = ""; // datetime ISO8601 when key set created
    }

    class PreSit {
        PubLot old = new PubLot(); //previous publot
        PubLot new_ = new PubLot(); //newly current publot
        PubLot nxt = new PubLot(); //next public publot
    }

    class PrePrm {
        int pidx = 0; // prefix index for this keypair sequence
        Algos algo = Algos.salty; // salty default uses indices and salt to create new key pairs
        String salt = ""; // empty salt used for salty algo
        String stem = ""; // default unique path stem for salty algo
        String tier = ""; // security tier for stretch index salty algo
    }

    class PubSet {
        List<String> pubs = new ArrayList<>(); // list qb64 public keys.
    }

    class PubPath {
        String path = "";
        String code = "";
        String tier = Tier.high.name();
        boolean temp = false;
    }

    class Keys {
        private List<Signer> signers;
        private List<String> paths;

        public Keys(List<Signer> signers, List<String> paths) {
            this.signers = signers;

            if(paths != null && signers.size() != paths.size()) {
                throw new IllegalArgumentException("'If paths are provided, they must be the same length as signers'");
            }
           
            this.paths = paths;
        }

        public List<String> getPaths() {
            return this.paths;
        }

        public List<Signer> getSigners() {
            return this.signers;
        }

    }

    interface Creator {
        Keys create(List<String> codes, int count, String code, boolean transferable, int pidx, int ridx, int kidx, boolean temp);
        String salt();
        String stem();
        Tier tier();
    }

    public class RandyCreator implements Creator {
        @Override
        public Keys create(List<String> codes, int count, String code, boolean transferable, int pidx, int ridx, int kidx, boolean temp) {
            // TODO: Implement RandyCreator create
            return null;
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

    public class SaltyCreator implements Creator {

        public Salter salter;
        private String _stem;

        public SaltyCreator(String salt, Tier tier, String stem) {
        }

        @Override
        public Keys create(List<String> codes, int count, String code, boolean transferable, int pidx, int ridx, int kidx, boolean temp) {
            // TODO: Implement SaltyCreator create
            return null;
        }

        @Override
        public String salt() {
            return this.salter.getQb64();
        }

        @Override
        public String stem() {
            return this._stem;
        }

        @Override
        public Tier tier() {
            return this.salter.getTier();
        }
    }

    // TODO: Implement remaining creator classes
}
