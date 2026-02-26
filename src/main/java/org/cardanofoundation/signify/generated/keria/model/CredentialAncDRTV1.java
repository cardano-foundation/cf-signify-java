package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DRTV1 variant for CredentialAnc
 */
public record CredentialAncDRTV1(@JsonValue DRTV1 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncDRTV1 {}
}
