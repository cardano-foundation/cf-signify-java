package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Jackson mixin to override deserialization of kt and nt fields in KeyStateRecord.
 * Marks these fields to be ignored during normal deserialization,
 * then provides custom getter/setter with Object type.
 */
@JsonIgnoreProperties(value = {"kt", "nt"}, allowGetters = true, allowSetters = false)
public abstract class KeyStateRecordMixin {
    
    @JsonProperty("kt")
    @JsonDeserialize(using = KeyStateRecordKtDeserializer.class)
    private Object kt;
    
    @JsonProperty("nt")
    @JsonDeserialize(using = KeyStateRecordKtDeserializer.class)
    private Object nt;
}
