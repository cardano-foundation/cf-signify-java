package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DIPV2 variant for CredentialAnc
 */
public record CredentialAncDIPV2(@JsonValue DIPV2 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncDIPV2 {}
}
