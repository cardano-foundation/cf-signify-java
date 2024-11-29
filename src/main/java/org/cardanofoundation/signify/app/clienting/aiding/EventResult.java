package org.cardanofoundation.signify.app.clienting.aiding;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.cardanofoundation.signify.cesr.Serder;
import org.springframework.http.ResponseEntity;

public record EventResult(Serder serder, List<String> sigs, CompletableFuture<ResponseEntity<String>> promise) {

    public Object op() throws InterruptedException, ExecutionException {
        ResponseEntity<String> res = promise.get();
        return res.getBody();
    }
}