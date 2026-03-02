package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DRTV2 variant for ControllerEe
 */
public record ControllerEeDRTV2(@JsonValue DRTV2 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeDRTV2 {}
}
