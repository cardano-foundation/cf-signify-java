package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ICPV1 variant for CredentialAnc
 */
public record CredentialAncICPV1(
    @JsonValue
    String value
) implements CredentialAnc {
}
