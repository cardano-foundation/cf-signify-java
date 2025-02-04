package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Httping;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

public class Notifying {
    @Getter
    public static class Notifications {
        private final SignifyClient client;
        private final ObjectMapper objectMapper = new ObjectMapper();

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
        public NotificationListResponse list(int start, int end) throws SodiumException, IOException, InterruptedException {
            Map<String, String> extraHeaders = Map.of(
                "Range", String.format("notes=%d-%d", start, end)
            );

            String path = "/notifications";
            String method = "GET";
            HttpResponse<String> res = client.fetch(path, method, null, extraHeaders);

            String cr = res.headers().firstValue("content-range").orElse(null);
            Httping.RangeInfo range = Httping.parseRangeHeaders(cr, "notes");
            Object notes = Utils.fromJson(res.body(), Object.class);

            return new NotificationListResponse(
                range.start(),
                range.end(),
                range.total(),
                notes
            );
        }

        public NotificationListResponse list() throws SodiumException, IOException, InterruptedException {
            return list(0, 24);
        }

        public NotificationListResponse list(int start) throws SodiumException, IOException, InterruptedException {
            return list(start, 24);
        }

        /**
         * Mark a notification as read
         * @param said SAID of the notification
         * @return Result of the marking
         */
        public String mark(String said) throws SodiumException, IOException, InterruptedException {
            String path = "/notifications/" + said;
            String method = "PUT";

            HttpResponse<String> response = this.client.fetch(path, method, null, null);
            return response.body();
        }

        /**
         * Delete a notification
         * @param said SAID of the notification
         */
        public void delete(String said) throws SodiumException, IOException, InterruptedException {
            String path = "/notifications/" + said;
            String method = "DELETE";

            this.client.fetch(path, method, null, null);
        }

        public record NotificationListResponse(int start, int end, int total, Object notes) {}
    }
}
