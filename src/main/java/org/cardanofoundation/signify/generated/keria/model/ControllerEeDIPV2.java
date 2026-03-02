package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DIPV2 variant for ControllerEe
 */
public record ControllerEeDIPV2(@JsonValue DIPV2 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeDIPV2 {}
}
