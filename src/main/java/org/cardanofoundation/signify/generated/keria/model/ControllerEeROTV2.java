package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ROTV2 variant for ControllerEe
 */
public record ControllerEeROTV2(@JsonValue ROTV2 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeROTV2 {}
}
