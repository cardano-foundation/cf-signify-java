package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;

/**
 * ListString variant for KeyStateRecordKt
 */
public record KeyStateRecordKtListString(@JsonValue List<String> value) implements KeyStateRecordKt {
    @JsonCreator
    public KeyStateRecordKtListString {}
}
