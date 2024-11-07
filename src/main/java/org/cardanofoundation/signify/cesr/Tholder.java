package org.cardanofoundation.signify.cesr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.math3.fraction.Fraction;
import org.cardanofoundation.signify.cesr.args.MatterArgs;
import org.cardanofoundation.signify.cesr.Codex.BexCodex;
import org.cardanofoundation.signify.cesr.Codex.NumCodex;

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

    public String getJson() throws Exception{
        return new ObjectMapper().writeValueAsString(this.getSith());
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
        Matter matter = new Matter(MatterArgs.builder().qb64(limen).build());
        if (NumCodex.has(matter.getCode())) {
            CesrNumber number = new CesrNumber(
                MatterArgs.builder()
                    .raw(matter.getRaw())
                    .code(matter.getCode())
                    .build(),
                null,
                null
            );
            _processUnweighted(number.getNum().intValue());
        } else if (BexCodex.has(matter.getCode())) {
            // TODO: Implement Bexter
        } else {
            throw new IllegalArgumentException("Invalid code for limen=" + matter.getCode());
        }
    }

    @SuppressWarnings("unchecked")
    private void _processSith(Object sith) {
        if (sith instanceof Integer) {
            this._processUnweighted((Integer) sith);
        } else if (sith instanceof String sithStr) {
            if (!sithStr.contains("[")) {
                this._processUnweighted(Integer.parseInt(sithStr, 16));
            } else {
                try {
                    List<Object> _sith = new ObjectMapper().readValue(sithStr, List.class);
                    processSithList(_sith);
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing sith string", e);
                }
            }
        } else if (sith instanceof List) {
            List<Object> _sith = (List<Object>) sith;
            processSithList(_sith);
        }
    }

    private void processSithList(List<Object> _sith) {
        if (_sith.isEmpty()) {
            throw new RuntimeException("Empty weight list");
        }

        boolean hasNonStrings = _sith.stream()
            .anyMatch(x -> !(x instanceof String));

        if (hasNonStrings) {
            _sith = Collections.singletonList(_sith);
        }

        for (Object c : _sith) {
            if (!(c instanceof List<?> clause)) {
                continue;
            }

            boolean hasNonStringWeights = clause.stream()
                .anyMatch(x -> !(x instanceof String));

            if (hasNonStringWeights) {
                throw new RuntimeException(
                    "Invalid sith, some weights in clause are non string"
                );
            }
        }

        List<List<Fraction>> thold = this._processClauses(_sith);
        this._processWeighted(thold);
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
        this.number = new CesrNumber(MatterArgs.builder().build(), BigInteger.valueOf(thold), null);
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
