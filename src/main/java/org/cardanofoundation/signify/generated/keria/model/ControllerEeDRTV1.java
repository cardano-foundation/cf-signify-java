package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DRTV1 variant for ControllerEe
 */
public record ControllerEeDRTV1(@JsonValue DRTV1 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeDRTV1 {}
}
