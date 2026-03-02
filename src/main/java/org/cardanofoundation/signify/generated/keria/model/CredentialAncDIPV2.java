package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DIPV2 variant for CredentialAnc
 */
public record CredentialAncDIPV2(
    @JsonValue
    String value
) implements CredentialAnc {
}
