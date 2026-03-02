package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * IXNV1 variant for CredentialAnc
 */
public record CredentialAncIXNV1(@JsonValue IXNV1 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncIXNV1 {}
}
