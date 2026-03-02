package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ROTV2 variant for CredentialAnc
 */
public record CredentialAncROTV2(
    @JsonValue
    String value
) implements CredentialAnc {
}
