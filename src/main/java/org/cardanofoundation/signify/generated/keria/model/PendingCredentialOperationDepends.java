package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PendingCredentialOperationDepends implements CredentialOperationDepends {

    @JsonProperty("name")
    private String name;

    @Override
    public boolean isDone() {
        return false;
    }
}
