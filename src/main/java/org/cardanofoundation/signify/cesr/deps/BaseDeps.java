package org.cardanofoundation.signify.cesr.deps;


import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

public interface BaseDeps {
    HttpResponse<String> fetch(
        String pathname,
        String method,
        Object body,
        HttpHeaders headers
    );
}
