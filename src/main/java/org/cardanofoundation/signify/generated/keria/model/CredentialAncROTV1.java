package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ROTV1 variant for CredentialAnc
 */
public record CredentialAncROTV1(
    @JsonValue
    String value
) implements CredentialAnc {
}
