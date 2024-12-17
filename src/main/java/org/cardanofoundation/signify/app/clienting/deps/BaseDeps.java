package org.cardanofoundation.signify.app.clienting.deps;

import com.goterl.lazysodium.exceptions.SodiumException;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Map;

public interface BaseDeps {
    HttpResponse<String> fetch(
        String pathname,
        String method,
        Object body,
        Map<String, String> extraHeaders
    ) throws SodiumException, InterruptedException, IOException;
}