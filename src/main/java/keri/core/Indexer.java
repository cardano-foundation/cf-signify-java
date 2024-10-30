package keri.core;

import keri.core.args.IndexerArgs;
import keri.core.exceptions.EmptyMaterialError;
import keri.core.Codex.IndexedBothSigCodex;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Indexer {

    static Map<String, Xizage> sizes = new HashMap<>();
    static {
        sizes.put("A", new Xizage(1, 1, 0, 88, 0));
        sizes.put("B", new Xizage(1, 1, 0, 88, 0));
        sizes.put("C", new Xizage(1, 1, 0, 88, 0));
        sizes.put("D", new Xizage(1, 1, 0, 88, 0));
        sizes.put("E", new Xizage(1, 1, 0, 88, 0));
        sizes.put("F", new Xizage(1, 1, 0, 88, 0));
        sizes.put("0A", new Xizage(2, 2, 1, 156, 0));
        sizes.put("0B", new Xizage(2, 2, 1, 156, 0));

        sizes.put("2A", new Xizage(2, 4, 2, 92, 0));
        sizes.put("2B", new Xizage(2, 4, 2, 92, 0));
        sizes.put("2C", new Xizage(2, 4, 2, 92, 0));
        sizes.put("2D", new Xizage(2, 4, 2, 92, 0));
        sizes.put("2E", new Xizage(2, 4, 2, 92, 0));
        sizes.put("2F", new Xizage(2, 4, 2, 92, 0));

        sizes.put("3A", new Xizage(2, 6, 3, 160, 0));
        sizes.put("3B", new Xizage(2, 6, 3, 160, 0));

        sizes.put("0z", new Xizage(2, 2, 0, null, 0));
        sizes.put("1z", new Xizage(2, 2, 1, 76, 1));
        sizes.put("4z", new Xizage(2, 6, 3, 80, 1));
    }

    static Map<String, Integer> hards = new HashMap<>();
    static {
        hards.put("A", 1);
        hards.put("B", 1);
        hards.put("C", 1);
        hards.put("D", 1);
        hards.put("E", 1);
        hards.put("F", 1);
        hards.put("G", 1);
        hards.put("H", 1);
        hards.put("I", 1);
        hards.put("J", 1);
        hards.put("K", 1);
        hards.put("L", 1);
        hards.put("M", 1);
        hards.put("N", 1);
        hards.put("O", 1);
        hards.put("P", 1);
        hards.put("Q", 1);
        hards.put("R", 1);
        hards.put("S", 1);
        hards.put("T", 1);
        hards.put("U", 1);
        hards.put("V", 1);
        hards.put("W", 1);
        hards.put("X", 1);
        hards.put("Y", 1);
        hards.put("Z", 1);
        hards.put("a", 1);
        hards.put("b", 1);
        hards.put("c", 1);
        hards.put("d", 1);
        hards.put("e", 1);
        hards.put("f", 1);
        hards.put("g", 1);
        hards.put("h", 1);
        hards.put("i", 1);
        hards.put("j", 1);
        hards.put("k", 1);
        hards.put("l", 1);
        hards.put("m", 1);
        hards.put("n", 1);
        hards.put("o", 1);
        hards.put("p", 1);
        hards.put("q", 1);
        hards.put("r", 1);
        hards.put("s", 1);
        hards.put("t", 1);
        hards.put("u", 1);
        hards.put("v", 1);
        hards.put("w", 1);
        hards.put("x", 1);
        hards.put("y", 1);
        hards.put("z", 1);
        hards.put("0", 2);
        hards.put("1", 4);
        hards.put("2", 4);
        hards.put("3", 4);
        hards.put("4", 2);
        hards.put("5", 2);
        hards.put("6", 2);
        hards.put("7", 4);
        hards.put("8", 4);
        hards.put("9", 4);
    }

    private String _code = "";
    private Integer _index = -1;
    private Integer _ondex;
    private byte[] _raw = new byte[0];

    public Indexer(IndexerArgs args) {
        byte[] raw = args.getRaw();
        String code = args.getCode();
        Integer index = args.getIndex();
        Integer ondex = args.getOndex();
        String qb64 = args.getQb64();
        byte[] qb64b = args.getQb64b();
        byte[] qb2 = args.getQb2();

        if (raw != null) {
            if (code == null) {
                throw new
                    EmptyMaterialError("Improper initialization need either (raw and code) or qb64b or qb64 or qb2.");
            }

            if (!sizes.containsKey(code)) {
                throw new RuntimeException("Unsupported code=" + code + ".");
            }

            Xizage xizage = sizes.get(code);
            int os = xizage.os;
            Integer fs = xizage.fs;
            int cs = xizage.hs + xizage.ss;
            int ms = xizage.ss - xizage.os;

            if (index == null || index < 0 || index > Math.pow(64, ms) - 1) {
                throw new RuntimeException("Invalid index=" + index + " for code=" + code + ".");
            }

            if (ondex != null && xizage.os != 0 && !(ondex >= 0 && ondex <= Math.pow(64, os) - 1)) {
                throw new RuntimeException("Invalid ondex=" + ondex + " for code=" + code + ".");
            }

            if (IndexedBothSigCodex.has(code) && ondex != null) {
                throw new RuntimeException("Non None ondex=" + ondex + " for code=" + code + ".");
            }

            if (IndexedBothSigCodex.has(code)) {
                if (ondex == null) {
                    ondex = index;
                } else {
                    if (!ondex.equals(index) && os == 0) {
                        throw new RuntimeException("Non matching ondex=" + ondex + " and index=" + index + " for code=" + code + ".");
                    }
                }
            }

            if (fs == null) {
                throw new RuntimeException("Variable length unsupported");
            }

            int rawsize = (int) Math.floor(((fs - cs) * 3.0) / 4.0);
            raw = Arrays.copyOf(raw, rawsize);

            if (raw.length != rawsize) {
                throw new RuntimeException("Not enough raw bytes for code=" + code + " and index=" + index + ", expected " + rawsize + " got " + raw.length + ".");
            }

            this._code = code;
            this._index = index;
            this._ondex = ondex;
            this._raw = raw;
        } else if (qb64b != null) {
            String qb64Str = Base64.getUrlEncoder().withoutPadding().encodeToString(qb64b);
            this._exfil(qb64Str);
        } else if (qb64 != null) {
            this._exfil(qb64);
        } else if (qb2 != null) {
            this._bexfil(qb2);
        } else {
            throw new EmptyMaterialError("Improper initialization need either (raw and code and index) or qb64b or qb64 or qb2.");
        }
    }

    private void _bexfil(byte[] qb2) {
        throw new RuntimeException("qb2 not yet supported: "  + Arrays.toString(qb2));
    }

    public static int getRawSize(String code) {
        final Xizage xizage = sizes.get(code);
        if (xizage.fs == null) {
            throw new RuntimeException();
        }
        return (int) Math.floor(((xizage.fs - (xizage.hs + xizage.ss)) * 3.0) / 4.0) - xizage.ls;
    }

    public String getCode() {
        return this._code;
    }

    public byte[] getRaw() {
        return this._raw;
    }
    
    public Integer getIndex() {
        return this._index;
    }

    public Integer getOndex() {
        return this._ondex;
    }

    public String getQb64() {
        return this._infil();
    }

    public byte[] getQb64b() {
        // TODO: Implement getQb64b
        return new byte[0];
    }

    private String _infil() {
        // TODO: Implement infil logic
        return "";
    }

    private void _exfil(String qb64) {
        // TODO: Implement exfil logic
        if (qb64.isEmpty()) {
            throw new RuntimeException("Empty Material");
        }
    }

    static class Xizage {
        public Integer hs;
        public Integer ss;
        public Integer os;
        public Integer fs;
        public Integer ls;

        public Xizage(Integer hs, Integer ss, Integer os, Integer ls, Integer fs) {
            this.hs = hs;
            this.ss = ss;
            this.os = os;
            this.ls = ls;
            this.fs = fs;
        }
    }
}
