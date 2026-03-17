package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CredentialStateDeserializer.class)
public abstract class CredentialStateMixin {
}
