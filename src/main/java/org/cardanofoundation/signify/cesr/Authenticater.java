package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import org.cardanofoundation.signify.core.Httping;

import java.util.List;


@Getter
public class Authenticater {
    private static final List<String> DEFAULT_FIELDS = List.of(
        "@method",
        "@path",
        "signify-resource",
        Httping.HEADER_SIG_TIME.toLowerCase()
    );

    private final Verfer verfer;
    private final Signer csig;

    public Authenticater(Signer csig, Verfer verfer) {
        this.csig = csig;
        this.verfer = verfer;
    }

    public boolean verify() {
        return true;
    }

}
