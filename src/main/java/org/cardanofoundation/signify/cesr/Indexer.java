package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.IndexerArgs;
import org.cardanofoundation.signify.cesr.exceptions.EmptyMaterialError;
import org.cardanofoundation.signify.cesr.Codex.IndexedBothSigCodex;
import org.cardanofoundation.signify.cesr.Codex.IndexedCurrentSigCodex;
import org.cardanofoundation.signify.cesr.util.CoreUtil;

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
        throw new RuntimeException("qb2 not yet supported: " + Arrays.toString(qb2));
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
        return this.getQb64().getBytes();
    }

    private String _infil() {
        String code = this.getCode();
        Integer index = this.getIndex();
        Integer ondex = this.getOndex();
        byte[] raw = this.getRaw();

        int ps = (3 - (raw.length % 3)) % 3;
        Xizage xizage = sizes.get(code);
        int cs = xizage.hs + xizage.ss;
        int ms = xizage.ss - xizage.os;

        if (index < 0 || index > Math.pow(64, ms) - 1) {
            throw new RuntimeException("Invalid index=" + index + " for code=" + code + ".");
        }

        if (ondex != null && xizage.os != 0 && !(ondex >= 0 && ondex <= Math.pow(64, xizage.os) - 1)) {
            throw new RuntimeException("Invalid ondex=" + ondex + " for os=" + xizage.os + " and code=" + code + ".");
        }

        String both = code + CoreUtil.intToB64(index, ms) + CoreUtil.intToB64(ondex == null ? 0 : ondex, xizage.os);

        if (both.length() != cs) {
            throw new RuntimeException("Mismatch code size = " + cs + " with table = " + both.length() + ".");
        }

        if (cs % 4 != ps - xizage.ls) {
            throw new RuntimeException("Invalid code=" + both + " for converted raw pad size=" + ps + ".");
        }

        byte[] bytes = new byte[ps + raw.length];
        for (int i = 0; i < ps; i++) {
            bytes[i] = 0;
        }
        System.arraycopy(raw, 0, bytes, ps, raw.length);

        String full = both + CoreUtil.encodeBase64Url(bytes).substring(ps - xizage.ls);
        if (full.length() != xizage.fs) {
            throw new RuntimeException("Invalid code=" + both + " for raw size=" + raw.length + ".");
        }

        return full;
    }

    private void _exfil(String qb64) {
        if (qb64.isEmpty()) {
            throw new RuntimeException("Empty Material");
        }

        char first = qb64.charAt(0);
        if (!hards.containsKey(String.valueOf(first))) {
            throw new RuntimeException("Unexpected code " + first);
        }

        int hs = hards.get(String.valueOf(first));
        if (qb64.length() < hs) {
            throw new RuntimeException("Need " + (hs - qb64.length()) + " more characters.");
        }

        String hard = qb64.substring(0, hs);
        if (!sizes.containsKey(hard)) {
            throw new RuntimeException("Unsupported code " + hard);
        }

        Xizage xizage = sizes.get(hard);
        int cs = xizage.hs + xizage.ss; // both hard + soft code size
        int ms = xizage.ss - xizage.os;

        if (qb64.length() < cs) {
            throw new RuntimeException("Need " + (cs - qb64.length()) + " more characters.");
        }

        String sindex = qb64.substring(hs, hs + ms);
        int index = CoreUtil.b64ToInt(sindex);

        String sondex = qb64.substring(hs + ms, hs + ms + xizage.os);
        Integer ondex;
        if (IndexedCurrentSigCodex.has(hard)) {
            ondex = xizage.os != 0 ? CoreUtil.b64ToInt(sondex) : null;
            if (ondex != null && ondex != 0) {
                throw new RuntimeException("Invalid ondex=" + ondex + " for code=" + hard + ".");
            } else {
                ondex = null;
            }
        } else {
            ondex = xizage.os != 0 ? CoreUtil.b64ToInt(sondex) : index;
        }

        if (xizage.fs == null) {
            throw new RuntimeException("variable length not supported");
        }

        if (qb64.length() < xizage.fs) {
            throw new RuntimeException("Need " + (xizage.fs - qb64.length()) + " more chars.");
        }

        qb64 = qb64.substring(0, xizage.fs);
        int ps = cs % 4;
        int pbs = 2 * ps != 0 ? ps : xizage.ls;
        byte[] raw;
        if (ps != 0) {
            String base = "A".repeat(ps) + qb64.substring(cs);
            byte[] paw = CoreUtil.decodeBase64Url(base); // decode base to leave prepadded raw
            int pi = CoreUtil.readInt(Arrays.copyOfRange(paw, 0, ps)); // prepad as int
            if ((pi & (1 << pbs) - 1) != 0) {
                // masked pad bits non-zero
                throw new RuntimeException("Non zeroed prepad bits = " + Integer.toBinaryString(pi & (1 << pbs) - 1) + " in " + qb64.charAt(cs) + ".");
            }
            raw = Arrays.copyOfRange(paw, ps, paw.length); // strip off ps prepad paw bytes
        } else {
            String base = qb64.substring(cs);
            byte[] paw = CoreUtil.decodeBase64Url(base);
            int li = CoreUtil.readInt(Arrays.copyOfRange(paw, 0, xizage.ls));
            if (li != 0) {
                if (li == 1) {
                    throw new RuntimeException("Non zeroed lead byte = 0x" + String.format("%02x", li) + ".");
                } else {
                    throw new RuntimeException("Non zeroed lead bytes = 0x" + String.format("%04x", li));
                }
            }
            raw = Arrays.copyOfRange(paw, xizage.ls, paw.length);
        }

        if (raw.length != Math.floor(((qb64.length() - cs) * 3.0) / 4.0)) {
            throw new RuntimeException("Improperly qualified material = " + qb64);
        }

        this._code = hard;
        this._index = index;
        this._ondex = ondex;
        this._raw = raw; // must be bytes for crypto opts and immutable not bytearray
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
