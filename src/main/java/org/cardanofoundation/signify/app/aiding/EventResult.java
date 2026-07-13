package org.cardanofoundation.signify.app.aiding;

import java.net.http.HttpResponse;
import java.util.List;

import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.generated.keria.model.Operation;

public record EventResult<T extends Operation>(Serder serder, List<String> sigs, T opInstance) {
    public T op() {
        return opInstance;
    }
}