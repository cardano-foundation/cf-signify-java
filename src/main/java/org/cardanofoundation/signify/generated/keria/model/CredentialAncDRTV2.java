package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DRTV2 variant for CredentialAnc
 */
public record CredentialAncDRTV2(@JsonValue DRTV2 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncDRTV2 {}
}
