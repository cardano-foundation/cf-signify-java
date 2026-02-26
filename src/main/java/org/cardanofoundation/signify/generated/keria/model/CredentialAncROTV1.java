package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ROTV1 variant for CredentialAnc
 */
public record CredentialAncROTV1(@JsonValue ROTV1 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncROTV1 {}
}
