package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * IXNV2 variant for CredentialAnc
 */
public record CredentialAncIXNV2(@JsonValue IXNV2 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncIXNV2 {}
}
