package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.cardanofoundation.signify.app.ExnMessages.IpexApplyExchange;
import org.cardanofoundation.signify.app.Notifying.Notifications.NotificationListResponse;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotifyingTest extends BaseMockServerTest {

    @Test
    void testNotifications() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
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

        NotificationListResponse page = notifications.list();
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/notifications", request.getPath());
        assertEquals(1, page.notes().size());
        assertEquals("/exn/ipex/apply", page.notes().getFirst().getA().getR());

        // the notification carries the said; fetch the typed exchange via exchanges()
        var typed = client.exchanges().getTyped(page.notes().getFirst().getA().getD());
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/exchanges/EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei", request.getPath());
        assertTrue(typed.isPresent());
        assertTrue(typed.orElseThrow() instanceof IpexApplyExchange);
    }
}