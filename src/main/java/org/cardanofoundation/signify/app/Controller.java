package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Controller is responsible for managing signing keys for the client and agent.  The client
 * signing key represents the Account for the client on the agent
 */
@Getter
@Setter
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

    public Controller(String bran, Tier tier, Integer ridx, Object state) throws SodiumException {
        ridx = ridx == null ? 0 : ridx;
        this.bran = MatterCodex.Salt_128.getValue() + "A" + bran.substring(0, 21); // qb64 salt for seed
        this.stem = "signify:controller";
        this.tier = tier;
        this.ridx = ridx;

        this.salter = new Salter(this.bran, this.tier);

        Manager.SaltyCreator creator = new Manager().new SaltyCreator(
                this.salter.getQb64(),
                this.tier,
                this.stem
        );

        this.signer = new LinkedList<>(creator
            .create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx,
                0,
                false
            ).getSigners()).pop();

        this.nsigner = new LinkedList<>(creator
            .create(
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

        RawArgs rawArgs = RawArgs.builder()
            .code(MatterCodex.Blake3_256.getValue())
            .build();
        this.ndigs = List.of(
            new Diger(rawArgs, this.nsigner.getVerfer().getQb64b())
                .getQb64()
        );

        Map<String, Object> stateMap = Utils.toMap(state);
        Map<String, Object> ee = Utils.toMap(stateMap.get("ee"));
        if (stateMap.isEmpty() || ee.get("s").equals("0")) {
            InceptArgs args = InceptArgs.builder()
                .keys(this.keys)
                .isith("1")
                .nsith("1")
                .ndigs(this.ndigs)
                .code(MatterCodex.Blake3_256.getValue())
                .toad(0)
                .wits(new ArrayList<>())
                .build();
            this.serder = Eventing.incept(args);
        } else {
            this.serder = new Serder(ee);
        }
    }

    public String getPre() {
        return this.serder.getPre();
    }

    public EventResult getEvent() throws Exception {
        Siger siger = (Siger) this.signer.sign(
            this.serder.getRaw().getBytes(),
            0);
        return new EventResult(this.serder, siger);
    }

    public record EventResult(Serder evt, Siger sign) {}

    public Verfer[] getVerfers() {
        return new Verfer[]{this.signer.getVerfer()};
    }

    //TODO implement the rest of the functions
    public Object approveDelegation(Agent agent) {
        return null;
    }

    public void derive() {}

    public void rotate() {}

    public void recrypt() {}
}
