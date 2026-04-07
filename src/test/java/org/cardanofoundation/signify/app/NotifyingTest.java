package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.Notification;
import org.cardanofoundation.signify.generated.keria.model.NotificationData;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.cardanofoundation.signify.app.ExnMessageTypes.IpexApplyExchange;
import static org.cardanofoundation.signify.app.ExnMessages.IPEX_APPLY_ROUTE;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

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

        Notification note = new Notification();
        note.setA(new NotificationData().r("/exn/ipex/grant").d("notificationSAID"));

        assertTrue(notifications.resolveExchange(note).isEmpty());
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/exchanges/notificationSAID", request.getPath());

        Notifying.Notifications.TypedNotificationListResponse typedPage = notifications.listTyped();
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/notifications", request.getPath());
        assertEquals(1, typedPage.notes().size());
        assertEquals("/exn/ipex/apply", typedPage.notes().getFirst().getA().getR());

        Notifying.Notifications.ResolvedNotificationListResponse resolvedPage = notifications.listResolved();
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/notifications", request.getPath());
        request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/exchanges/EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei", request.getPath());
        assertTrue(resolvedPage.notes().getFirst().exchange().isPresent());
        assertTrue(resolvedPage.notes().getFirst().exchange().orElseThrow().typed() instanceof IpexApplyExchange);
    }

    @Test
    void testResolveExchangeWithProvidedResource() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Tier.LOW, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Notifying.Notifications notifications = client.notifications();

        Exn exn = new Exn();
        exn.setR(IPEX_APPLY_ROUTE);
        exn.setA(new LinkedHashMap<>(Map.of("m", "hello")));
        exn.setE(new LinkedHashMap<>(Map.of("d", "embed-d")));

        ExchangeResource exchangeResource = new ExchangeResource();
        exchangeResource.setExn(exn);
        exchangeResource.setPathed(new LinkedHashMap<>());

        var resolved = notifications.resolveExchange("/exn/ipex/apply", exchangeResource);
        assertTrue(resolved.isPresent());
        assertEquals(IPEX_APPLY_ROUTE, resolved.orElseThrow().route());
        assertTrue(resolved.orElseThrow().typed() instanceof IpexApplyExchange);
    }
}