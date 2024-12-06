package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Codex.IndexedSigCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeException;
import org.cardanofoundation.signify.cesr.exceptions.validation.ValidationException;

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

    public Siger(RawArgs rawArgs, Integer index, Integer ondex, Verfer verfer) {
        super(rawArgs, index, ondex);

        if (!IndexedSigCodex.has(this.getCode())) {
            throw new InvalidCodeException("Invalid code = " + this.getCode() + " for Siger.");
        }
        this.verfer = verfer;
    }

    public Siger(String qb64) {
        super(qb64);

        if (!IndexedSigCodex.has(this.getCode())) {
            throw new InvalidCodeException("Invalid code = " + this.getCode() + " for Siger.");
        }
    }
}