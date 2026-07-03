package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.cesr.Serder;

import java.util.Map;

/**
 * An event embedded in an exchange message: a typed read-only view paired with the
 * exact sad (self-addressed data) as deserialized off the wire.
 *
 * <p>KERI events are byte-frozen — SAIDs and signatures are computed over the exact
 * serialized bytes. The {@link #sad()} map preserves wire field order and scalar types,
 * so anything cryptographic (re-signing, SAID verification, {@link Serder} construction)
 * must go through {@link #sad()} or {@link #toSerder()}. The {@link #value()} view is a
 * projection for reading only and is never re-serialized: a typed round-trip could
 * reorder fields, drop unknown ones, or re-type scalars and change the computed SAID.</p>
 */
public record Embed<T>(T value, Map<String, Object> sad) {

    public Serder toSerder() {
        return new Serder(sad);
    }
}
