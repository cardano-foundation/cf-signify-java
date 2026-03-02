package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ICPV2 variant for ControllerEe
 */
public record ControllerEeICPV2(@JsonValue ICPV2 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeICPV2 {}
}
