package org.cardanofoundation.signify.app.clienting.aiding;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.cardanofoundation.signify.cesr.Serder;

public record EventResult(Serder serder, List<String> sigs, CompletableFuture<HttpResponse<String>> promise) {

    public Object op() throws InterruptedException, ExecutionException {
        HttpResponse<String> res = promise.get();
        return res.body();
    }
}