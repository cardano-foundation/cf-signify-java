package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DIPV1 variant for CredentialAnc
 */
public record CredentialAncDIPV1(@JsonValue DIPV1 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncDIPV1 {}
}
