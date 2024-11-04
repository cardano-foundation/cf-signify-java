package org.cardanofoundation.signify.cesr;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import org.cardanofoundation.signify.cesr.Codex.NonTransCodex;
import org.cardanofoundation.signify.cesr.Codex.DigiCodex;
import org.cardanofoundation.signify.cesr.Codex.SmallVarRawSizeCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.Codex.LargeVarRawSizeCodex;

public class Matter {

    static Map<String, Sizage> sizes = new HashMap<>();

    static {
        sizes.put("A", new Sizage(1, 0, 44, 0));
        sizes.put("B", new Sizage(1, 0, 44, 0));
        sizes.put("C", new Sizage(1, 0, 44, 0));
        sizes.put("D", new Sizage(1, 0, 44, 0));
        sizes.put("E", new Sizage(1, 0, 44, 0));
        sizes.put("F", new Sizage(1, 0, 44, 0));
        sizes.put("G", new Sizage(1, 0, 44, 0));
        sizes.put("H", new Sizage(1, 0, 44, 0));
        sizes.put("I", new Sizage(1, 0, 44, 0));
        sizes.put("J", new Sizage(1, 0, 44, 0));
        sizes.put("K", new Sizage(1, 0, 76, 0));
        sizes.put("L", new Sizage(1, 0, 76, 0));
        sizes.put("M", new Sizage(1, 0, 4, 0));
        sizes.put("N", new Sizage(1, 0, 12, 0));
        sizes.put("O", new Sizage(1, 0, 44, 0));
        sizes.put("P", new Sizage(1, 0, 124, 0));
        sizes.put("Q", new Sizage(1, 0, 44, 0));
        sizes.put("0A", new Sizage(2, 0, 24, 0));
        sizes.put("0B", new Sizage(2, 0, 88, 0));
        sizes.put("0C", new Sizage(2, 0, 88, 0));
        sizes.put("0D", new Sizage(2, 0, 88, 0));
        sizes.put("0E", new Sizage(2, 0, 88, 0));
        sizes.put("0F", new Sizage(2, 0, 88, 0));
        sizes.put("0G", new Sizage(2, 0, 8, 0));
        sizes.put("0H", new Sizage(2, 0, 8, 0));
        sizes.put("0I", new Sizage(2, 0, 88, 0));
        sizes.put("1AAA", new Sizage(4, 0, 48, 0));
        sizes.put("1AAB", new Sizage(4, 0, 48, 0));
        sizes.put("1AAC", new Sizage(4, 0, 80, 0));
        sizes.put("1AAD", new Sizage(4, 0, 80, 0));
        sizes.put("1AAE", new Sizage(4, 0, 56, 0));
        sizes.put("1AAF", new Sizage(4, 0, 8, 0));
        sizes.put("1AAG", new Sizage(4, 0, 36, 0));
        sizes.put("1AAH", new Sizage(4, 0, 100, 0));
        sizes.put("1AAI", new Sizage(4, 0, 48, 0));
        sizes.put("1AAJ", new Sizage(4, 0, 48, 0));
        sizes.put("2AAA", new Sizage(4, 0, 8, 1));
        sizes.put("3AAA", new Sizage(4, 0, 8, 2));
        sizes.put("4A", new Sizage(2, 2, null, 0));
        sizes.put("5A", new Sizage(2, 2, null, 1));
        sizes.put("6A", new Sizage(2, 2, null, 2));
        sizes.put("7AAA", new Sizage(4, 4, null, 0));
        sizes.put("8AAA", new Sizage(4, 4, null, 1));
        sizes.put("9AAA", new Sizage(4, 4, null, 2));
        sizes.put("4B", new Sizage(2, 2, null, 0));
        sizes.put("5B", new Sizage(2, 2, null, 1));
        sizes.put("6B", new Sizage(2, 2, null, 2));
        sizes.put("7AAB", new Sizage(4, 4, null, 0));
        sizes.put("8AAB", new Sizage(4, 4, null, 1));
        sizes.put("9AAB", new Sizage(4, 4, null, 2));
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
    private Integer _size = -1;
    private byte[] _raw = new byte[0];

    public Matter(RawArgs args, Integer rize) {
        int size = -1;
        if (args.getCode() == null || args.getCode().isEmpty()) {
            throw new RuntimeException("Improper initialization need either (raw and code) or qb64b or qb64 or qb2.");
        }

        String firstCodeChar = args.getCode().substring(0, 1);

        if (SmallVarRawSizeCodex.has(firstCodeChar) || LargeVarRawSizeCodex.has(firstCodeChar)) {
            if (rize != null) {
                if (rize < 0) {
                    throw new RuntimeException("missing var raw size for code=" + args.getCode());
                }
            } else {
                size = args.getRaw().length;
            }

            final int ls = (3 - (size % 3)) % 3;
            size = (int) Math.floor((double) (size + ls) / 3);

            if (SmallVarRawSizeCodex.has(firstCodeChar)) {
                if (size <= Math.pow(64, 2) - 1) {
                    final int hs = 2;
                    final String s = SmallVarRawSizeCodex.fromLsIndex(ls).getValue();
                    this._code = s + args.getCode().charAt(1);
                } else if (size <= Math.pow(64, 4) - 1) {
                    final int hs = 4;
                    final String s = LargeVarRawSizeCodex.fromLsIndex(ls).getValue();
                    this._code = s + "AAAA".substring(0, hs - 2) + args.getCode().charAt(1);
                } else {
                    throw new RuntimeException("Unsupported raw size for code=" + args.getCode());
                }
            } else {
                if (size <= Math.pow(64, 4) - 1) {
                    final int hs = 4;
                    final String s = LargeVarRawSizeCodex.fromLsIndex(ls).getValue();
                    this._code = s + args.getCode().substring(1, hs);
                } else {
                    throw new RuntimeException("Unsupported raw size for code=" + args.getCode());
                }
            }
        } else {
            final Sizage sizage = sizes.get(args.getCode());
            if (sizage == null || sizage.fs == null || sizage.fs == -1) {
                // invalid
                throw new RuntimeException("Unsupported variable size code=" + args.getCode());
            }

            rize = Matter.getRawSize(args.getCode());
        }

        args.setRaw(Arrays.copyOfRange(args.getRaw(), 0, rize)); // copy only exact size from raw stream

        if (args.getRaw().length != rize) {
            // forbids shorter
            throw new RuntimeException("Not enougth raw bytes for code=" + args.getCode() + " expected " + rize + " got " + args.getRaw().length + ".");
        }

        this._code = args.getCode();
        this._size = size;
        this._raw = args.getRaw();
    }

    public Matter(RawArgs args) {
        this(args, null);
    }

    public Matter(String qb64) {
        this._exfil(qb64);
    }

    public Matter(byte[] qb2) {
        this._bexfil(qb2);
    }

    public String getCode() {
        return this._code;
    }

    public Integer getSize() {
        return this._size;
    }

    public byte[] getRaw() {
        return this._raw;
    }

    public String getQb64() {
        return this._infil();
    }

    public byte[] getQb64b() {
        return this.getQb64().getBytes();
    }

    public boolean isTransferable() {
        return !NonTransCodex.has(this._code);
    }

    public boolean isDigestible() {
        return DigiCodex.has(this._code);
    }

    static class Sizage {
        public Integer hs;
        public Integer ss;
        public Integer ls;
        public Integer fs;

        public Sizage(Integer hs, Integer ss, Integer fs, Integer ls) {
            this.hs = hs;
            this.ss = ss;
            this.fs = fs;
            this.ls = ls;
        }
    }

    public static int getRawSize(String code) {
        final Sizage sizage = sizes.get(code);
        final Integer cs = sizage.hs + sizage.ss;
        if (sizage.fs == null || sizage.fs == -1) {
            throw new RuntimeException("Non-fixed raw size code " + code + ".");
        }
        return (int) Math.floor(((sizage.fs - cs) * 3.0) / 4.0) - sizage.ls;
    }

    private void _exfil(String qb64) {
        if (qb64.isEmpty()) {
            throw new RuntimeException("Empty Material");
        }

        final String first = qb64.substring(0, 1);
        if (!Matter.hards.containsKey(first)) {
            throw new RuntimeException("Unexpected code " + first);
        }

        final Integer hs = Matter.hards.get(first);
        if (qb64.length() < hs) {
            throw new RuntimeException("Shortage Error");
        }

        final String hard = qb64.substring(0, hs);
        if (!Matter.sizes.containsKey(hard)) {
            throw new RuntimeException("Unsupported code " + hard);
        }

        final Sizage sizage = Matter.sizes.get(hard);
        final int cs = sizage.hs + sizage.ss;
        int size = -1;
        if (sizage.fs == -1) {
            // variable size code, NOT SUPPORTED
            throw new RuntimeException("Variable size codes not supported yet");
        } else {
            size = sizage.fs;
        }

        if (qb64.length() < sizage.fs) {
            throw new RuntimeException("Need " + (size - qb64.length()) + " more chars.");
        }

        qb64 = qb64.substring(0, sizage.fs);
        final int ps = cs % 4;
        final int pbs = 2 * (ps == 0 ? sizage.ls : ps);
        byte[] raw;

        if (ps != 0) {
            final String base = "A".repeat(ps) +
                    qb64.substring(cs);

            final byte[] paw = CoreUtil.decodeBase64Url(base);
            // new byte array with the two first elements of paw
            final int pi = CoreUtil.readInt(Arrays.copyOfRange(paw, 0, ps));
            final int mask = pi & (1 << pbs - 1);
            if (mask != 0) {
                // masked pad bits non-zero
                throw new RuntimeException(String.format("Non zeroed prepad bits = %06d in %s",
                        (pi & ((1 << pbs) - 1)),
                        qb64.charAt(cs)));
            }
            raw = Arrays.copyOfRange(paw, ps, paw.length);
        } else {
            final String base = qb64.substring(cs);
            final byte[] paw = CoreUtil.decodeBase64Url(base);
            final int li = CoreUtil.readInt(Arrays.copyOfRange(paw, 0, sizage.ls));
            if (li != 0) {
                if (li == 1) {
                    throw new IllegalStateException(String.format("Non zeroed lead byte = 0x%02x", li));
                } else {
                    throw new IllegalStateException(String.format("Non zeroed lead bytes = 0x%04x", li));
                }
            }
            raw = Arrays.copyOfRange(paw, sizage.ls, paw.length);
        }

        this._code = hard; // hard only
        this._size = size;
        this._raw = raw; // ensure bytes so immutable and for crypto ops
    }

    private void _bexfil(byte[] qb2) {
        throw new RuntimeException("qb2 not yet supported");
    }

    private String _infil() {
        final String code = this.getCode();
        final Integer size = this.getSize();
        final byte[] raw = this.getRaw();

        final int ps = (3 - (raw.length % 3)) % 3;
        final Sizage sizage = sizes.get(code);

        if (sizage != null && sizage.fs == null) {
            // Variable size code, NOT SUPPORTED
            final int cs = sizage.hs + sizage.ss;
            if (cs % 4 == 1) {
                throw new RuntimeException("Whole code size not multiple of 4 for variable length material. cs=" + cs);
            }

            if (size < 0 || size > Math.pow(64, sizage.ss) - 1) {
                throw new RuntimeException("Invalid size=" + size + " for code=" + code + ".");
            }

            final String both = code + CoreUtil.intToB64(size, sizage.ss);
            if (both.length() % 4 != ps - sizage.ls) {
                throw new RuntimeException("Invalid code=" + both + " for converted raw pad size=" + ps + ", " + raw.length + ".");
            }

            final byte[] bytes = new byte[sizage.ls + raw.length];
            for (int i = 0; i < sizage.ls; i++) {
                bytes[i] = 0;
            }
            for (int i = 0; i < raw.length; i++) {
                int odx = i + ps;
                bytes[odx] = raw[i];
            }

            return both + CoreUtil.encodeBase64Url(bytes);
        } else {
            final String both = code;
            final int cs = both.length();
            if (cs % 4 != ps - sizage.ls) {
                // adjusted pad given lead bytes
                throw new RuntimeException("Invalid code=" + both + " for converted raw pad size=" + ps + ", " + raw.length + ".");
            }
            // prepad, convert, and replace upfront
            // when fixed and ls != 0 then cs % 4 is zero and ps==ls
            // otherwise  fixed and ls == 0 then cs % 4 == ps
            final byte[] bytes = new byte[ps + raw.length];
            for (int i = 0; i < ps; i++) {
                bytes[i] = 0;
            }
            for (int i = 0; i < raw.length; i++) {
                int odx = i + ps;
                bytes[odx] = raw[i];
            }
            return both + CoreUtil.encodeBase64Url(bytes).substring(cs % 4);
        }
    }
}
