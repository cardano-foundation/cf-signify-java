package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * MixIn to remove @JsonTypeInfo annotation from KeyStateRecordKt sealed interface.
 * By applying this MixIn, Jackson will not try to validate type information,
 * allowing our custom deserializer to handle the deserialization.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY, property = "")
public interface KeyStateRecordKtMixIn {
}
