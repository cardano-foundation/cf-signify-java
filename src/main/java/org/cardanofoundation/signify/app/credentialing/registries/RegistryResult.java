package org.cardanofoundation.signify.app.credentialing.registries;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.RegistryOperation;

import java.net.http.HttpResponse;
import java.util.List;

@Getter
@Setter
public class RegistryResult {
    private final Serder regser;
    private final Serder serder;
    private final List<String> sigs;
    private final HttpResponse<String> response;

    public RegistryResult(Serder regser, Serder serder, List<String> sigs, HttpResponse<String> response) {
        this.regser = regser;
        this.serder = serder;
        this.sigs = sigs;
        this.response = response;
    }

    public RegistryOperation op() {
        return Utils.fromJson(response.body(), RegistryOperation.class);
    }
}