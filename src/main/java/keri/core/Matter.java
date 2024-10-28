package keri.core;

import java.util.HashMap;
import java.util.Map;

import keri.core.Codex.MatterCodex;
import keri.core.Codex.NonTransCodex;
import keri.core.Codex.DigiCodex;
import keri.core.Codex.NumCodex;
import keri.core.Codex.BexCodex;
import keri.core.Codex.SmallVarRawSizeCodex;
import keri.core.args.MatterArgs;
import keri.core.Codex.LargeVarRawSizeCodex;

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


    public MatterCodex mtrDex = new MatterCodex();
    public NonTransCodex nonTransCodex = new NonTransCodex();
    public DigiCodex digiDex = new DigiCodex();
    public NumCodex numDex = new NumCodex();
    public BexCodex bexDex = new BexCodex();
    public SmallVarRawSizeCodex smallVrzDex = new SmallVarRawSizeCodex();
    public LargeVarRawSizeCodex largeVrzDex = new LargeVarRawSizeCodex();
    

    public Matter(MatterArgs args) {
        // TODO: Implement Matter constructor
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
        // TODO: Implement getQb64
        return "";
    }

    public byte[] getQb64b() {
        // TODO: Implement getQb64b
        return new byte[0];
    }

    public boolean isTransferable() {
        return !nonTransCodex.has(this._code);
    }

    public boolean isDigestible() {
        return digiDex.has(this._code);
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
        if (sizage == null || sizage.fs == null) {
            throw new RuntimeException("Non-fixed raw size code " + code + ".");
        }
        return (int) Math.floor(((sizage.fs - cs) * 3) / 4) - sizage.ls;
    }
}
