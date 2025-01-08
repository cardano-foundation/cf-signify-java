package org.cardanofoundation.signify.app.habery;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;

import java.security.DigestException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Habery {
    private String name;
    private Manager mgr;
    private Map<String, Hab> habs = new LinkedHashMap<>();

    public Habery(HaberyArgs args) throws SodiumException {
        this.name = args.getName();
        if (args.getPasscode() != null && args.getSeed() == null) {
            if (args.passcode.length() < 21) {
                throw new IllegalArgumentException("Bran (passcode seed material) too short.");
            }

            String bran = Codex.MatterCodex.Salt_128.getValue() + "A" + args.passcode.substring(0, 21); // qb64 salt for seed
            Signer signer = new Salter(bran).signer(
                    Codex.MatterCodex.Ed25519_Seed.getValue(),
                    false,
                    "",
                    null,
                    false
            );

            args.setSeed(signer.getQb64());
            if (args.getAeid() == null) {
                args.setAeid(signer.getVerfer().getQb64()); // qb64 salt for seed
            }
        }

        Manager.Algos algo;
        Salter salter = args.getSalt() != null ? new Salter(args.getSalt()) : null;
        if (salter != null) {
            algo = Manager.Algos.salty;
        } else {
            algo = Manager.Algos.randy;
        }

        Manager.ManagerArgs managerArgs = Manager.ManagerArgs.builder()
                .seed(args.getSeed())
                .aeid(args.getAeid())
                .pidx(args.getPidx())
                .algo(algo)
                .salter(salter)
                .build();
        this.mgr = new Manager(managerArgs);
    }

    public Manager getMgr() {
        return this.mgr;
    }

    public List<Hab> getHabs() {
        return this.habs.values().stream().toList();
    }

    public Hab habByName(String name) {
        return this.habs.get(name);
    }

    public Hab makeHab(MakeHabArgs args) throws SodiumException, DigestException {
        if (args.nsith == null) {
            args.nsith = args.isith;
        }

        if (args.ncount == null) {
            args.ncount = args.icount;
        }

        if (!args.transferable) {
            args.ncount = 0;
            args.nsith = "0";
            args.code = Codex.MatterCodex.Ed25519N.getValue();
        }

        Manager.ManagerInceptArgs mgrArgs = Manager.ManagerInceptArgs.builder()
                .icount(args.icount)
                .ncount(args.ncount)
                .stem(this.getName())
                .transferable(args.transferable)
                .temp(false)
                .build();
        Manager.ManagerInceptResult managerInceptResult = this.mgr.incept(mgrArgs);
        List<Verfer> verfers = managerInceptResult.verfers();
        List<Diger> digers = managerInceptResult.digers();

        args.icount = verfers.size();
        args.ncount = digers != null ? digers.size() : 0;
        if (args.isith == null) {
            args.isith = Integer.toHexString(Math.max(1, (int) Math.ceil(args.icount / 2.0)));
        }
        if (args.nsith == null) {
            args.nsith = Integer.toHexString(Math.max(1, (int) Math.ceil(args.ncount / 2.0)));
        }

        List<String> cnfg = new ArrayList<>();
        if (args.estOnly) {
            cnfg.add(TraitCodex.EstOnly.getValue());
        }
        if (args.DnD) {
            cnfg.add(TraitCodex.DoNotDelegate.getValue());
        }

        List<String> keys = verfers.stream().map(Verfer::getQb64).toList();
        List<String> ndigs = digers.stream().map(Diger::getQb64).toList();

        InceptArgs inceptArgs = InceptArgs.builder()
                .keys(keys)
                .isith(args.isith)
                .ndigs(ndigs)
                .nsith(args.nsith)
                .toad(args.toad)
                .wits(args.wits)
                .cnfg(cnfg)
                .data(args.data)
                .code(args.code)
                .delpre(args.delpre)
                .build();
        Serder icp = Eventing.incept(inceptArgs);
        Hab hab = new Hab(name, icp);
        this.habs.put(name, hab);
        return hab;
    }

}
