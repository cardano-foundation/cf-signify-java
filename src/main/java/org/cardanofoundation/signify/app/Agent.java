package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Diger;
import org.cardanofoundation.signify.cesr.Tholder;
import org.cardanofoundation.signify.cesr.Verfer;
import org.cardanofoundation.signify.cesr.CesrNumber;
import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
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

    @SuppressWarnings("unchecked")
    private void parse(Object agent) {
        Object[] result = this.event(agent);
        Map<String, Object> state = (Map<String, Object>) result[0];
        Verfer verfer = (Verfer) result[1];

        this.sn = new CesrNumber(null, null, (String) state.get("s")).getNum();
        this.said = (String) state.get("d");

        if (!Ilks.DIP.getValue().equals(state.get("et"))) {
            throw new IllegalStateException("invalid inception event type " + state.get("et"));
        }

        this.pre = (String) state.get("i");
        if (!state.containsKey("di")) {
            throw new IllegalStateException("no anchor to controller AID");
        }

        this.anchor = (String) state.get("di");

        this.verfer = verfer;
        this.state = state;
    }

    @SuppressWarnings("unchecked")
    private Object[] event(Object evt) {
        Map<String, Object> event = (Map<String, Object>) evt;
        List<String> keys = (List<String>) event.get("k");
        List<String> nextKeys = (List<String>) event.get("n");

        if (keys.size() != 1) {
            throw new IllegalStateException("agent inception event can only have one key");
        }

        Verfer verfer = new Verfer(
            MatterArgs.builder()
                .qb64(keys.getFirst())
                .build()
        );

        if (nextKeys.size() != 1) {
            throw new IllegalStateException("agent inception event can only have one next key");
        }

        Diger diger = new Diger(
            MatterArgs.builder()
                .qb64(nextKeys.getFirst())
                .build(),
            null
        );

        Tholder tholder = new Tholder(null, null, event.get("kt"));
        if (tholder.getNum() != 1) {
            throw new IllegalStateException("invalid threshold " + tholder.getNum() + ", must be 1");
        }

        Tholder ntholder = new Tholder(null, null, event.get("nt"));
        if (ntholder.getNum() != 1) {
            throw new IllegalStateException(
                "invalid next threshold " + ntholder.getNum() + ", must be 1"
            );
        }

        return new Object[]{event, verfer, diger};
    }
}
