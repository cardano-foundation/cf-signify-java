package org.cardanofoundation.signify.cesr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ident;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;

public class Eventing {
    //TODO implement function
    public static Serder interact(InteractArgs args) {
        return new Serder(null, null, null);
    }

    public static Serder incept(InceptArgs args) {
        final String vs = CoreUtil.versify(Ident.KERI, args.getVersion(), args.getKind(), 0);
        final Ilks ilk = args.getDelpre() == null ? Ilks.ICP : Ilks.DIP;
        final CesrNumber sner = new CesrNumber(BigInteger.ZERO);
    
        if(args.getIsith() == null) {
            args.setIsith(Math.max(1, Math.ceil((double) args.getKeys().size() / 2)));
        }

        final Tholder tholder = new Tholder(null, null, args.getIsith());
        if(tholder.getNum() != null && tholder.getNum() < 1) {
            throw new IllegalArgumentException("Invalid sith = " + tholder.getNum() + " less than 1.");
        }
        if(tholder.getSize() > args.getKeys().size()) {
            throw new IllegalArgumentException("Invalid sith = " + tholder.getNum() + " for keys " + args.getKeys());
        }

        if(args.getNdigs() == null) {
            args.setNdigs(new ArrayList<>());
        }

        if(args.getNsith() == null) {
            args.setNsith(Math.max(0, Math.ceil((double) args.getNdigs().size() / 2)));
        }

        final Tholder nTholder = new Tholder(null, null, args.getNsith());
        if(nTholder.getNum() != null && nTholder.getNum() < 0) {
            throw new IllegalArgumentException("Invalid nsith = " + nTholder.getNum() + " less than 0.");
        }
        if(nTholder.getSize() > args.getNdigs().size()) {
            throw new IllegalArgumentException("Invalid nsith = " + nTholder.getNum() + " for keys " + args.getNdigs());
        }

        final List<String> wits = args.getWits() == null ? new ArrayList<>() : args.getWits();
        final Set<String> witsSet = new HashSet<>(wits);
        if(witsSet.size() != wits.size()) {
            throw new IllegalArgumentException("Invalid wits = " + wits + ", has duplicates.");
        }

        if(args.getToad() == null) {
            args.setToad(!wits.isEmpty() ? ample(wits.size(), null, true) : 0);
        }

        final CesrNumber toader = new CesrNumber(BigInteger.valueOf((Integer) args.getToad()));
        if(!wits.isEmpty()) {
            if(toader.getNum().compareTo(BigInteger.ONE) < 0 || toader.getNum().compareTo(BigInteger.valueOf(wits.size())) > 0) {
                throw new IllegalArgumentException("Invalid toad = " + toader.getNum() + " for wits = " + wits);
            }
        } else {
            if(!toader.getNum().equals(BigInteger.ZERO)) {
                throw new IllegalArgumentException("Invalid toad = " + toader.getNum() + " for wits = " + wits);
            }
        }

        final List<String> cnfg = args.getCnfg() == null ? new ArrayList<>() : args.getCnfg();
        final List<Object> data = args.getData() == null ? new ArrayList<>() : args.getData();

        Map<String, Object> ked = new HashMap<>();
        ked.put("v", vs);
        ked.put("t", ilk);
        ked.put("d", "");
        ked.put("i", "");
        ked.put("s", sner.getNumh());
        ked.put("kt", args.getIntive() && tholder.getNum() != null ? tholder.getNum() : tholder.getSith());
        ked.put("k", args.getKeys());
        ked.put("nt", args.getIntive() && nTholder.getNum() != null ? nTholder.getNum() : nTholder.getSith());
        ked.put("n", args.getNdigs());
        ked.put("bt", args.getIntive() ? toader.getNum() : toader.getNumh());
        ked.put("b", wits);
        ked.put("c", args.getCnfg());
        ked.put("a", data);

        if(args.getDelpre() != null) {
            ked.put("di", args.getDelpre());
            ked.put("code", Codex.MatterCodex.Blake3_256.getValue());
        }

        Prefixer prefixer;
        if(args.getDelpre() == null && args.getCode() == null && args.getKeys().size() == 1) {
            prefixer = new Prefixer(args.getKeys().get(0));
        } else {
            prefixer = new Prefixer(args.getCode(), ked);
            if(args.getDelpre() != null) {
                if(!prefixer.isDigestible()) {
                    throw new IllegalArgumentException("Invalid derivation code = " + args.getCode() + " for delegation. Must be digestive");
                }
            }
        }

        ked.put("i", prefixer.getQb64());
        if(prefixer.isDigestible()) {
            ked.put("d", prefixer.getQb64());
        } else {
            Saider.SaidifyResult saidifyResult = Saider.saidify(ked);
            ked = saidifyResult.sad();
        }

        return new Serder(ked);
    }

    private static int ample(Integer n, Integer f, boolean weak) {
        n = Math.max(0, n);
        int f1;
        if(f == null) {
            f1 = Math.max(1, (int) Math.floor((double) Math.max(0, n - 1) / 3));

            int f2 = Math.max(1, (int) Math.ceil((double) Math.max(0, n - 1) / 3));
            if(weak) {
                int m1 = (int) Math.ceil((double) (n + f1 + 1) / 2);
                int m2 = (int) Math.ceil((double) (n + f2 + 1) / 2);
                return Math.min(n, Math.min(m1, m2));
            } else {
                int m1 = (int) Math.max(0, n - f1);
                int m2 = (int) Math.ceil((double) (n + f1 + 1) / 2);
                return Math.min(n, Math.max(m1, m2));
            }
        } else {
            int m1 = (int) Math.ceil((double) (n + f + 1) / 2);
            int m2 = (int) Math.max(0, n - f);
            if(m2 < m1 && n > 0) {
                throw new IllegalArgumentException("Invalid f = " + f + " is too big for n = " + n + ".");
            }
            if(weak) {
                return Math.min(n, Math.min(m1, m2));
            } else {
                return Math.min(n, Math.max(m1, m2));
            }
        }
    }
}
