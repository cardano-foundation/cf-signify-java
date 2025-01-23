package org.cardanofoundation.signify.app;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotifyingTest extends BaseMockServerTest {

    @Override
    public HttpResponse<String> mockFetch(String path) {
        String body = path.startsWith("/identifiers/aid1/credentials")
            ? MOCK_CREDENTIAL
            : MOCK_GET_AID;

        return createMockResponse(body);
    }

    @Override
    public HttpResponse<String> createMockResponse(String body) {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn(body);
        when(httpResponse.statusCode()).thenReturn(202);

        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.firstValue("content-range")).thenReturn(Optional.of("notes 0-25/1000"));
        when(httpResponse.headers()).thenReturn(headers);

        return httpResponse;
    }

    @Test
    void testNotifications() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Notifying.Notifications notifications = client.getNotifications();

        notifications.list(20, 40);
        Mockito.verify(client).fetch(
            eq("/notifications"),
            eq("GET"),
            isNull(),
            any()
        );
        verifyRequestHeader("Range", "notes=20-40");

        notifications.mark("notificationSAID");
        Mockito.verify(client).fetch(
            eq("/notifications/notificationSAID"),
            eq("PUT"),
            isNull(),
            any()
        );

        notifications.delete("notificationSAID");
        Mockito.verify(client).fetch(
            eq("/notifications/notificationSAID"),
            eq("DELETE"),
            isNull(),
            any()
        );
    }
}