package org.cardanofoundation.signify.app.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson mixin to handle polymorphic deserialization of kt and nt fields in KeyStateRecord.
 * These fields can be either String or List<String> in JSON.
 * By declaring them as Object, Jackson deserializes them naturally without casting issues.
 */
@JsonIgnoreProperties(value = {"kt", "nt"}, allowGetters = true, allowSetters = false)
public abstract class KeyStateRecordMixin {
    
    @JsonProperty("kt")
    private Object kt;
    
    @JsonProperty("nt")
    private Object nt;
}
