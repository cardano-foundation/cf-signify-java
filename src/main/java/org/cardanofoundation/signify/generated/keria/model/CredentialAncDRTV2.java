package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DRTV2 variant for CredentialAnc
 */
public record CredentialAncDRTV2(
    @JsonValue
    String value
) implements CredentialAnc {
}
