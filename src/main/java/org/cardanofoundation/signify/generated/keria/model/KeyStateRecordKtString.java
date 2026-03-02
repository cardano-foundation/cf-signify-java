package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * String variant for KeyStateRecordKt
 */
public record KeyStateRecordKtString(@JsonValue String value) implements KeyStateRecordKt {
    @JsonCreator
    public KeyStateRecordKtString {}
}
