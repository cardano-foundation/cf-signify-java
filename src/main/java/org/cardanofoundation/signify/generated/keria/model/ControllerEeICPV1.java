package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ICPV1 variant for ControllerEe
 */
public record ControllerEeICPV1(@JsonValue ICPV1 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeICPV1 {}
}
