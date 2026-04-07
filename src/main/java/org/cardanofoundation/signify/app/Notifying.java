package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Httping;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Notification;
import static org.cardanofoundation.signify.app.ExnMessages.*;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asIpexAdmit;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asIpexAgree;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asIpexApply;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asIpexGrant;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asIpexOffer;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigExn;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigIcp;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigIss;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigIxn;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigRev;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigRot;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigRpy;
import static org.cardanofoundation.signify.app.ExnMessageTypes.asMultisigVcp;
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
                    res.body()
            );
        }

        public NotificationListResponse list() throws IOException, InterruptedException, LibsodiumException {
            return list(0, 24);
        }

        public NotificationListResponse list(int start) throws IOException, InterruptedException, LibsodiumException {
            return list(start, 24);
        }

        public TypedNotificationListResponse listTyped(int start, int end) throws IOException, InterruptedException, LibsodiumException {
            NotificationListResponse page = list(start, end);
            return new TypedNotificationListResponse(
                page.start(),
                page.end(),
                page.total(),
                parseNotifications(page.notes())
            );
        }

        public TypedNotificationListResponse listTyped() throws IOException, InterruptedException, LibsodiumException {
            return listTyped(0, 24);
        }

        public TypedNotificationListResponse listTyped(int start) throws IOException, InterruptedException, LibsodiumException {
            return listTyped(start, 24);
        }

        public ResolvedNotificationListResponse listResolved(int start, int end) throws IOException, InterruptedException, LibsodiumException {
            TypedNotificationListResponse page = listTyped(start, end);
            List<ResolvedNotification> resolved = page.notes().stream()
                .map(note -> new ResolvedNotification(note, safeResolveExchange(note)))
                .toList();

            return new ResolvedNotificationListResponse(page.start(), page.end(), page.total(), resolved);
        }

        public ResolvedNotificationListResponse listResolved() throws IOException, InterruptedException, LibsodiumException {
            return listResolved(0, 24);
        }

        public ResolvedNotificationListResponse listResolved(int start) throws IOException, InterruptedException, LibsodiumException {
            return listResolved(start, 24);
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
            return switch (normalizedRoute) {
                case MULTISIG_ICP_ROUTE -> asMultisigIcp(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case MULTISIG_ROT_ROUTE -> asMultisigRot(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case MULTISIG_IXN_ROUTE -> asMultisigIxn(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case MULTISIG_RPY_ROUTE -> asMultisigRpy(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case MULTISIG_VCP_ROUTE -> asMultisigVcp(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case MULTISIG_ISS_ROUTE -> asMultisigIss(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case MULTISIG_EXN_ROUTE -> asMultisigExn(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case MULTISIG_REV_ROUTE -> asMultisigRev(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case IPEX_GRANT_ROUTE -> asIpexGrant(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case IPEX_OFFER_ROUTE -> asIpexOffer(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case IPEX_APPLY_ROUTE -> asIpexApply(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case IPEX_AGREE_ROUTE -> asIpexAgree(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                case IPEX_ADMIT_ROUTE -> asIpexAdmit(exchangeResource)
                    .map(value -> new ResolvedTypedExchange(normalizedRoute, exchangeResource, value));
                default -> Optional.empty();
            };
        }

        /**
         * Mark a notification as read
         * @param said SAID of the notification
         * @return Result of the marking
         */
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

            try {
                return Utils.fromJson(notesJson, new TypeReference<List<Notification>>() {});
            } catch (RuntimeException ex) {
                return List.of();
            }
        }

        private Optional<ResolvedTypedExchange> safeResolveExchange(Notification notification) {
            try {
                return resolveExchange(notification);
            } catch (Exception ex) {
                return Optional.empty();
            }
        }

        public record NotificationListResponse(int start, int end, int total, String notes) {
        }

        public record TypedNotificationListResponse(int start, int end, int total, List<Notification> notes) {
        }

        public record ResolvedTypedExchange(String route, ExchangeResource exchange, Object typed) {
        }

        public record ResolvedNotification(Notification notification, Optional<ResolvedTypedExchange> exchange) {
        }

        public record ResolvedNotificationListResponse(int start, int end, int total, List<ResolvedNotification> notes) {
        }
    }
}
