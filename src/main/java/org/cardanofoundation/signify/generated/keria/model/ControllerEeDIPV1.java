package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DIPV1 variant for ControllerEe
 */
public record ControllerEeDIPV1(@JsonValue DIPV1 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeDIPV1 {}
}
