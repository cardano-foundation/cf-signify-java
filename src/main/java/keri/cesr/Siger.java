package keri.cesr;

import keri.cesr.args.IndexerArgs;
import keri.cesr.Codex.IndexedSigCodex;

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

public class Siger extends Indexer {
    private Verfer _verfer;

    public Siger(IndexerArgs args, Verfer verfer) {
        super(args);

        if (!IndexedSigCodex.has(this.getCode())) {
            throw new RuntimeException("Invalid code = " + this.getCode() + " for Siger.");
        }
        this._verfer = verfer;
    }

    public Verfer getVerfer() {
        return this._verfer;
    }

    public void setVerfer(Verfer verfer) {
        this._verfer = verfer;
    }
} 