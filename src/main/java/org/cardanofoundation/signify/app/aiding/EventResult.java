package id.veridian.signify.app.aiding;

import java.net.http.HttpResponse;
import java.util.List;

import id.veridian.signify.cesr.Serder;

public record EventResult(Serder serder, List<String> sigs, HttpResponse<String> response) {

    public Object op() {
        return response.body();
    }
}