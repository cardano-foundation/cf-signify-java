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
    public static class State {
        private  int[] vn;
        private  String i;
        private  String s;
        private  String p;
        private  String d;
        private  String f;
        private  String dt;
        private  String et;
        private  Object kt;
        private  List<String> k;
        private  Object nt;
        private  List<String> n;
        private  String bt;
        private  List<String> b;
        private  List<String> c;
        private  EstablishmentState ee;
        private  String di;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
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
        String icp_dt;
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
}