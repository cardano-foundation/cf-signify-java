package org.cardanofoundation.signify.core;

import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ident;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.util.*;

import static org.cardanofoundation.signify.cesr.util.CoreUtil.versify;

public class Eventing {
    public static Serder interact(InteractArgs args) throws DigestException {
        String vs = versify(
            Ident.KERI, 
            args.getVersion(), 
            args.getKind(), 
            0
        );
        String ilk = Ilks.IXN.getValue();
        CesrNumber sner = new CesrNumber(args.getSn());

        if (sner.getNum().compareTo(BigInteger.ONE) < 0) {
            throw new InvalidValueException("Invalid sn = " + sner.getNumh() + "for ixn.");
        }

        List<Object> data = args.getData();
        if (data == null) {
            data = new ArrayList<>();
        }

        Map<String, Object> ked = new LinkedHashMap<>();
        ked.put("v", vs);
        ked.put("t", ilk);
        ked.put("d", "");
        ked.put("i", args.getPre());
        ked.put("s", sner.getNumh());
        ked.put("p", args.getDig());
        ked.put("a", data);

        Saider.SaidifyResult result = Saider.saidify(ked);

        return new Serder(result.sad());
    }

    public static Serder incept(InceptArgs args) throws DigestException {
        if (args.getIntive() == null) {
            args.setIntive(false);
        }
        String vs = versify(
            Ident.KERI,
            args.getVersion() != null ? args.getVersion() : new CoreUtil.Version(),
            args.getKind() != null ? args.getKind() : Serials.JSON,
            0);
        String ilk = args.getDelpre() == null ? Ilks.ICP.getValue() : Ilks.DIP.getValue();
        CesrNumber sner = new CesrNumber(BigInteger.ZERO);

        if (args.getIsith() == null) {
            args.setIsith(Math.max(1, (int) Math.ceil(args.getKeys().size() / 2.0)));
        }

        Tholder tholder = new Tholder(null, null, args.getIsith());
        if (tholder.getNum() != null && tholder.getNum() < 1) {
            throw new InvalidValueException("Invalid sith = " + tholder.getNum() + " less than 1.");
        }
        if (tholder.getSize() > args.getKeys().size()) {
            throw new InvalidValueException("Invalid sith = " + tholder.getNum() + " for keys " + args.getKeys());
        }

        if (args.getNdigs() == null) {
            args.setNdigs(new ArrayList<>());
        }

        if (args.getNsith() == null) {
            args.setNsith(Math.max(0, (int) Math.ceil(args.getNdigs().size() / 2.0)));
        }

        Tholder ntholder = new Tholder(null, null, args.getNsith());
        if (ntholder.getNum() != null && ntholder.getNum() < 0) {
            throw new InvalidValueException("Invalid nsith = " + ntholder.getNum() + " less than 0.");
        }
        if (ntholder.getSize() > args.getKeys().size()) {
            throw new InvalidValueException("Invalid nsith = " + ntholder.getNum() + " for keys " + args.getNdigs());
        }

        List<String> wits = args.getWits() != null ? args.getWits() : new ArrayList<>();
        if (new HashSet<>(wits).size() != wits.size()) {
            throw new InvalidValueException("Invalid wits = " + wits + ", has duplicates.");
        }

        if (args.getToad() == null) {
            args.setToad(wits.isEmpty() ? 0 : ample(wits.size()));
        }

        CesrNumber toader = new CesrNumber(RawArgs.builder().build(), BigInteger.valueOf(args.getToad()), null);
        if (!wits.isEmpty()) {
            if (toader.getNum().intValue() < 1 || toader.getNum().intValue() > wits.size()) {
                throw new InvalidValueException("Invalid toad = " + toader.getNum() + " for wits = " + wits);
            }
        } else if (toader.getNum().intValue() != 0) {
            throw new InvalidValueException("Invalid toad = " + toader.getNum() + " for wits = " + wits);
        }

        List<String> cnfg = args.getCnfg() != null ? args.getCnfg() : new ArrayList<>();
        List<Object> data = args.getData() != null ? args.getData() : new ArrayList<>();

        // Build ked
        Map<String, Object> ked = new LinkedHashMap<>();
        ked.put("v", vs);
        ked.put("t", ilk);
        ked.put("d", "");
        ked.put("i", "");
        ked.put("s", sner.getNumh());
        ked.put("kt", args.getIntive() && tholder.getNum() != null ? tholder.getNum() : tholder.getSith());
        ked.put("k", args.getKeys());
        ked.put("nt", args.getIntive() && ntholder.getNum() != null ? ntholder.getNum() : ntholder.getSith());
        ked.put("n", args.getNdigs());
        ked.put("bt", args.getIntive() ? toader.getNum() : toader.getNumh());
        ked.put("b", wits);
        ked.put("c", cnfg);
        ked.put("a", data);

        if (args.getDelpre() != null) {
            ked.put("di", args.getDelpre());
            if (args.getCode() == null) {
                args.setCode(MatterCodex.Blake3_256.getValue());
            }
        }

        Prefixer prefixer;
        if (args.getDelpre() == null && args.getCode() == null && args.getKeys().size() == 1) {
            prefixer = new Prefixer(args.getKeys().getFirst());
            if (prefixer.isDigestible()) {
                throw new InvalidCodeException(
                    "Invalid code, digestive=" + prefixer.getCode() + ", must be derived from ked."
                );
            }
        } else {
            prefixer = new Prefixer(args.getCode(), ked);
            if (args.getDelpre() != null && !prefixer.isDigestible()) {
                throw new InvalidCodeException(
                    "Invalid derivation code = " + prefixer.getCode() + " for delegation. Must be digestive"
                );
            }
        }

        ked.put("i", prefixer.getQb64());
        if (prefixer.isDigestible()) {
            ked.put("d", prefixer.getQb64());
        } else {
            ked = Saider.saidify(ked).sad();
        }

        return new Serder(ked);
    }

    // TODO implement function
    public static byte[] messagize(
        Serder serder,
        List<Siger> sigers,
        Object seal,
        List<Cigar> wigers,
        List<Cigar> cigars,
        boolean pipelined
    ) {
        byte[] msg = serder.getRaw().getBytes(StandardCharsets.UTF_8);
        return msg;
    }

    public static int ample(int n) {
        return ample(n, null, true);
    }

    public static int ample(int n, Integer f, boolean weak) {
        n = Math.max(0, n); // no negatives

        if (f == null) {
            // least floor f subject to n >= 3*f+1
            int f1 = Math.max(1, (int) Math.floor(Math.max(0, n - 1) / 3.0));
            // most ceil f subject to n >= 3*f+1
            int f2 = Math.max(1, (int) Math.ceil(Math.max(0, n - 1) / 3.0));

            if (weak) {
                // try both fs to see which one has lowest m
                return Math.min(
                    n,
                    Math.min(
                        (int) Math.ceil((n + f1 + 1) / 2.0),
                        (int) Math.ceil((n + f2 + 1) / 2.0)
                    )
                );
            } else {
                return Math.min(
                    n,
                    Math.max(
                        0,
                        Math.max(
                            n - f1,
                            (int) Math.ceil((n + f1 + 1) / 2.0)
                        )
                    )
                );
            }
        } else {
            f = Math.max(0, f);
            int m1 = (int) Math.ceil((n + f + 1) / 2.0);
            int m2 = Math.max(0, n - f);

            if (m2 < m1 && n > 0) {
                throw new InvalidValueException(
                    String.format("Invalid f=%d is too big for n=%d.", f, n)
                );
            }

            if (weak) {
                return Math.min(n, Math.min(m1, m2));
            } else {
                return Math.min(n, Math.max(m1, m2));
            }
        }
    }

} 