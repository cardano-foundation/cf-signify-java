package org.cardanofoundation.signify.cesr;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import org.apache.commons.math3.fraction.Fraction;

/**
 * Interface representing a signing threshold (Sith).
 * Can be implemented as a numeric threshold, string threshold, or weighted
 * threshold.
 */
public interface Sith {

    /**
     * Get the value of this Sith
     * 
     * @return The value of this Sith
     */
    Object getValue();


    /**
     * Implementation of Sith for numeric thresholds
     */
    class IntegerSith implements Sith {
        private final Integer value;

        public IntegerSith(Integer value) {
            this.value = value;
        }

        @Override
        public Integer getValue() {
            return value;
        }


        @Override
        public String toString() {
            return value.toString();
        }
    }

    /**
     * Implementation of Sith for string thresholds
     */
    class StringSith implements Sith {
        private final String value;
        @Getter
        boolean weighted;

        public StringSith(String value) {
            this.value = value;
            this.weighted = value.contains("[");
        }

        @Override
        public String getValue() {
            return value;
        }


        @Override
        public String toString() {
            return value;
        }

    }

    /**
     * Implementation of Sith for weighted thresholds
     */
    class WeightedSith implements Sith {
        private final List<List<String>> weightedStringValue;
        @Getter
        private final List<List<Fraction>> weightedFractionValue;

        public WeightedSith(List<List<Fraction>> weightedStringValue) {
            this.weightedFractionValue = weightedStringValue;
            this.weightedStringValue = fromFractionWeighted(weightedStringValue);
        }

        @Override
        public List<List<String>> getValue() {
            return weightedStringValue;
        }


        @Override
        public String toString() {
            return "WeightedSith(" + weightedStringValue.size() + " clauses)";
        }

        public static List<List<Fraction>> fromStringWeightedLst(List<List<String>> sith) {
            return sith.stream()
                    .map(clause -> ((List<?>) clause).stream()
                            .map(w -> weight(w.toString()))
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }

        public static List<List<Fraction>> fromStringWeighted(List<String> sith) {
            List<List<Fraction>> result = new ArrayList<>();
            List<Fraction> clause = sith.stream()
                    .map(WeightedSith::weight)
                    .collect(Collectors.toList());
            result.add(clause);
            return result;
        }

        public static List<List<String>> fromFractionWeighted(List<List<Fraction>> weightedFractionValue) {
            List<List<String>> sith = new ArrayList<>();
            for (List<Fraction> clause : weightedFractionValue) {
                List<String> clauseStr = new ArrayList<>();
                for (Fraction f : clause) {
                    if (0 < f.doubleValue() && f.doubleValue() < 1) { // fraction ratio
                        clauseStr.add(fractionToString(f));
                    } else { // fraction decimal
                        if (f.getDenominator() == 1) {
                            clauseStr.add("" + f.intValue());
                        } else {
                            clauseStr.add("" + f.doubleValue());
                        }
                    }
                }
                sith.add(clauseStr);
            }
            return sith;
        }

        private static String fractionToString(Fraction f) {
            String str;
            if (f.getDenominator() == 1) {
                str = Integer.toString(f.getNumerator());
            } else {
                str = f.getNumerator() + "/" + f.getDenominator();
            }
            return str;
        }

        private static Fraction weight(String w) {
            String[] parts = w.split("/");
            if (parts.length == 2) {
                return new Fraction(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]));
            } else {
                return new Fraction(Integer.parseInt(w));
            }
        }
    }

    /**
     * Create a Sith from a numeric value
     * 
     * @param value The numeric value
     * @return A new NumericSith instance
     */
    static Sith fromInteger(Integer value) {
        return new IntegerSith(value);
    }

    /**
     * Create a Sith from a string value
     * 
     * @param value The string value of json or hex string
     * @return A new StringSith instance
     */
    static Sith fromString(String value) {
        return new StringSith(value);
    }

    /**
     * Create a Sith from a weighted threshold list
     * 
     * @param value The weighted threshold list
     * @return A new WeightedSith instance
     */
    static Sith fromStringWeightedLst(List<List<String>> value) {
        return new WeightedSith(WeightedSith.fromStringWeightedLst(value));
    }

    static Sith fromStringWeighted(List<String> value) {
        return new WeightedSith(WeightedSith.fromStringWeighted(value));
    }

    static Sith fromFractionWeighted(List<List<Fraction>> value) {
        return new WeightedSith(value);
    }

    /**
     * Create a Sith from an object
     * 
     * @param value The object value
     * @return A new Sith instance
     * @throws IllegalArgumentException if the value is not a supported type
     */
    static Sith fromObject(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Sith value cannot be null");
        } else if (value instanceof Integer intValue) {
            return fromInteger(intValue);
        } else if (value instanceof Number numValue) {
            return fromInteger(numValue.intValue());
        } else if (value instanceof String strValue) {
            return fromString(strValue);
        } else if (value instanceof List<?> listValue) {
            // Check if this is a list of lists structure
            if (!listValue.isEmpty() && listValue.get(0) instanceof List<?>) {
                try {
                    List<List<String>> weightedValue = new ArrayList<>();
                    for (Object outerItem : listValue) {
                        if (!(outerItem instanceof List<?>)) {
                            throw new IllegalArgumentException("Expected list of lists, but found mixed structure");
                        }
                        
                        List<?> innerList = (List<?>) outerItem;
                        List<String> stringInnerList = new ArrayList<>();
                        
                        for (Object innerItem : innerList) {
                            stringInnerList.add(innerItem.toString());
                        }
                        
                        weightedValue.add(stringInnerList);
                    }
                    return fromStringWeightedLst(weightedValue);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to convert list structure to Sith: " + e.getMessage(), e);
                }
            } else {
                // Try to handle a flat list as a single clause
                try {
                    List<String> stringList = new ArrayList<>();
                    
                    for (Object item : listValue) {
                        stringList.add(item.toString());
                    }
                    
                    return fromStringWeighted(stringList);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to convert list to Sith: " + e.getMessage(), e);
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported Sith value type: " + value.getClass().getName());
        }
    }
}