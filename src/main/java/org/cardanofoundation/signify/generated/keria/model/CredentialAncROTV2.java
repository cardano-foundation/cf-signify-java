package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ROTV2 variant for CredentialAnc
 */
public record CredentialAncROTV2(@JsonValue ROTV2 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncROTV2 {}
}
