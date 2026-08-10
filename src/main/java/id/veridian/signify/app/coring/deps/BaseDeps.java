package org.cardanofoundation.signify.app.coring.deps;

import java.net.http.HttpResponse;
import java.util.Map;

public interface BaseDeps {
    HttpResponse<String> fetch(
        String pathname,
        String method,
        Object body,
        Map<String, String> extraHeaders
    );

    HttpResponse<String> fetch(
        String pathname,
        String method,
        Object body
    );
}
