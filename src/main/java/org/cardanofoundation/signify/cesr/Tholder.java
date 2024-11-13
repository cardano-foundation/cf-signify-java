package org.cardanofoundation.signify.cesr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.math3.fraction.Fraction;
import org.cardanofoundation.signify.cesr.Codex.BexCodex;
import org.cardanofoundation.signify.cesr.Codex.NumCodex;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class Tholder {
    private boolean weighted = false;
    private Object thold;
    private int size = 0;
    private CesrNumber number;
    private Function<List<Integer>, Boolean> _satisfy;

    public Tholder(Object thold, Object limen, Object sith) {
        if (thold != null) {
            _processThold(thold);
        } else if (limen != null) {
            _processLimen((String) limen);
        } else if (sith != null) {
            _processSith(sith);
        } else {
            throw new RuntimeException("Missing threshold expression");
        }
    }

    public byte[] getLimen() {
        return number != null ? number.getQb64b() : null;
    }

    public String getSith() {
        if (weighted) {
            //TODO find mathjs replacement
            return "";
        } else {
            return Integer.toHexString((Integer) thold);
        }
    }

    public String getJson() {
        return Utils.jsonStringify(this.getSith());
    }

    public Integer getNum() {
        return weighted ? null : (Integer) thold;
    }

    private void _processThold(Object thold) {
        if (thold instanceof Integer) {
            _processUnweighted((Integer) thold);
        } else {
            @SuppressWarnings("unchecked")
            List<List<Fraction>> weightedThold = (List<List<Fraction>>) thold;
            _processWeighted(weightedThold);
        }
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
            throw new IllegalArgumentException("Invalid code for limen=" + matter.getCode());
        }
    }

    @SuppressWarnings("unchecked")
    private void _processSith(Object sith) {
        if (sith instanceof Number) {
            this._processUnweighted(((Number) sith).intValue());
        } else if (sith instanceof String sithStr && !sithStr.contains("[")) {
            this._processUnweighted(Integer.parseInt(sithStr, 16));
        } else {
            List<Object> _sith;
            if (sith instanceof String sithStr) {
                try {
                    _sith = new ObjectMapper().readValue(sithStr, List.class);
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing sith string", e);
                }
            } else {
                _sith = Collections.singletonList(Utils.toList(sith));
            }

            if (_sith.isEmpty()) {
                throw new IllegalArgumentException("Empty weight list");
            }

            List<Boolean> mask = _sith.stream()
                .map(x -> !(x instanceof String))
                .toList();

            if (!mask.isEmpty() && mask.stream().anyMatch(x -> x)) {
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
                    throw new IllegalArgumentException(
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
            throw new IllegalArgumentException("Non-positive int threshold = " + thold);
        }
        this.thold = thold;
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
                throw new IllegalArgumentException(
                    "Invalid sith clause: " + thold + " all clause weight sums must be >= 1"
                );
            }
        }

        this.thold = thold;
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
        return (Integer) thold > 0 && indices.size() >= (Integer) thold; // at least one
    }

    private boolean _satisfyWeighted(List<Integer> indices) {
        if (indices.isEmpty()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        List<List<Fraction>> weightedThold = (List<List<Fraction>>) thold;
        
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

    //TODO find mathjs replacement
}
