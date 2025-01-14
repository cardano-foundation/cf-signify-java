package org.cardanofoundation.signify.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.habery.TraitCodex;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.Prefixer;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.CoreUtil;

import java.security.DigestException;
import java.util.*;

import static org.cardanofoundation.signify.cesr.util.CoreUtil.Versionage;
import static org.cardanofoundation.signify.cesr.util.CoreUtil.versify;
import static org.cardanofoundation.signify.core.Eventing.ample;

public class Vdring {

    public static Serder incept(VDRInceptArgs args) throws DigestException {
        String vs = versify(CoreUtil.Ident.KERI, args.version, args.kind, 0);
        int isn = 0;
        String ilk = CoreUtil.Ilks.VCP.getValue();

        if (args.cnfg.contains(TraitCodex.NoBackers.getValue()) && !args.baks.isEmpty()) {
            throw new IllegalArgumentException(args.baks.size() + " backers specified for NB vcp, 0 allowed");
        }

        Set<String> baksSet = new HashSet<>(args.baks);
        if (baksSet.size() < args.baks.size()) {
            throw new IllegalArgumentException("Invalid baks " + args.baks + " has duplicates");
        }

        int _toad;
        if (args.toad == null) {
            _toad = args.baks.isEmpty() ? 0 : ample(args.baks.size());
        } else {
            _toad = Integer.parseInt(args.toad.toString());
        }

        if (!args.baks.isEmpty()) {
            if (_toad < 1 || _toad > args.baks.size()) {
                throw new IllegalArgumentException("Invalid toad " + _toad + " for baks in " + args.baks);
            }
        } else {
            if (_toad != 0) {
                throw new IllegalArgumentException("Invalid toad " + _toad + " for no baks");
            }
        }

        Map<String, Object> ked = new LinkedHashMap<>();
        ked.put("v", vs);
        ked.put("t", ilk);
        ked.put("d", "");
        ked.put("i", "");
        ked.put("ii", args.getPre());
        ked.put("s", "" + isn);
        ked.put("c", args.getCnfg());
        ked.put("bt", Integer.toHexString(_toad));
        ked.put("b", args.getBaks());
        ked.put("n", args.getNonce());


        Prefixer prefixer = new Prefixer(args.code, ked);
        ked.put("i", prefixer.getQb64());
        ked.put("d", prefixer.getQb64());

        return new Serder(ked);
    }

    @Getter
    @Setter
    @Builder
    public static class VDRInceptArgs {
        private String pre;
        private Object toad; // Can be Integer or String

        @Builder.Default
        private String nonce = Coring.randomNonce();
        @Builder.Default
        private List<String> baks = new ArrayList<>();
        @Builder.Default
        private List<String> cnfg = new ArrayList<>();
        @Builder.Default
        private CoreUtil.Version version = Versionage;
        @Builder.Default
        private CoreUtil.Serials kind = CoreUtil.Serials.JSON;
        @Builder.Default
        private String code = Codex.MatterCodex.Blake3_256.getValue();
    }
}
