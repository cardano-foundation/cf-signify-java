package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.MatterArgs;

public class Cigar extends Matter {
    private Verfer _verfer;

    public Cigar(MatterArgs args, Verfer verfer) {
        super(args);
        this._verfer = verfer;
    }

    public Verfer getVerfer() {
        return _verfer;
    }

    public void setVerfer(Verfer verfer) {
        this._verfer = verfer;
    }
}
