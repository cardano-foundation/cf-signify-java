package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;

import java.util.Map;

public class Saider extends Matter {
    private final String Dummy = "#";

    public enum Ids {
        d("d");

        private final String value;
        Ids(String value) {
            this.value = value;
        }
    }

    private static class Digestage {
        public Object klas;
        public Integer size;
        public Integer length;

        public Digestage(byte[] klas, Integer size, Integer length) {
            this.klas = klas;
            this.size = size == null ? 0 : size;
            this.length = length == null ? 0 : length;
        }
    }

    //TODO implement saider constructor
    public Saider(RawArgs args, Map<String, Object> sad, CoreUtil.Serials kind, String label) {
        super(args);
    }

    public Saider(String qb64) {
        super(qb64);
    }

    // TODO implement saidify logic
    public static SaidifyResult saidify(Map<String, Object> sad, String code, CoreUtil.Serials kind, String label) {
        return new SaidifyResult(null, null);
    }

    public static SaidifyResult saidify(Map<String, Object> sad) {
        return saidify(sad, Codex.MatterCodex.Blake3_256.getValue(), CoreUtil.Serials.JSON, Ids.d.value);
    }

    public record SaidifyResult(Saider saider, Map<String, Object> sad) {}
}
