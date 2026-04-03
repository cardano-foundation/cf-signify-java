package org.cardanofoundation.signify.app.aiding;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.Operation;

public record EventResult<T extends Operation>(Serder serder, List<String> sigs, HttpResponse<String> response, Class<T> opType) {

    public T op() throws IOException {
        return Utils.fromJson(response.body(), opType);
    }
}