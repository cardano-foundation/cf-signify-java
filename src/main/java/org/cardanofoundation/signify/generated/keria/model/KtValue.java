package org.cardanofoundation.signify.generated.keria.model;

import java.util.List;

/**
 * Concrete representation of a KERI signing threshold ({@code kt}/{@code nt} fields).
 * Exactly one of {@link #getUnweightedThreshold()} or {@link #getWeightedThreshold()} is non-null.
 * <ul>
 *   <li>Unweighted: a single string such as {@code "2"}</li>
 *   <li>Weighted: a list of fractions such as {@code ["1/2", "1/2"]}</li>
 * </ul>
 * Extends {@link KeyStateRecordKt} so it is assignment-compatible with all generated model
 * fields typed as {@code KeyStateRecordKt} or {@code ICPV1Kt}.
 */
public final class KtValue extends KeyStateRecordKt {

    private final String unweightedThreshold;
    private final List<String> weightedThreshold;

    private KtValue(String unweightedThreshold, List<String> weightedThreshold) {
        this.unweightedThreshold = unweightedThreshold;
        this.weightedThreshold = weightedThreshold;
    }

    public static KtValue unweighted(String value) {
        return new KtValue(value, null);
    }

    public static KtValue weighted(List<String> weights) {
        return new KtValue(null, weights);
    }

    public boolean isWeighted() {
        return weightedThreshold != null;
    }

    public String getUnweightedThreshold() {
        return unweightedThreshold;
    }

    public List<String> getWeightedThreshold() {
        return weightedThreshold;
    }

    /**
     * Returns the raw value expected by CESR utilities such as {@code Tholder}:
     * a {@code String} for unweighted, or a {@code List<String>} for weighted.
     */
    public Object raw() {
        return isWeighted() ? weightedThreshold : unweightedThreshold;
    }
}
