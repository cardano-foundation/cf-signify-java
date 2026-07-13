package org.cardanofoundation.signify.app.credentialing.registries;

import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.generated.keria.model.RegistryOperation;

import java.util.List;

public record RegistryResult(Serder regser, Serder serder, List<String> sigs, RegistryOperation opInstance) {
    public RegistryOperation op() {
        return opInstance;
    }
}
