package org.cardanofoundation.signify.core;

import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.args.RotateArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ident;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.signify.cesr.util.CoreUtil.versify;

public class Eventing {
    private static final int MaxIntThold = (int) (Math.pow(2, 32) - 1);

    public static Serder interact(InteractArgs args) {
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

    public static Serder incept(InceptArgs args) {
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

    public static Serder reply(
            String route,
            Object data,
            String stamp,
            CoreUtil.Version version,
            CoreUtil.Serials kind
    ) {
        if (route == null) {
            route = "";
        }
        if (kind == null) {
            kind = CoreUtil.Serials.JSON;
        }
        String vs = CoreUtil.versify(Ident.KERI, version, kind, 0);
        if (data == null) {
            data = new HashMap<>();
        }
        Map<String, Object> _sad = new LinkedHashMap<>();
        _sad.put("v", vs);
        _sad.put("t", Ilks.RPY.getValue());
        _sad.put("d", "");
        _sad.put("dt", stamp != null ? stamp : new Date().toInstant().toString().replace("Z", "000+00:00"));
        _sad.put("r", route);
        _sad.put("a", data);

        Saider.SaidifyResult result = Saider.saidify(_sad);
        Map<String, Object> sad = result.sad();
        Saider saider = new Saider(sad.get("d").toString());

        if (!saider.verify(sad, true, true, kind, "d")) {
            throw new InvalidValueException("Invalid said = " + saider.getQb64() + " for reply msg=" + sad);
        }
        return new Serder(sad);
    }

    public static Serder rotate(RotateArgs args) {
        if (args.getSn() == null) {
            args.setSn(1);
        }
        if (args.getIntive() == null) {
            args.setIntive(true);
        }

        String vs = versify(Ident.KERI, args.getVersion(), args.getKind(), 0);
        String _ilk = args.getIlk() != null ? args.getIlk() : Ilks.ROT.getValue();
        if (!_ilk.equals(Ilks.ROT.getValue()) && !_ilk.equals(Ilks.DRT.getValue())) {
            throw new InvalidValueException("Invalid ilk = " + args.getIlk() + "for rot or drt.");
        }

        CesrNumber sner = new CesrNumber(BigInteger.valueOf(args.getSn()));
        if (sner.getNum().compareTo(BigInteger.ONE) <= 0) {
            throw new InvalidValueException("Invalid sn = " + sner.getNumh() + "for rot or drt.");
        }
        int _isith;
        if (args.getIsith() == null) {
            _isith = Math.max(1, (int) Math.ceil(args.getKeys().size() / 2.0));
        } else {
            _isith = Integer.parseInt((String) args.getIsith());
        }

        Tholder tholder = new Tholder(null, null, _isith);
        if (tholder.getNum() != null && tholder.getNum() < 1) {
            throw new InvalidValueException("Invalid sith = " + tholder.getNum() + "less than 1.");
        }
        if (tholder.getSize() > args.getKeys().size()) {
            throw new InvalidValueException("Invalid sith = " + tholder.getNum() + "for keys = " + args.getKeys());
        }

        List<String> _ndigs;
        if (args.getNdigs() == null) {
            _ndigs = new ArrayList<>();
        } else {
            _ndigs = args.getNdigs();
        }

        int _nsith;
        if (args.getNsith() == null) {
            _nsith = Math.max(1, (int) Math.ceil(_ndigs.size() / 2.0));
        } else {
            _nsith = Integer.parseInt((String) args.getNsith());
        }

        Tholder ntholder = new Tholder(null, null, _nsith);
        if (ntholder.getNum() != null && ntholder.getNum() < 1) {
            throw new InvalidValueException("Invalid sith = " + ntholder.getNum() + "less than 1.");
        }
        if (ntholder.getSize() > _ndigs.size()) {
            throw new InvalidValueException("Invalid sith = " + ntholder.getNum() + "for keys = " + _ndigs);
        }

        List<String> _wits;
        if (args.getWits() == null) {
            _wits = new ArrayList<>();
        } else {
            _wits = args.getWits();
        }
        Set<String> witset = new HashSet<>(_wits);
        if (witset.size() != _wits.size()) {
            throw new IllegalArgumentException("Invalid wits = " + args.getWits() + ", has duplicates.");
        }

        List<String> _cuts;
        if (args.getCuts() == null) {
            _cuts = new ArrayList<>();
        } else {
            _cuts = args.getCuts();
        }
        Set<String> cutset = new HashSet<>(_cuts);
        if (cutset.size() != _cuts.size()) {
            throw new IllegalArgumentException("Invalid cuts = " + args.getCuts() + ", has duplicates.");
        }

        List<String> _adds;
        if (args.getAdds() == null) {
            _adds = new ArrayList<>();
        } else {
            _adds = args.getAdds();
        }
        Set<String> addset = new HashSet<>(_adds);

        // Non-empty intersection of witset and addset
        Set<String> witaddset = witset.stream()
            .filter(addset::contains)
            .collect(Collectors.toSet());
        if (!witaddset.isEmpty()) {
            throw new IllegalArgumentException("Invalid member combination among wits = " + _wits + ", and adds = " + addset + ".");
        }

        // Non-empty intersection of cutset and addset
        Set<String> cutaddset = cutset.stream()
            .filter(addset::contains)
            .collect(Collectors.toSet());
        if (!cutaddset.isEmpty()) {
            throw new IllegalArgumentException("Invalid member combination among cuts = " + cutset + ", and adds = " + addset + ".");
        }

        Set<String> newitsetdiff = new HashSet<>(_wits);
        for (String v : _cuts) {
            newitsetdiff.remove(v);
        }

        Set<String> newitset = new HashSet<>(newitsetdiff);
        newitset.addAll(addset);

        if (newitset.size() != witset.size() - cutset.size() + addset.size()) {
            throw new IllegalArgumentException(
                "Invalid member combination among wits = " + _wits +
                    ", cuts = " + cutset + ", and adds = " + addset + "."
            );
        }

        Integer _toad;
        if (args.getToad() == null) {
            if (newitset.isEmpty()) {
                _toad = 0;
            } else {
                _toad = ample(newitset.size());
            }
        } else {
            _toad = args.getToad();
        }

        if (!newitset.isEmpty()) {
            if (_toad < 1 || _toad > newitset.size()) {
                throw new IllegalArgumentException("Invalid toad = " + _toad + " for wit = " + _wits);
            }
        } else {
            if (_toad != 0) {
                throw new IllegalArgumentException("Invalid toad = " + _toad + " for wit = " + _wits);
            }
        }

        Map<String, Object> _ked = new LinkedHashMap<>();
        _ked.put("v", vs);
        _ked.put("t", _ilk);
        _ked.put("d", "");
        _ked.put("i", args.getPre());
        _ked.put("s", sner.getNumh());
        _ked.put("p", args.getDig());
        _ked.put("kt",
            tholder.getNum() != null &&
            args.getIntive() &&
            tholder.getNum() <= MaxIntThold
                ? Integer.toHexString(tholder.getNum())
                : tholder.getSith());
        _ked.put("k", args.getKeys());
        _ked.put("nt",
            ntholder.getNum() != null &&
            args.getIntive() &&
            ntholder.getNum() <= MaxIntThold
                ? Integer.toHexString(ntholder.getNum())
                : ntholder.getSith());
        _ked.put("n", _ndigs);
        _ked.put("bt",
            _toad != null && args.getIntive() && _toad <= MaxIntThold
                ? _toad
                : Integer.toHexString(_toad));
        _ked.put("br", args.getCuts());
        _ked.put("ba", args.getAdds());
        _ked.put("a", args.getData() != null ? args.getData() : new ArrayList<>());

        Saider.SaidifyResult result = Saider.saidify(_ked);
        return new Serder(result.sad());
    }
}