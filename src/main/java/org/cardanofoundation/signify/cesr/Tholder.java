package org.cardanofoundation.signify.cesr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.math3.fraction.Fraction;
import org.cardanofoundation.signify.cesr.Codex.BexCodex;
import org.cardanofoundation.signify.cesr.Codex.NumCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.exceptions.serialize.SerializeException;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class Tholder {
    private boolean weighted = false;
    private Integer unweightedThold;
    private List<List<Fraction>> weightedThold;
    private int size = 0;
    private CesrNumber number;
    private Function<List<Integer>, Boolean> _satisfy;

    public Tholder(Integer unweightedThold) {
        this._processUnweighted(unweightedThold);
    }

    public Tholder(List<List<Fraction>> weightedThold) {
        this._processWeighted(weightedThold);
    }

    public Tholder(String limen) {
        this._processLimen(limen);
    }

    public Tholder(Sith sith) {
        this._processSith(sith);
    }

    public byte[] getLimen() {
        return number != null ? number.getQb64b() : null;
    }

    public Sith getSith() {
        if (this.weighted) {
            return new Sith.WeightedSith(this.weightedThold);
        } else {
            return new Sith.StringSith(Integer.toHexString(this.unweightedThold));
        }
    }

    public String getJson() {
        return Utils.jsonStringify(this.getSith().getValue());
    }

    public Integer getNum() {
        return weighted ? null : unweightedThold;
    }

    private void _processLimen(String limen) {
        Matter matter = new Matter(limen);
        if (NumCodex.has(matter.getCode())) {
            RawArgs args = RawArgs.builder()
                    .raw(matter.getRaw())
                    .code(matter.getCode())
                    .build();
            CesrNumber number = new CesrNumber(args, null, null);
            _processUnweighted(number.getNum().intValue());
        } else if (BexCodex.has(matter.getCode())) {
            // TODO: Implement Bexter
        } else {
            throw new InvalidCodeException("Invalid code for limen=" + matter.getCode());
        }
    }

    private void _processSith(Sith sith) {
        if (sith instanceof Sith.IntegerSith) {
            this._processUnweighted(((Sith.IntegerSith) sith).getValue());
        }  else if (sith instanceof Sith.WeightedSith) {
            this._processWeighted(((Sith.WeightedSith) sith).getWeightedFractionValue());
        } else if (sith instanceof Sith.StringSith && !((Sith.StringSith) sith).isWeighted()) {
            this._processUnweighted(Integer.parseInt(((Sith.StringSith) sith).getValue(), 16));
        } else {
            List<Object> _sith;
            if (sith instanceof Sith.StringSith) { // json of weighted sith from cli
                try {
                    _sith = new ObjectMapper().readValue(((Sith.StringSith) sith).getValue(), List.class); // deserialize
                } catch (Exception e) {
                    throw new SerializeException("Error parsing sith string");
                }
            } else {
                _sith = (List<Object>) sith;
            }

            if (sith == null || _sith.isEmpty()) {
                throw new InvalidValueException("Empty weight list");
            }

            List<Boolean> mask = _sith.stream()
                    .map(x -> !(x instanceof String))
                    .toList();

            if (!mask.isEmpty() && mask.stream().noneMatch(x -> x)) {
                _sith = List.of(_sith);
            }

            for (Object c : _sith) {
                if (!(c instanceof List<?> clause)) {
                    continue;
                }
                List<Boolean> clauseMask = clause.stream()
                        .map(x -> x instanceof String)
                        .toList();

                if (!clauseMask.isEmpty() && !clauseMask.stream().allMatch(x -> x)) {
                    throw new InvalidValueException(
                            "Invalid sith, some weights in clause " + clauseMask + " are non string"
                    );
                }
            }

            List<List<Fraction>> thold = this._processClauses(_sith);
            this._processWeighted(thold);
        }
    }

    private List<List<Fraction>> _processClauses(List<Object> sith) {
        return sith.stream()
                .map(clause -> ((List<?>) clause).stream()
                        .map(w -> weight(w.toString()))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private void _processUnweighted(int thold) {
        if (thold < 0) {
            throw new InvalidValueException("Non-positive int threshold = " + thold);
        }
        this.unweightedThold = thold;
        this.weighted = false;
        this.size = thold; // used to verify that keys list size is at least size
        this._satisfy = this::_satisfyNumeric;
        this.number = new CesrNumber(BigInteger.valueOf(thold));
    }

    private void _processWeighted(List<List<Fraction>> thold) {
        for (List<Fraction> clause : thold) {
            double sum = clause.stream()
                    .mapToDouble(Fraction::doubleValue)
                    .sum();
            if (sum < 1) {
                throw new InvalidValueException(
                        "Invalid sith clause: " + thold + " all clause weight sums must be >= 1"
                );
            }
        }

        this.weightedThold = thold;
        this.weighted = true;
        this.size = thold.stream()
                .mapToInt(List::size)
                .sum();
        this._satisfy = this::_satisfyWeighted;
    }

    private Fraction weight(String w) {
        String[] parts = w.split("/");
        if (parts.length == 2) {
            return new Fraction(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1])
            );
        } else {
            return new Fraction(Integer.parseInt(w));
        }
    }

    private boolean _satisfyNumeric(List<Integer> indices) {
        return this.unweightedThold > 0 && indices.size() >= this.unweightedThold; // at least one
    }

    private boolean _satisfyWeighted(List<Integer> indices) {
        if (indices.isEmpty()) {
            return false;
        }

        Set<Integer> indexes = new TreeSet<>(indices);
        boolean[] sats = new boolean[size];
        indexes.forEach(idx -> sats[idx] = true);

        int wio = 0;
        for (List<Fraction> clause : weightedThold) {
            double cw = 0;
            for (Fraction w : clause) {
                if (sats[wio]) {
                    cw += w.doubleValue();
                }
                wio++;
            }
            if (cw < 1) {
                return false;
            }
        }

        return true;
    }

    public boolean satisfy(List<Integer> indices) {
        return _satisfy.apply(indices);
    }
}
