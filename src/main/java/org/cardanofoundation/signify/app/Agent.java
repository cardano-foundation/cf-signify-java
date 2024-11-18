package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Diger;
import org.cardanofoundation.signify.cesr.Tholder;
import org.cardanofoundation.signify.cesr.Verfer;
import org.cardanofoundation.signify.cesr.CesrNumber;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Agent is a custodial entity that can be used in conjunction with a local Client to establish the
 * KERI "signing at the edge" semantic
 */
@Getter
public class Agent {
    private String pre;
    private String anchor;
    private Verfer verfer;
    private Map<String, Object> state;
    private BigInteger sn;
    private String said;

    public Agent(Object agent) {
        this.pre = "";
        this.anchor = "";
        this.verfer = null;
        this.state = null;
        this.sn = BigInteger.ZERO;
        this.said = "";
        this.parse(agent);
    }

    private void parse(Object agent) {
        EventResult result = this.event(agent);
        Map<String, Object> state = Utils.toMap(result.state);
        Verfer verfer = result.verfer;

        this.sn = new CesrNumber(
            RawArgs.builder().build(), null, (String) state.get("s")
        ).getNum();
        this.said = (String) state.get("d");

        if (!Ilks.DIP.getValue().equals(state.get("et"))) {
            throw new IllegalArgumentException("invalid inception event type " + state.get("et"));
        }

        this.pre = (String) state.get("i");
        if (!state.containsKey("di")) {
            throw new IllegalArgumentException("no anchor to controller AID");
        }

        this.anchor = (String) state.get("di");

        this.verfer = verfer;
        this.state = state;
    }

    private EventResult event(Object evt) {
        Map<String, Object> event = Utils.toMap(evt);
        List<String> keys = Utils.toList(event.get("k"));
        List<String> nextKeys = Utils.toList(event.get("n"));

        if (keys.size() != 1) {
            throw new IllegalArgumentException("agent inception event can only have one key");
        }

        Verfer verfer = new Verfer(keys.getFirst());

        if (nextKeys.size() != 1) {
            throw new IllegalArgumentException("agent inception event can only have one next key");
        }

        Diger diger = new Diger(nextKeys.getFirst());

        Tholder tholder = new Tholder(null, null, event.get("kt"));
        if (tholder.getNum() != 1) {
            throw new IllegalArgumentException("invalid threshold " + tholder.getNum() + ", must be 1");
        }

        Tholder ntholder = new Tholder(null, null, event.get("nt"));
        if (ntholder.getNum() != 1) {
            throw new IllegalArgumentException(
                "invalid next threshold " + ntholder.getNum() + ", must be 1"
            );
        }

        return new EventResult(event, verfer, diger);
    }

    private record EventResult(Object state, Verfer verfer, Diger diger) {}
}
