package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Objects;

/**
 * Concrete representation of a KERI signing threshold ({@code kt}/{@code nt} fields).
 * KERIA returns these as either a plain string (unweighted, e.g. {@code "2"}) or a list
 * of fraction weights (weighted, e.g. {@code ["1/2", "1/2"]}, possibly nested for
 * multi-clause thresholds). Generated models ({@code KeyStateRecord}, {@code Icp},
 * {@code Rot}, ...) use this type directly via the {@code schemaMappings} entries in
 * {@code build.gradle}. Discriminate with an exhaustive switch:
 *
 * <pre>{@code
 * switch (state.getKt()) {
 *     case Threshold.Unweighted u -> u.threshold();
 *     case Threshold.Weighted w -> w.weights();
 * }
 * }</pre>
 */
public abstract sealed class Threshold {

    private Threshold() {
    }

    public static Unweighted unweighted(String threshold) {
        return new Unweighted(threshold);
    }

    public static Unweighted unweighted(int threshold) {
        return new Unweighted(threshold);
    }

    public static Weighted weighted(List<?> weights) {
        return new Weighted(weights);
    }

    /**
     * Null-safe extraction of the raw threshold from a generated model field.
     * Returns {@code null} when the field is absent.
     */
    public static Object rawOf(Threshold value) {
        return value == null ? null : value.raw();
    }

    /**
     * Returns the raw value as KERIA sent it and as CESR utilities such as
     * {@code Tholder} expect it: a {@code String} for unweighted, or a {@code List}
     * for weighted. Also used by Jackson so serialization round-trips the
     * original JSON.
     */
    @JsonValue
    public abstract Object raw();

    /**
     * Unweighted thresholds are usually a hex string (keripy: {@code int(sith, 16)});
     * KERI v1 events created with keripy's {@code intive=True} instead carry a JSON
     * integer, whose decimal value must not be conflated with the hex string form
     * ({@code 10} is ten, {@code "10"} is sixteen). {@link #raw()} keeps whichever
     * form arrived so serialization stays byte-faithful.
     */
    public static final class Unweighted extends Threshold {
        private final Object raw;

        private Unweighted(String threshold) {
            this.raw = Objects.requireNonNull(threshold, "threshold");
        }

        private Unweighted(int threshold) {
            this.raw = threshold;
        }

        /** The threshold as a canonical hex sith string, regardless of wire form. */
        public String threshold() {
            return raw instanceof Integer i ? Integer.toHexString(i) : (String) raw;
        }

        @Override
        public Object raw() {
            return raw;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Unweighted other && raw.equals(other.raw);
        }

        @Override
        public int hashCode() {
            return raw.hashCode();
        }

        @Override
        public String toString() {
            return "Threshold.Unweighted(" + raw + ")";
        }
    }

    public static final class Weighted extends Threshold {
        private final List<?> weights;

        private Weighted(List<?> weights) {
            this.weights = List.copyOf(Objects.requireNonNull(weights, "weights"));
        }

        public List<?> weights() {
            return weights;
        }

        @Override
        public Object raw() {
            return weights;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Weighted other && weights.equals(other.weights);
        }

        @Override
        public int hashCode() {
            return weights.hashCode();
        }

        @Override
        public String toString() {
            return "Threshold.Weighted(" + weights + ")";
        }
    }
}
