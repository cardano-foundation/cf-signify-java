package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DRTV1 variant for CredentialAnc
 */
public record CredentialAncDRTV1(
    @JsonValue
    String value
) implements CredentialAnc {
}
