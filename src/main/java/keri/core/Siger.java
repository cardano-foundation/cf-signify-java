package keri.core;

import keri.core.args.IndexerArgs;
import keri.core.Codex.IndexedSigCodex;

/**
     Siger is subclass of Indexer, indexed signature material,
     Adds .verfer property which is instance of Verfer that provides
     associated signature verifier.

     See Indexer for inherited attributes and properties:

     Attributes:

     Properties:
     .verfer is Verfer object instance

     Methods:
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