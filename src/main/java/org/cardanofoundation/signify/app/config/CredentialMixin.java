package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Mixin to override generated Credential deserialization.
 */
@JsonDeserialize(using = CredentialDeserializer.class)
public interface CredentialMixin {
}
