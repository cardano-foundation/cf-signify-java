package id.veridian.signify.app.credentialing.registries;

import lombok.Getter;
import lombok.Setter;
import id.veridian.signify.cesr.Serder;

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

    public String op() {
        return response.body();
    }
}