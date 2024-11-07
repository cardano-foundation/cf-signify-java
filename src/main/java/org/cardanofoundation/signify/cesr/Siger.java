package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.args.IndexerArgs;
import org.cardanofoundation.signify.cesr.Codex.IndexedSigCodex;

/**
 * Siger is subclass of Indexer, indexed signature material,
 * Adds .verfer property which is instance of Verfer that provides
 * associated signature verifier.
 * <p>
 * See Indexer for inherited attributes and properties:
 * <p>
 * Attributes:
 * <p>
 * Properties:
 * .verfer is Verfer object instance
 * <p>
 * Methods:
 **/

@Getter
@Setter
public class Siger extends Indexer {
    private Verfer verfer;

    public Siger(IndexerArgs args, Verfer verfer) {
        super(args);

        if (!IndexedSigCodex.has(this.getCode())) {
            throw new RuntimeException("Invalid code = " + this.getCode() + " for Siger.");
        }
        this.verfer = verfer;
    }

}