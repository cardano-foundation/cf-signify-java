package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Salter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotifyingTest extends BaseMockServerTest {

    @Test
    void testNotifications() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Notifying.Notifications notifications = client.notifications();

        notifications.list(20, 40);
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/notifications", request.getPath());
        assertEquals("notes=20-40", request.getHeader("Range"));

        notifications.mark("notificationSAID");
        request = mockWebServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/notifications/notificationSAID", request.getPath());

        notifications.delete("notificationSAID");
        request = mockWebServer.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/notifications/notificationSAID", request.getPath());
    }
}