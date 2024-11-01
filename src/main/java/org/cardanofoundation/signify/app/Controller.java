package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.args.SalterArgs;

import java.util.ArrayList;
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

    public Controller(String bran, Tier tier, int ridx, Object state) {
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

        this.signer = creator.create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx,
                0,
                false
        ).getSigners().removeFirst();

        this.nsigner = creator.create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx + 1,
                0,
                false
        ).getSigners().removeFirst();

        this.keys = new ArrayList<>();
        this.keys.add(this.signer.getVerfer().getQb64());
        this.ndigs = new ArrayList<>();
        this.ndigs.add(
            new Diger(
                MatterArgs.builder().code(MatterCodex.Blake3_256.getValue()).build(),
                this.nsigner.getVerfer().getQb64b()).getQb64()
        );

        if (state == null || ((Map<String, Object>) state).get("ee").get("s").equals("0")) {
            //TODO Implement Eventing
            this.serder = Eventing.incept(
                    this.keys,
                    "1",
                    "1",
                    this.ndigs,
                    MatterCodex.Blake3_256,
                    "0",
                    new ArrayList<>()
            );
        } else {
            //TODO Implement Serder
            this.serder = new Serder(((Map<String, Object>) state).get("ee"));
        }
    }

}
