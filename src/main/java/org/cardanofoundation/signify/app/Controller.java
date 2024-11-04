package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.args.SalterArgs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Controller is responsible for managing signing keys for the client and agent.  The client
 * signing key represents the Account for the client on the agent
 */
public class Controller {
    private String bran;
    public String stem;
    public Tier tier;
    public int ridx;
    public Salter salter;
    public Signer signer;
    private Signer nsigner;
    public Serder serder;
    private List<String> keys;
    public List<String> ndigs;

    @SuppressWarnings("unchecked")
    public Controller(String bran, Tier tier, Integer ridx, Map<String, Object> state) {
        ridx = ridx == null ? 0 : ridx;
        this.bran = MatterCodex.Salt_128 + "A" + bran.substring(0, 21); // qb64 salt for seed
        this.stem = "signify:controller";
        this.tier = tier;
        this.ridx = ridx;

        this.salter = new Salter(SalterArgs.builder()
            .qb64(this.bran)
            .tier(this.tier)
            .build());

        Manager.SaltyCreator creator = new Manager().new SaltyCreator(
                this.salter.getQb64(),
                this.tier,
                this.stem
        );

        this.signer = new LinkedList<>(creator.create(
            null,
            1,
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            0,
            this.ridx,
            0,
            false
        ).getSigners()).pop();

        this.nsigner = new LinkedList<>(creator.create(
            null,
            1,
            MatterCodex.Ed25519_Seed.getValue(),
            true,
            0,
            this.ridx + 1,
            0,
            false
        ).getSigners()).pop();

        this.keys = List.of(this.signer.getVerfer().getQb64());
        this.ndigs = List.of(
            new Diger(
                MatterArgs.builder().code(MatterCodex.Blake3_256.getValue()).build(),
                this.nsigner.getVerfer().getQb64b()
            ).getQb64()
        );

        Object ee = state == null ? null : state.get("ee");
        if (state == null || ((Map<String, Object>) ee).get("s").equals("0")) {
            InceptArgs args = InceptArgs.builder()
                .keys(this.keys)
                .isith("1")
                .nsith("1")
                .ndigs(this.ndigs)
                .code(MatterCodex.Blake3_256.getValue())
                .toad("0")
                .wits(new ArrayList<>())
                .build();
            this.serder = Eventing.incept(args);
        } else {
            this.serder = new Serder((Map<String, Object>) ee, null, null);
        }
    }

    public String getPre() {
        return this.serder.getPre();
    }

    public EventResult getEvent() throws Exception {
        Siger siger = (Siger) this.signer.sign(
            this.serder.get_raw().getBytes(),
            0,
            null,
            null);
        return new EventResult(this.serder, siger);
    }

    public record EventResult(Serder serder, Siger siger) {}

    public Verfer[] getVerfers() {
        return new Verfer[]{this.signer.getVerfer()};
    }

    //TODO implement the rest of the functions
    public void approveDelegation() {}

    public void derive() {}

    public void rotate() {}

    public void recrypt() {}
}
