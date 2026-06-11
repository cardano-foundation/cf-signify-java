package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonValue;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;

import java.util.List;
import java.util.Objects;

/**
 * Concrete representation of a KERI signing threshold ({@code kt}/{@code nt} fields).
 * KERIA returns these as either a plain string (unweighted, e.g. {@code "2"}) or a list
 * of fraction weights (weighted, e.g. {@code ["1/2", "1/2"]}, possibly nested for
 * multi-clause thresholds). Discriminate with an exhaustive switch:
 *
 * <pre>{@code
 * switch (KtValue.of(state.getKt())) {
 *     case KtValue.Unweighted u -> u.threshold();
 *     case KtValue.Weighted w -> w.weights();
 * }
 * }</pre>
 */
public abstract sealed class KtValue extends KeyStateRecordKt {

    private KtValue() {
    }

    public static Unweighted unweighted(String threshold) {
        return new Unweighted(threshold);
    }

    public static Weighted weighted(List<?> weights) {
        return new Weighted(weights);
    }

    /**
     * Narrows a generated {@code kt}/{@code nt} field to a {@link KtValue}. Every value
     * deserialized off the wire is one; throws on programmatically constructed
     * {@link KeyStateRecordKt} instances that carry no threshold.
     */
    public static KtValue of(KeyStateRecordKt value) {
        if (value instanceof KtValue ktValue) {
            return ktValue;
        }
        throw new IllegalArgumentException("kt/nt value carries no threshold: " + value);
    }

    /**
     * Null-safe extraction of the raw threshold from a generated model field.
     * Returns {@code null} when the field is absent or carries no threshold.
     */
    public static Object rawOf(KeyStateRecordKt value) {
        return value instanceof KtValue ktValue ? ktValue.raw() : null;
    }

    /**
     * Returns the raw value as KERIA sent it and as CESR utilities such as
     * {@code Tholder} expect it: a {@code String} for unweighted, or a {@code List}
     * for weighted. Also used by Jackson so serialization round-trips the
     * original JSON instead of emitting an empty object.
     */
    @JsonValue
    public abstract Object raw();

    public static final class Unweighted extends KtValue {
        private final String threshold;

        private Unweighted(String threshold) {
            this.threshold = Objects.requireNonNull(threshold, "threshold");
        }

        public String threshold() {
            return threshold;
        }

        @Override
        public Object raw() {
            return threshold;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Unweighted other && threshold.equals(other.threshold);
        }

        @Override
        public int hashCode() {
            return threshold.hashCode();
        }

        @Override
        public String toString() {
            return "KtValue.Unweighted(" + threshold + ")";
        }
    }

    public static final class Weighted extends KtValue {
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
            return "KtValue.Weighted(" + weights + ")";
        }
    }
}
