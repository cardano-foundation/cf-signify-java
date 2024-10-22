package keri.core;

public class Salter extends Matter {

    public Tier tier;
    
    public Salter(SalterArgs args) {
        super(args.toMatterArgs());
        //TODO Auto-generated constructor stub
    }

    enum Tier {
        low,
        med,
        high
    }

    public class SalterArgs {
        byte[] raw;
        String code;
        Tier tier;
        byte[] qb64b;
        String qb64;
        byte[] qb2;

        public MatterArgs toMatterArgs() {
            return new MatterArgs(raw, code, qb64b, qb64, qb2);
        }
       
    }

    public Tier getTier() {
        return this.tier;
    }
}
