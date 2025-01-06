package org.cardanofoundation.signify.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class States {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class State {
        @JsonProperty("vn")
        private  int[] vn;
        @JsonProperty("i")
        private  String i;
        @JsonProperty("s")
        private  String s;
        @JsonProperty("p")
        private  String p;
        @JsonProperty("d")
        private  String d;
        @JsonProperty("f")
        private  String f;
        @JsonProperty("dt")
        private  String dt;
        @JsonProperty("et")
        private  String et;
        @JsonProperty("kt")
        private  Object kt;
        @JsonProperty("k")
        private  List<String> k;
        @JsonProperty("nt")
        private  Object nt;
        @JsonProperty("n")
        private  List<String> n;
        @JsonProperty("bt")
        private  String bt;
        @JsonProperty("b")
        private  List<String> b;
        @JsonProperty("c")
        private  List<String> c;
        @JsonProperty("ee")
        private  EstablishmentState ee;
        @JsonProperty("di")
        private  String di;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EstablishmentState {
        private  String d;
        private  String s;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SaltyState {
        private  String sxlt;
        private  int pidx;
        private  int kidx;
        private  String stem;
        private  Tier tier;
        private  String dcode;
        private  List<String> icodes;
        private  List<String> ncodes;
        private  boolean transferable;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RandyState {
        private  List<String> prxs;
        private  List<String> nxts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class GroupState {
        private  HabState mhab;
        private  List<String> keys;
        private  List<String> ndigs;
    }

    @Getter
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HabState {
        private  String name;
        private  String prefix;
        private  boolean transferable;
        private  State state;
        private  List<Object> windexes;
        private  SaltyState salty;
        private  RandyState randy;
        private  GroupState group;

        public boolean containsKey(String algo) {
            return switch (algo) {
                case "salty" -> salty != null;
                case "randy" -> randy != null;
                case "group" -> group != null;
                default -> throw new InvalidValueException("Unexpected value: " + algo);
            };
        }

        public Object get(String algo) {
            return switch (algo) {
                case "salty" -> salty;
                case "randy" -> randy;
                case "group" -> group;
                default -> throw new InvalidValueException("Unexpected value: " + algo);
            };
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static List<States.State> convertToStates(List<?> nestedStates) {
        List<States.State> flatStates = new ArrayList<>();

        if (nestedStates == null) {
            return flatStates;
        }

        for (Object outerItem : nestedStates) {
            if (outerItem instanceof List<?> innerList) {
                for (Object innerItem : innerList) {
                    if (innerItem instanceof Map) {
                        States.State state = objectMapper.convertValue(innerItem, States.State.class);
                        flatStates.add(state);
                    } else if (innerItem instanceof States.State) {
                        flatStates.add((States.State) innerItem);
                    }
                }
            }
        }

        return flatStates;
    }
}