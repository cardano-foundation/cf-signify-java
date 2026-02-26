package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ICPV2 variant for CredentialAnc
 */
public record CredentialAncICPV2(@JsonValue ICPV2 value) implements CredentialAnc {
    @JsonCreator
    public CredentialAncICPV2 {}
}
