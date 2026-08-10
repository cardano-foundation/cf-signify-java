package org.cardanofoundation.signify.e2e.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class IssuerRegistry {
    private String regk;
    private String name;
    private String pre;
    private Map<String, Object> state;
}
