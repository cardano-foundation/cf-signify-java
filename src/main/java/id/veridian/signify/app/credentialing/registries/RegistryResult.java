package id.veridian.signify.app.credentialing.registries;

import id.veridian.signify.cesr.Serder;
import id.veridian.signify.generated.keria.model.RegistryOperation;

import java.util.List;

public record RegistryResult(Serder regser, Serder serder, List<String> sigs, RegistryOperation opInstance) {
    public RegistryOperation op() {
        return opInstance;
    }
}
