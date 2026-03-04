package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ICPV2 variant for CredentialAnc
 */
public record CredentialAncICPV2(
    @JsonValue
    String value
) implements CredentialAnc {
}
