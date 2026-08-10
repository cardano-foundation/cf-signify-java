package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.args.RawArgs;

@Getter
@Setter
public class Cigar extends Matter {
    private Verfer verfer;

    public Cigar(String qb64) {
        this(qb64, null);
    }

    public Cigar(RawArgs args, Verfer verfer) {
        super(args);
        this.verfer = verfer;
    }

    public Cigar(String qb64, Verfer verfer) {
        super(qb64);
        this.verfer = verfer;
    }
}
