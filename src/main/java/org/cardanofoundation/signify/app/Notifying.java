package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Httping;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Notification;
import org.cardanofoundation.signify.app.ExnMessageTypes.TypedExchange;
import static org.cardanofoundation.signify.app.ExnMessages.isRoute;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asTyped;
import org.cardanofoundation.signify.generated.keria.model.NotificationData;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Notifying {
    @Getter
    public static class Notifications {
        private final SignifyClient client;

        /**
         * Notifications
         * @param client {SignifyClient}
         */
        public Notifications(SignifyClient client) {
            this.client = client;
        }

        /**
         * List notifications
         * @param start Start index of list of notifications, defaults to 0
         * @param end End index of list of notifications, defaults to 24
         * @return List of notifications
         */
        public NotificationListResponse list(int start, int end) throws IOException, InterruptedException, LibsodiumException {
            Map<String, String> extraHeaders = Map.of(
                    "Range", String.format("notes=%d-%d", start, end)
            );

            String path = "/notifications";
            String method = "GET";
            HttpResponse<String> res = this.client.fetch(path, method, null, extraHeaders);

            String cr = res.headers().firstValue("content-range").orElse(null);
            Httping.RangeInfo range = Httping.parseRangeHeaders(cr, "notes");

            return new NotificationListResponse(
                    range.start(),
                    range.end(),
                    range.total(),
                    parseNotifications(res.body())
            );
        }

        public NotificationListResponse list() throws IOException, InterruptedException, LibsodiumException {
            return list(0, 24);
        }

        public NotificationListResponse list(int start) throws IOException, InterruptedException, LibsodiumException {
            return list(start, 24);
        }

        public Optional<ResolvedTypedExchange> resolveExchange(Notification notification) throws Exception {
            NotificationData data = notification == null ? null : notification.getA();
            if (data == null) {
                return Optional.empty();
            }

            String route = data.getR();
            String said = data.getD();

            if (route == null) {
                Object additionalRoute = data.getAdditionalProperty("r");
                if (additionalRoute instanceof String additionalRouteString) {
                    route = additionalRouteString;
                }
            }
            if (said == null) {
                Object additionalSaid = data.getAdditionalProperty("d");
                if (additionalSaid instanceof String additionalSaidString) {
                    said = additionalSaidString;
                }
            }

            return resolveExchange(route, said);
        }

        public Optional<ResolvedTypedExchange> resolveExchange(String route, String said) throws Exception {
            if (route == null || said == null || said.isBlank()) {
                return Optional.empty();
            }

            String normalizedRoute = normalizeRoute(route);
            return this.client.exchanges()
                .get(said)
                .flatMap(exchangeResource -> resolveExchange(normalizedRoute, exchangeResource));
        }

        public Optional<ResolvedTypedExchange> resolveExchange(String route, ExchangeResource exchangeResource) {
            if (route == null || exchangeResource == null) {
                return Optional.empty();
            }

            String normalizedRoute = normalizeRoute(route);
            if (!isRoute(exchangeResource, normalizedRoute)) {
                return Optional.empty();
            }
            return asTyped(exchangeResource)
                .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
        }

        public String mark(String said) throws IOException, InterruptedException, LibsodiumException {
            String path = "/notifications/" + said;
            String method = "PUT";
            HttpResponse<String> response = this.client.fetch(path, method, null);
            return response.body();
        }

        /**
         * Delete a notification
         * @param said SAID of the notification
         */
        public void delete(String said) throws IOException, InterruptedException, LibsodiumException {
            String path = "/notifications/" + said;
            String method = "DELETE";
            this.client.fetch(path, method, null);
        }

        private static String normalizeRoute(String route) {
            if (route == null) {
                return null;
            }
            String trimmed = route.trim();
            if (trimmed.startsWith("/exn/")) {
                return trimmed.substring(4);
            }
            return trimmed;
        }

        private static List<Notification> parseNotifications(String notesJson) {
            if (notesJson == null || notesJson.isBlank()) {
                return List.of();
            }
            return Utils.fromJson(notesJson, new TypeReference<List<Notification>>() {});
        }

        public record NotificationListResponse(int start, int end, int total, List<Notification> notes) {
        }

        public record ResolvedTypedExchange(String route, ExchangeResource exchange, TypedExchange typed) {
        }

    }
}
