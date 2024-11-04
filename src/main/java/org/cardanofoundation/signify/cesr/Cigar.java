package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.args.MatterArgs;

@Getter
@Setter
public class Cigar extends Matter {
    private Verfer verfer;

    public Cigar(MatterArgs args, Verfer verfer) {
        super(args);
        this.verfer = verfer;
    }
}
