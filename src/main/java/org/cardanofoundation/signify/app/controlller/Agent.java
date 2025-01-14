package org.cardanofoundation.signify.app.controlller;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Tholder;
import org.cardanofoundation.signify.cesr.Verfer;
import org.cardanofoundation.signify.cesr.CesrNumber;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.IlkException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Agent is a custodial entity that can be used in conjunction with a local Client to establish the
 * KERI "signing at the edge" semantic
 */
@Getter
public class Agent {
    private final String pre;
    private final String anchor;
    private final Verfer verfer;
    private final Map<String, Object> state;
    private final BigInteger sn;
    private final String said;

    public Agent(Object agent) {
        Map<String, Object> event = Utils.toMap(agent);
        List<String> keys = Utils.toList(event.get("k"));
        List<String> nextKeys = Utils.toList(event.get("n"));

        if (keys.size() != 1) {
            throw new IllegalArgumentException("agent inception event can only have one key");
        }

        this.verfer = new Verfer(keys.getFirst());

        if (nextKeys.size() != 1) {
            throw new IllegalArgumentException("agent inception event can only have one next key");
        }


        Tholder tholder = new Tholder(null, null, event.get("kt"));
        if (tholder.getNum() != 1) {
            throw new InvalidValueException("invalid threshold " + tholder.getNum() + ", must be 1");
        }

        Tholder ntholder = new Tholder(null, null, event.get("nt"));
        if (ntholder.getNum() != 1) {
            throw new InvalidValueException(
                "invalid next threshold " + ntholder.getNum() + ", must be 1"
            );
        }

        this.state = event;
        this.sn = new CesrNumber(RawArgs.builder().build(), null, (String) state.get("s")).getNum();
        this.said = (String) state.get("d");

        if (!Ilks.DIP.getValue().equals(state.get("et"))) {
            throw new IlkException("invalid inception event type " + state.get("et"));
        }

        this.pre = (String) state.get("i");
        if (!state.containsKey("di")) {
            throw new NoSuchElementException("no anchor to controller AID");
        }

        this.anchor = (String) state.get("di");
    }
}
