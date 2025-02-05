package org.cardanofoundation.signify.app.coring.deps;

import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

public interface BaseDeps {
    HttpResponse<String> fetch(
        String pathname,
        String method,
        Object body,
        Map<String, String> extraHeaders
    ) throws LibsodiumException, InterruptedException, IOException;

    HttpResponse<String> fetch(
        String pathname,
        String method,
        Object body
    ) throws LibsodiumException, InterruptedException, IOException;
}