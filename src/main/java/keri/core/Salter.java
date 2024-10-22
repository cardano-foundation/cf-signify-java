package keri.core;

import keri.core.args.SalterArgs;

public class Salter extends Matter {

    public Tier tier;

    public Salter(SalterArgs args) {
        super(args.toMatterArgs());
        // TODO Auto-generated constructor stub
    }

    public enum Tier {
        low,
        med,
        high
    }

    public Tier getTier() {
        return this.tier;
    }
}
