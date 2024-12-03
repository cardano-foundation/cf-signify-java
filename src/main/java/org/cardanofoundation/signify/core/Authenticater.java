package org.cardanofoundation.signify.core;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.cesr.Verfer;

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

    // TODO implement in next PR
    public boolean verify() {
        return true;
    }

}
