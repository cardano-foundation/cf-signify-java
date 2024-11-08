package org.cardanofoundation.signify.cesr;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ident;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Version;
import org.cardanofoundation.signify.cesr.util.CoreUtil.DeversifyResult;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class Serder {
    private Serials kind;
    private String raw = "";
    private Map<String, Object> ked;
    private Ident ident = Ident.KERI;
    private int size = 0;
    private Version version = new Version();
    private final String code;

    public Serder(Map<String, Object> ked, Serials kind, String code) {
        this.code = code != null ? code : MatterCodex.Blake3_256.getValue();
        this.kind = kind != null ? kind : Serials.JSON;
        setKed(ked);
    }

    public void setKed(Map<String, Object> ked) {
        ExhaleResult result = _exhale(ked, this.kind);
        int size = result.raw.length();
        this.raw = result.raw;
        this.ident = result.ident;
        this.ked = result.kd;
        this.kind = result.kind;
        this.size = size;
        this.version = result.version;
    }

    public String getPre() {
        return (String) this.ked.get("i");
    }

    public CesrNumber getSner() {
        return new CesrNumber(RawArgs.builder().build(), null, (String) this.ked.get("s"));
    }

    public int getSn() {
        return getSner().getNum().intValue();
    }

    private ExhaleResult _exhale(Map<String, Object> ked, Serials kind) {
        return sizeify(ked, kind);
    }

    @SuppressWarnings("unchecked")
    public List<Verfer> getVerfers() {
        List<String> keys;
        if (this.ked.containsKey("k")) {
            // establishment event
            keys = (List<String>) this.ked.get("k");
        } else {
            // non-establishment event
            keys = new ArrayList<>();
        }
        // create a new Verfer for each key
        List<Verfer> verfers = new ArrayList<>();
        for (String key : keys) {
            verfers.add(new Verfer(key));
        }
        return verfers;
    }

    @SuppressWarnings("unchecked")
    public List<Diger> getDigers() {
        List<String> keys;
        if (this.ked.containsKey("n")) {
            // establishment event
            keys = (List<String>) this.ked.get("n");
        } else {
            // non-establishment event
            keys = new ArrayList<>();
        }
        // create a new Diger for each key
        List<Diger> digers = new ArrayList<>();
        for (String key : keys) {
            digers.add(new Diger(key));
        }
        return digers;
    }

    public static String dumps(Map<String, Object> ked, Serials kind) {
        if (kind == Serials.JSON) {
            return Utils.jsonStringify(ked);
        } else {
            throw new RuntimeException("unsupported event encoding");
        }
    }

    public static ExhaleResult sizeify(Map<String, Object> ked, Serials kind) {
        if (!ked.containsKey("v")) {
            throw new RuntimeException("Missing or empty version string");
        }

        DeversifyResult deversifyResult = CoreUtil.deversify((String) ked.get("v"));
        Ident ident = deversifyResult.ident();
        Serials knd = deversifyResult.kind();
        Version version = deversifyResult.version();

        if (!version.equals(new Version())) {
            throw new RuntimeException("unsupported version " + version);
        }

        if (kind == null) {
            kind = knd;
        }

        String raw = dumps(ked, kind);
        int size = raw.getBytes(StandardCharsets.UTF_8).length;

        ked.put("v", CoreUtil.versify(ident, version, kind, size));

        raw = dumps(ked, kind);

        return new ExhaleResult(raw, ident, kind, ked, version);
    }


    public record ExhaleResult (
        String raw,
        Ident ident,
        Serials kind,
        Map<String, Object> kd,
        Version version
    ) {}
}
