package org.cardanofoundation.signify.core;

import lombok.*;
import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;

import java.util.List;

public class States {
    @Data
    @Builder
    public static class State {
        private final int[] vn;
        private final String i;
        private final String s;
        private final String p;
        private final String d;
        private final String f;
        private final String dt;
        private final String et;
        private final Object kt;
        private final List<String> k;
        private final Object nt;
        private final List<String> n;
        private final String bt;
        private final List<String> b;
        private final List<String> c;
        private final EstablishmentState ee;
        private final String di;
    }

    @Data
    @Builder
    public static class EstablishmentState {
        private final String d;
        private final String s;
    }

    @Data
    @Builder
    public static class SaltyState {
        private final String sxlt;
        private final int pidx;
        private final int kidx;
        private final String stem;
        private final Tier tier;
        private final String dcode;
        private final List<String> icodes;
        private final List<String> ncodes;
        private final boolean transferable;
    }

    @Data
    @Builder
    public static class RandyState {
        private final List<String> prxs;
        private final List<String> nxts;
    }

    @Data
    @Builder
    public static class GroupState {
        private final HabState mhab;
        private final List<String> keys;
        private final List<String> ndigs;
    }

    @Getter
    @Builder
    public static class HabState {
        private final String name;
        private final String prefix;
        private final boolean transferable;
        private final State state;
        private final List<Object> windexes;
        private final SaltyState saltyState;
        private final RandyState randyState;
        private final GroupState groupState;

        public boolean containsKey(String algo) {
            return switch (algo) {
                case "salty" -> saltyState != null;
                case "randy" -> randyState != null;
                case "group" -> groupState != null;
                default -> throw new InvalidValueException("Unexpected value: " + algo);
            };
        }

        public Object get(String algo) {
            return switch (algo) {
                case "salty" -> saltyState;
                case "randy" -> randyState;
                case "group" -> groupState;
                default -> throw new InvalidValueException("Unexpected value: " + algo);
            };
        }
    }
}