package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ROTV1 variant for ControllerEe
 */
public record ControllerEeROTV1(@JsonValue ROTV1 value) implements ControllerEe {
    @JsonCreator
    public ControllerEeROTV1 {}
}
