package org.cardanofoundation.signify.cesr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ident;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Version;
import org.cardanofoundation.signify.cesr.util.CoreUtil.DeversifyResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class Serder {
    private Serials _kind;
    private String _raw = "";
    private Map<String, Object> _ked;
    private Ident _ident = Ident.KERI;
    private int _size = 0;
    private Version _version = new Version();
    private final String _code;

    public Serder(Map<String, Object> ked, Serials kind, String code) {
        this._code = code != null ? code : MatterCodex.Blake3_256.getValue();
        this._kind = kind != null ? kind : Serials.JSON;
        setKed(ked);
    }

    public void setKed(Map<String, Object> ked) {
        ExhaleResult result = _exhale(ked, this._kind);
        int size = result.raw.length();
        this._raw = result.raw;
        this._ident = result.ident;
        this._ked = result.kd;
        this._kind = result.kind;
        this._size = size;
        this._version = result.version;
    }

    public String getPre() {
        return (String) this._ked.get("i");
    }

    public Map<String, Object> getKed() {
        return this._ked;
    }

    public String getCode() {
        return this._code;
    }

    public String getRaw() {
        return this._raw;
    }

    public CesrNumber getSner() {
        return new CesrNumber(null, null, (String) this._ked.get("s"));
    }

    public Serials getKind() {
        return this._kind;
    }

    public int getSn() {
        return getSner().getNum().intValue();
    }

    private ExhaleResult _exhale(Map<String, Object> ked, Serials kind) {
        return sizeify(ked, kind);
    }

    public Ident getIdent() {
        return this._ident;
    }

    public int getSize() {
        return this._size;
    }

    public Version getVersion() {
        return this._version;
    }

    @SuppressWarnings("unchecked")
    public List<Verfer> getVerfers() {
        List<String> keys;
        if (this._ked.containsKey("k")) {
            // establishment event
            keys = (List<String>) this._ked.get("k");
        } else {
            // non-establishment event
            keys = new ArrayList<>();
        }
        // create a new Verfer for each key
        List<Verfer> verfers = new ArrayList<>();
        for (String key : keys) {
            verfers.add(new Verfer(MatterArgs.builder().qb64(key).build()));
        }
        return verfers;
    }

    @SuppressWarnings("unchecked")
    public List<Diger> getDigers() {
        List<String> keys;
        if (this._ked.containsKey("n")) {
            // establishment event
            keys = (List<String>) this._ked.get("n");
        } else {
            // non-establishment event
            keys = new ArrayList<>();
        }
        // create a new Diger for each key
        List<Diger> digers = new ArrayList<>();
        for (String key : keys) {
            digers.add(new Diger(MatterArgs.builder().qb64(key).build(), null));
        }
        return digers;
    }

    public static String dumps(Map<String, Object> ked, Serials kind) {
        if (kind == Serials.JSON) {
            try {
                return new ObjectMapper().writeValueAsString(ked);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing ked to JSON", e);
            }
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
        int size = raw.getBytes().length;

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
