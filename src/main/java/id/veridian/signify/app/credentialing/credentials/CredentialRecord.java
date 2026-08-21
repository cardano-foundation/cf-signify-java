package id.veridian.signify.app.credentialing.credentials;

import id.veridian.signify.app.WireSad;
import id.veridian.signify.cesr.Serder;
import id.veridian.signify.exception.SignifySerializationException;
import id.veridian.signify.generated.keria.model.Credential;

import java.util.List;
import java.util.Map;

/**
 * A credential as KERIA returned it: a typed read-only view paired with the exact
 * response body as deserialized off the wire.
 *
 * <p>Same contract as {@link WireSad}: {@link #acdc()}, {@link #iss()} and {@link #anc()}
 * build the byte-frozen blocks from {@link #body()}, never from the typed getters. Pass
 * each one's CESR attachment alongside it when re-granting a credential you did not issue
 * — leaving one unset makes {@code Ipex.grant} re-sign with the sender's own keys.</p>
 *
 * <p>{@link #body()} also carries what the spec leaves out: a revoked credential's
 * {@code rev} and {@code revatc} are on the wire but absent from {@link Credential}.</p>
 *
 * @param value the typed read-only projection
 * @param body  the response envelope exactly as deserialized off the wire
 */
public record CredentialRecord(Credential value, Map<String, Object> body) {

    /** The ACDC itself, from the wire {@code sad} block. */
    public Serder acdc() {
        return new Serder(nested("sad"));
    }

    /** The issuance TEL event, from the wire {@code iss} block. */
    public Serder iss() {
        return new Serder(nested("iss"));
    }

    /** The anchoring KEL event, from the wire {@code anc} block. */
    public Serder anc() {
        return new Serder(nested("anc"));
    }

    /** The ACDC's CESR attachment ({@code atc}), or {@code null} when KERIA sent none. */
    public String acdcAttachment() {
        return value == null ? null : value.getAtc();
    }

    /** The issuance event's CESR attachment ({@code issatc}), or {@code null} when KERIA sent none. */
    public String issAttachment() {
        return value == null ? null : value.getIssatc();
    }

    /**
     * The anchoring event's CESR attachment, or {@code null} when KERIA sent none.
     * {@code ancatc} is a list on the wire; grant takes one value, so this is the first.
     */
    public String ancAttachment() {
        List<String> ancatc = value == null ? null : value.getAncatc();
        return ancatc == null || ancatc.isEmpty() ? null : ancatc.getFirst();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nested(String key) {
        Object nested = body.get(key);
        if (!(nested instanceof Map<?, ?>)) {
            throw new SignifySerializationException(
                    "Credential is missing the '" + key + "' block: " + body, null);
        }
        return (Map<String, Object>) nested;
    }
}
