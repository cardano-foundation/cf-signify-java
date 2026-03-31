package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = KeyStateRecordKtDeserializer.class)
public abstract class KeyStateRecordKtMixIn {}
