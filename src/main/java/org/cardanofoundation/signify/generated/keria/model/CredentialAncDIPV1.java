package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DIPV1 variant for CredentialAnc
 */
public record CredentialAncDIPV1(
    @JsonValue
    String value
) implements CredentialAnc {
}
